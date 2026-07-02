# Spring `@Transactional` + JDBI 3 Incompatibility

> This document addresses a real production issue discovered when running multithreaded
> tests against a Spring Boot + JDBI 3 + PostgreSQL stack.

---

## The Root Cause

Spring's `@Transactional` works by binding a JDBC `Connection` to the **current thread** before
entering the annotated method. Any code that asks for a connection through the *same* `DataSource`
gets that already-open, already-in-transaction connection.

**JDBI breaks this assumption.**

When you call `jdbi.withHandle(...)` or `jdbi.inTransaction(...)`, JDBI calls
`DataSource.getConnection()` directly — it does **not** go through Spring's
`DataSourceUtils.getConnection()`, which is the method that returns the thread-bound connection.

```
Spring @Transactional proxy
  └── binds Connection C1 to thread
        └── VisitRepository.insertVisit()
              └── jdbi.withHandle(h -> ...)   ← JDBI calls ds.getConnection()
                                                 gets Connection C2 (NEW connection!)
        └── AuditRepository.insertAuditLog()
              └── jdbi.withHandle(h -> ...)   ← gets Connection C3 (yet another!)
```

**Result:** each `withHandle` call runs on its own autocommit connection.  
The three connections are completely independent — `@Transactional` provides **zero atomicity**.

---

## Why `READ_COMMITTED` (the PostgreSQL default) Makes It Worse

Even if the connections *were* unified, PostgreSQL's default isolation (`READ_COMMITTED`) takes
a fresh snapshot **per statement**. A competing transaction can insert or update rows between
any two statements in your "transaction", and your code will see those changes.

```
Thread 1 (service method)         Thread 2 (concurrent request)
SELECT seats_available → 3
                                   UPDATE seats SET available = 0; COMMIT
INSERT INTO booking ...            ← books a seat that's already gone
COMMIT
```

`@Transactional` alone does **not** prevent this under `READ_COMMITTED`.

---

## Why `jdbi3-spring5` Alone Doesn't Fix It

`jdbi3-spring5` adds a `SpringTransactionHandler` that makes JDBI's *own*
`@Transaction` / `inTransaction` calls Spring-aware. It does **not** retroactively
make `jdbi.withHandle()` calls inside a `@Transactional` method share the
Spring-managed connection.

---

## ✅ Fix 1: `TransactionAwareDataSourceProxy`

Spring ships a proxy that wraps your `DataSource` and overrides `getConnection()` to
return the thread-bound connection when a Spring transaction is active.  
Wrapping JDBI's `DataSource` in it closes the gap completely.

```java
@Bean
DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
    ds.setUsername("postgres");
    ds.setPassword("pachhigares");
    return ds;
}

@Bean
Jdbi jdbi(DataSource dataSource) {
    // Wrap in the proxy so JDBI's getConnection() honours Spring's thread-bound tx
    TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(dataSource);
    return Jdbi.create(proxy)
               .installPlugin(new PostgresPlugin())
               .installPlugin(new SqlObjectPlugin());
}

@Bean
DataSourceTransactionManager transactionManager(DataSource dataSource) {
    // The *unwrapped* DataSource — the tx manager owns the real connection lifecycle
    return new DataSourceTransactionManager(dataSource);
}
```

With this in place:

```
Spring @Transactional proxy
  └── binds Connection C1 to thread
        └── VisitRepository.insertVisit()
              └── jdbi.withHandle(h -> ...)   ← proxy returns C1  ✅
        └── AuditRepository.insertAuditLog()
              └── jdbi.withHandle(h -> ...)   ← proxy returns C1  ✅
  └── COMMIT C1
```

Both DAOs now share a single connection and a single transaction.

> **Important:** pass the **raw** `DataSource` (not the proxy) to `DataSourceTransactionManager`.
> The transaction manager must open and close the *real* connection; the proxy just borrows it.

---

## ✅ Fix 2: Raise the Isolation Level

`TransactionAwareDataSourceProxy` solves the connection-sharing problem.  
To also prevent phantom/non-repeatable reads between concurrent service calls, raise
the isolation level on the `@Transactional` annotation:

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public long saveVisitWithAudit(MuseumVisit v) { ... }
```

Or for the strongest guarantee (e.g. seat reservation, inventory):

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void reserveSeat(long seatId, long userId) { ... }
```

PostgreSQL will signal serialization failures via a `PSQLException`
(`ERROR: could not serialize access`). Spring re-throws it as a `DataAccessException`,
which you should catch and retry at the call site:

```java
int attempts = 0;
while (attempts++ < 3) {
    try {
        svc.reserveSeat(seatId, userId);
        return;
    } catch (DataAccessException e) {
        if (!isSerializationFailure(e) || attempts == 3) throw e;
    }
}
```

---

## ✅ Fix 3: PostgreSQL Advisory Locks (Targeted Workaround)

When changing isolation level project-wide is too risky, PostgreSQL **advisory locks**
let you serialize access to a single logical resource without touching isolation:

```java
@Transactional
public long saveVisitWithAudit(MuseumVisit v) {
    jdbi.withHandle(h -> {
        // Acquire an exclusive transaction-level lock keyed on the user id.
        // Any other session trying to acquire the same key will block until this tx ends.
        h.execute("SELECT pg_advisory_xact_lock(?)", v.getUserId());
        return null;
    });

    long visitId = visitRepo.insertVisit(v);
    auditRepo.insertAuditLog(visitId, "CREATE", "visit for userId=" + v.getUserId());
    return visitId;
}
```

`pg_advisory_xact_lock` is automatically released when the transaction commits or rolls back.  
This is the lightest-weight targeted fix, but it **serializes all concurrent calls for the same key**
— fine for per-user operations, bottleneck for shared resources.

---

## Decision Guide

| Situation | Recommended approach |
|---|---|
| Greenfield Spring + JDBI project | Use `TransactionAwareDataSourceProxy` from the start |
| Existing project, surgical fix | Add `TransactionAwareDataSourceProxy` + raise isolation on critical methods |
| Critical section with low contention | `pg_advisory_xact_lock` inside `@Transactional` |
| High-throughput with complex object graphs | Consider migrating to Spring Data JPA / Hibernate |
| JDBI is a hard requirement, no Spring | Use explicit `Handle` passing (`TransactionExample.java`) or JDBI `@Transaction` (`TransactionExample3.java`) — both are correct by construction |

---

## Summary

```
Problem layer          Root cause                          Fix
─────────────────────────────────────────────────────────────────────────────
Connection sharing     JDBI bypasses Spring's              TransactionAwareDataSourceProxy
                       DataSourceUtils.getConnection()

Isolation too weak     PostgreSQL default is               @Transactional(isolation=REPEATABLE_READ)
(phantom/NR reads)     READ_COMMITTED                      or SERIALIZABLE

Race on shared         Two txs pass the guard check        pg_advisory_xact_lock  OR  SERIALIZABLE
resource               before either commits               + retry on serialization failure
```

---

## See Also

- [`transactional-isolation.md`](./transactional-isolation.md) — isolation levels, MVCC, and how to set them in Spring and JDBI
- [`TransactionExample4.java`](../src/main/java/postgres/TransactionExample4.java) — Spring `@Transactional` wired with `TransactionAwareDataSourceProxy`
- [JDBI 3 Transactions (blog)](https://blog.anorakgirl.co.uk/2018/10/jdbi-3-transactions/)

