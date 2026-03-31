# Relational Databases — Engineering Reference

A hands-on reference for relational database fundamentals, performance engineering, and 
operational patterns across PostgreSQL, MySQL, Oracle, CockroachDB, and Redshift.

---

## Table of Contents

- [ACID Guarantees](#acid-guarantees)
- [Distributed Transactions & Consensus](#distributed-transactions--consensus)
- [Relational Algebra & JOINs](#relational-algebra--joins)
- [Indexing](#indexing)
- [Partitioning](#partitioning)
- [Data Archiving & WAL](#data-archiving--wal)
- [Connection Pooling](#connection-pooling)
- [Storage Internals — Database Pages](#storage-internals--database-pages)
- [Transaction Isolation & Locking](#transaction-isolation--locking)
- [High Availability & Replication](#high-availability--replication)
- [Performance Engineering](#performance-engineering)
- [Schema Design](#schema-design)
- [Database Engines](#database-engines)
- [Managed Database Services](#managed-database-services)

---

## ACID Guarantees

ACID is the foundational contract that a database makes to application code. Violating any property shifts correctness responsibility to the application layer.

| Property | Guarantee | Failure Scenario |
|---|---|---|
| **Atomicity** | A transaction is all-or-nothing. On crash or error, the engine rolls back to the pre-transaction state. | Partial writes on power loss |
| **Consistency** | Every committed transaction moves the database from one valid state to another, enforcing all constraints and rules. | Referential integrity violations |
| **Isolation** | Concurrent transactions produce the same result as serial execution. JDBC default: `TRANSACTION_READ_COMMITTED`. | Dirty reads, phantom reads |
| **Durability** | Committed data survives crashes, power loss, and restarts. Achieved via write-ahead logging (WAL). | Silent data loss after commit ACK |

**Key refs:**
- [ACID and database transactions (Stack Overflow)](http://stackoverflow.com/a/3740307/432903)
- [Isolation levels (Wikipedia)](https://en.wikipedia.org/wiki/Isolation_(database_systems))
- [JDBC isolation defaults (Oracle docs)](https://docs.oracle.com/cd/E19830-01/819-4721/beamv/index.html)

---

## Distributed Transactions & Consensus

### Two-Phase Commit (2PC)

2PC is the standard protocol for achieving **strong consistency** across distributed nodes. It involves a coordinator and one or more cohort nodes.

```
Coordinator (Master)                     Cohort (Secondary)
                       QUERY TO COMMIT
              ─────────────────────────────────>

                                          Execute transaction
                                    (writes to UNDO_LOG & REDO_LOG)
                        VOTE YES / NO
              <─────────────────────────────────

commit / abort        COMMIT / ROLLBACK
              ─────────────────────────────────>

                        ACKNOWLEDGMENT
              <─────────────────────────────────
end
```

**Phase 1 — Voting:** Coordinator sends `QUERY TO COMMIT`. Each cohort executes the transaction locally, logs it, then votes `YES` (prepared) or `NO` (abort).

**Phase 2 — Completion:** If all cohorts vote `YES`, coordinator sends `COMMIT`. Otherwise, sends `ROLLBACK`. Cohorts apply and acknowledge.

### 2PC Critical Weaknesses

| Weakness | Detail |
|---|---|
| **Blocking** | If the coordinator crashes after Phase 1, cohorts that voted `YES` are indefinitely blocked — they cannot unilaterally commit or abort. |
| **Single point of failure** | The coordinator's persistent failure leaves cohorts in an uncertain state. Requires manual intervention or a timeout-based heuristic. |
| **Latency** | Requires 2 full round-trips plus disk flushes on each node. |

**Mitigations:** 3PC (Three-Phase Commit) adds a `pre-commit` phase to reduce blocking, but at higher latency cost. Modern systems (Spanner, CockroachDB) use Paxos/Raft-based consensus instead.

**Refs:**
- [Two-Phase Commit (Wikipedia)](https://en.wikipedia.org/wiki/Two-phase_commit_protocol#Basic_algorithm)
- [2PC deep dive — The Paper Trail](http://the-paper-trail.org/blog/consensus-protocols-two-phase-commit/)
- [Distributed Algorithms — Cambridge](https://www.cl.cam.ac.uk/teaching/0809/DistSys/3-algs.pdf)

---

## Relational Algebra & JOINs

### Cartesian Product

The base operation that underpins all JOINs. Produces every combination of rows between two sets.

```
weights          = { [pkgA1, 100g], [pkgA2, 200g] }
shippingDates    = { [pkgB1, Jul], [pkgB2, Aug], [pkgB3, Sep] }

weights × shippingDates = {
  [pkgA1, 100g, pkgB1, Jul],  [pkgA1, 100g, pkgB2, Aug],  [pkgA1, 100g, pkgB3, Sep],
  [pkgA2, 200g, pkgB1, Jul],  [pkgA2, 200g, pkgB2, Aug],  [pkgA2, 200g, pkgB3, Sep]
}
```

### JOIN Types

| JOIN | Set Operation | Behaviour |
|---|---|---|
| `INNER JOIN` | A ∩ B | Only rows with matching keys in both tables |
| `LEFT OUTER JOIN` | A ∪ (A − B) | All rows from A; unmatched B rows filled with `NULL` |
| `RIGHT OUTER JOIN` | B ∪ (B − A) | All rows from B; unmatched A rows filled with `NULL` |
| `FULL OUTER JOIN` | A ∪ B | All rows from both; `NULL` where no match |
| `CROSS JOIN` | A × B | Full Cartesian product — use only when intentional |

```sql
-- Set difference (A − B): rows in A with no match in B
SELECT DISTINCT a.*
FROM a
LEFT OUTER JOIN b ON a.id = b.id
WHERE b.id IS NULL;
```

**Performance note:** JOINs are expensive because they are O(M × N) in the worst case (nested loop). Prefer hash joins or merge joins on large datasets, and always index JOIN key columns.

**Refs:**
- [Relational algebra (Wikipedia)](https://en.wikipedia.org/wiki/Relational_algebra)
- [The Join Operation — Use The Index, Luke](http://use-the-index-luke.com/sql/join)
- [When and why are relational database joins expensive?](https://stackoverflow.com/a/174047/432903)
- [Performance considerations for JOIN queries (Cloudera)](https://www.cloudera.com/documentation/enterprise/5-9-x/topics/impala_perf_joins.html)
- [LEFT vs LEFT OUTER JOIN (Stack Overflow)](http://stackoverflow.com/a/4401540/432903)

---

## Indexing

Indexes trade write amplification and storage for dramatically faster reads. Choose index types deliberately.

| Index Type | Best For |
|---|---|
| B-Tree (default) | Equality and range queries on high-cardinality columns |
| Hash | Equality-only lookups (avoid for ranges) |
| GIN / GiST | Full-text search, JSONB, array columns |
| BRIN | Very large append-only tables with natural sort order (e.g., time-series) |
| Partial index | Indexes on a subset of rows (e.g., `WHERE is_deleted = false`) |
| Composite index | Multi-column predicates — column order matters |

**Operational rules:**
- Index all foreign keys to avoid full-table scans on JOINs.
- Avoid indexing low-cardinality columns (e.g., boolean flags) — the planner will ignore them.
- Use `EXPLAIN ANALYZE` to confirm index usage before and after schema changes.
- `DEFERRABLE` constraints allow constraint checking at commit time, not row-insert time.

**Refs:**
- [PostgreSQL indexes (Heroku Dev Center)](https://devcenter.heroku.com/articles/postgresql-indexes)
- [SET CONSTRAINTS (PostgreSQL docs)](https://www.postgresql.org/docs/9.1/sql-set-constraints.html)
- [DEFERRABLE vs NOT DEFERRABLE (Stack Overflow)](https://stackoverflow.com/questions/5300307/not-deferrable-versus-deferrable-initially-immediate)

---

## Partitioning

Partitioning splits a logical table into physical sub-tables (partitions), pruning I/O at query time.

| Strategy | Use Case |
|---|---|
| **Range** | Time-series data — one partition per month/year |
| **List** | Discrete category splits — e.g., region, tenant |
| **Hash** | Even distribution when no natural range or list key exists |

**Operational considerations:**
- Partition pruning only fires when the partition key appears in the `WHERE` clause with a literal or bind parameter the planner can evaluate.
- Indexes must be created on each partition individually (PostgreSQL 10 declarative partitioning handles this automatically for primary keys).
- Cross-partition queries (e.g., `ORDER BY` spanning partitions) have higher overhead — plan schemas accordingly.

**Refs:**
- [Table partitioning (PostgreSQL 10 docs)](https://www.postgresql.org/docs/10/ddl-partitioning.html)
- [Scaling with partitioning — TimescaleDB blog](https://blog.timescale.com/blog/scaling-partitioning-data-postgresql-10-explained-cd48a712a9a1/)

---

## Data Archiving & WAL

### Write-Ahead Logging (WAL)

Every mutation is first written to the WAL before being applied to the data pages. This guarantees Durability and enables:
- **Point-in-time recovery (PITR)**
- **Streaming replication** (ship WAL segments to standby)
- **Logical replication** (decode WAL into row-level change events)

### Archiving Strategy

```
Active DB  →  WAL segments  →  Archive storage (S3 / GCS)
                          ↓
               Standby DB (streaming replication)
```

**Vacuum & space reclamation:**
Dead rows (from `UPDATE`/`DELETE`) are not immediately reclaimed — PostgreSQL uses MVCC and requires `VACUUM` to reclaim space. `AUTOVACUUM` handles this automatically but must be tuned for high-write tables.

```sql
-- Manual vacuum with analyze
VACUUM ANALYZE my_table;

-- Check bloat
SELECT schemaname, tablename, n_dead_tup, n_live_tup,
       round(n_dead_tup::numeric / nullif(n_live_tup,0) * 100, 1) AS dead_pct
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;
```

**Refs:**
- [WAL archiving with wal-g and S3 (Fusionbox)](https://www.fusionbox.com/blog/detail/postgresql-wal-archiving-with-wal-g-and-s3-complete-walkthrough/644/)
- [WAL retention and pg_archivecleanup (Percona)](https://www.percona.com/blog/2019/07/10/wal-retention-and-clean-up-pg_archivecleanup/)
- [Continuous archiving (PostgreSQL docs)](https://www.postgresql.org/docs/9.3/continuous-archiving.html)
- [Routine vacuuming (PostgreSQL docs)](https://www.postgresql.org/docs/11/routine-vacuuming.html)
- [Archiving RDS PostgreSQL to S3/Glacier (AWS blog)](https://aws.amazon.com/blogs/database/archiving-data-from-relational-databases-to-amazon-glacier-via-aws-dms/)
- [AWS DMS](https://aws.amazon.com/dms/)

---

## Connection Pooling

Every database connection is a long-lived OS thread/process plus a TCP socket. Opening a new connection per request costs **5–15ms** of latency and significant memory.

```
Application threads  →  Connection Pool (HikariCP / PgBouncer)  →  DB backend processes
```

**HikariCP pool sizing formula:**

```
pool_size = (cpu_core_count × 2) + effective_spindle_count

Example: 4-core server, 1 SSD spindle:
pool_size = (4 × 2) + 1 = 9 connections
```

**Key insight:** More connections ≠ better throughput. Beyond the saturation point, additional connections increase context switching and lock contention. PostgreSQL's practical limit is typically `max_connections = 100–200`; use PgBouncer in transaction-pooling mode to multiplex thousands of app connections onto a small backend pool.

**Transport options:**
- `TCP` — standard, works across hosts, supports TLS.
- `Unix domain socket` — lower latency for local connections (no TCP stack overhead).

**Refs:**
- [Database connection pooling (Stack Overflow)](http://stackoverflow.com/a/4041136/432907)
- [HikariCP pool sizing rationale](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [Unix socket vs TCP (Unix SE)](https://unix.stackexchange.com/a/32138/17781)

---

## Storage Internals — Database Pages

The database engine reads and writes data in fixed-size units called **pages** (PostgreSQL default: 8 KB; MySQL InnoDB: 16 KB). Understanding pages is essential for tuning I/O-bound workloads.

| Concept | Detail |
|---|---|
| **Page** | Fixed-size disk unit storing a set of rows (tuples). All reads/writes are page-granular. |
| **Heap** | Unordered collection of pages that make up a table. |
| **FSM** | Free Space Map — tracks available space per page for INSERT placement. |
| **Visibility Map** | Tracks which pages have no dead tuples (used to skip VACUUM work). |
| **Fill Factor** | Reserve space in pages for in-place UPDATEs (default 100; set 70–80 for hot-update tables). |

**Implications for schema design:**
- Wide rows (many columns or `TEXT`/`JSONB`) reduce rows-per-page, increasing I/O for table scans.
- `TOAST` (PostgreSQL) spills large values to a side table, keeping the main page dense.

**Refs:**
- [PostgreSQL page layout](https://www.postgresql.org/docs/8.0/storage-page-layout.html)
- [MySQL InnoDB page structure](https://dev.mysql.com/doc/internals/en/innodb-page-structure.html)

---

## Transaction Isolation & Locking

### Isolation Levels

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| `READ UNCOMMITTED` | ✅ possible | ✅ possible | ✅ possible |
| `READ COMMITTED` *(JDBC default)* | ❌ prevented | ✅ possible | ✅ possible |
| `REPEATABLE READ` | ❌ | ❌ | ✅ possible |
| `SERIALIZABLE` | ❌ | ❌ | ❌ |

**PostgreSQL nuance:** PostgreSQL's `REPEATABLE READ` actually prevents phantom reads as well (due to MVCC snapshot semantics), which is stronger than the SQL standard requires.

### Lock Types

| Lock | Acquired By | Blocks |
|---|---|---|
| `ACCESS SHARE` | `SELECT` | Only `ACCESS EXCLUSIVE` |
| `ROW EXCLUSIVE` | `INSERT / UPDATE / DELETE` | `SHARE` and above |
| `SHARE UPDATE EXCLUSIVE` | `VACUUM`, `ANALYZE`, `CREATE INDEX CONCURRENTLY` | Schema changes |
| `ACCESS EXCLUSIVE` | `ALTER TABLE`, `DROP TABLE`, `VACUUM FULL` | Everything |

**Deadlock avoidance:** Always acquire locks in a consistent order across transactions. Use `SELECT ... FOR UPDATE SKIP LOCKED` for queue-style workloads to avoid contention.

**Refs:**
- [Transaction locking (Methods & Tools)](http://www.methodsandtools.com/archive/archive.php?id=83)
- [Relational algebra — Aggregation (Wikipedia)](https://en.wikipedia.org/wiki/Relational_algebra#Aggregation)

---

## High Availability & Replication

### Replication Modes

| Mode | Data Loss Risk | Read Scalability | Use Case |
|---|---|---|---|
| **Synchronous streaming** | Zero (at the cost of write latency) | Yes | Financial systems, strong consistency |
| **Asynchronous streaming** | Small window (WAL lag) | Yes | Most production systems |
| **Logical replication** | Configurable | Partial | Cross-version migrations, selective table replication |

### HA Topology

```
              ┌─────────────────┐
              │   Primary (R/W) │
              └────────┬────────┘
          WAL streaming│
     ┌──────────────────┴──────────────────┐
     ▼                                     ▼
┌─────────┐                         ┌─────────┐
│Standby 1│  ← auto-failover via    │Standby 2│
│  (R/O)  │    Patroni / pgpool-II  │  (R/O)  │
└─────────┘                         └─────────┘
```

**Operational checklist:**
- Monitor replication lag (`pg_stat_replication.write_lag`).
- Set `synchronous_standby_names` for zero-RPO requirements.
- Test failover procedure regularly — not just in theory.

**Refs:**
- [High Availability (PostgreSQL docs)](https://www.postgresql.org/docs/9.3/high-availability.html)
- [Streaming Replication (PostgreSQL wiki)](https://wiki.postgresql.org/wiki/Streaming_Replication)

---

## Performance Engineering

### Query-level

- Run `EXPLAIN (ANALYZE, BUFFERS)` — look at actual vs. estimated rows; large divergence signals stale statistics. Run `ANALYZE` to refresh them.
- Avoid `SELECT *` in application code — fetches unnecessary columns and bloats network I/O.
- Use bind parameters (prepared statements) for plan caching and SQL injection prevention.

### System-level Knobs (PostgreSQL)

| Parameter | Purpose | Recommended Starting Point |
|---|---|---|
| `shared_buffers` | Buffer pool size | 25% of RAM |
| `work_mem` | Per-sort / per-hash memory | 4–16 MB (lower for high concurrency) |
| `effective_cache_size` | Planner hint for OS page cache | 50–75% of RAM |
| `random_page_cost` | Planner cost for random I/O | `1.1` for SSD; `4.0` for HDD |
| `max_connections` | Hard cap on connections | 100–200; use PgBouncer beyond that |

**Refs:**
- [100× faster Postgres by changing 1 line (Datadog)](https://www.datadoghq.com/blog/100x-faster-postgres-performance-by-changing-1-line/)

---

## Schema Design

See [`database_practices.md`](database_practices.md) for standards.

---

## Database Engines

| Engine | Type | Docs |
|---|---|---|
| PostgreSQL | RDBMS | [`db/pg/README.md`](db/pg/README.md) |
| MySQL | RDBMS | [`db/mysql/README.md`](db/mysql/README.md) |
| Oracle | RDBMS | [`db/oracle/README.md`](db/oracle/README.md) |
| CockroachDB | Distributed SQL | [`db/cdb/README.md`](db/cdb/README.md) |
| Redshift | Columnar / OLAP | [`db/pg/README.md`](db/pg/README.md) (Redshift section) |

---

## Managed Database Services

| Provider | Service | Notes |
|---|---|---|
| AWS | RDS / Aurora | Aurora offers up to 5× throughput vs. RDS PostgreSQL via custom storage layer |
| AWS | Redshift | Columnar MPP; optimized for OLAP, not OLTP |
| AWS | DMS | Schema/data migration across heterogeneous engines |
| Azure | Azure SQL | Elastic pools for multi-tenant cost sharing |
| Azure | Synapse Analytics | Unified analytics platform (formerly SQL Data Warehouse) |
| Azure | Time Series Insights | Purpose-built for IoT time-series data |

**Refs:**
- [Aurora vs. Oracle RAC (AWS blog)](https://aws.amazon.com/blogs/database/amazon-aurora-as-an-alternative-to-oracle-rac/)
- [Elastic pools (Azure docs)](https://docs.microsoft.com/en-us/azure/azure-sql/database/elastic-pool-overview)
- [Azure backup for PostgreSQL](https://docs.microsoft.com/en-us/azure/backup/backup-azure-database-postgresql)
