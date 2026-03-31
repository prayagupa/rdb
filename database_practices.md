# Relational Database Best Practices

A living engineering standard for schema design, SQL authoring, migrations, performance, and security. Treat every item here as a default — deviate only with explicit justification.

---

## Table of Contents

- [Naming Conventions](#naming-conventions)
- [Schema Design](#schema-design)
- [SQL Authoring](#sql-authoring)
- [Migrations & Version Control](#migrations--version-control)
- [Indexing](#indexing)
- [Performance](#performance)
- [Security](#security)
- [Refs](#refs)

---

## Naming Conventions

- **Tables** — singular nouns, `snake_case`. The table represents a type, not a collection.
  ```
  ✅  ad_campaign, ad_placement, user_profile, product_review
  ❌  ad_campaigns, AdCampaign, tbl_users
  ```

- **Columns** — `snake_case`, descriptive, no type suffixes.
  ```
  ✅  created_at, geo_location_lat, loyalty_point
  ❌  strName, dtCreatedDate, n_id
  ```

- **Timestamps** — suffix with `_at` (preferred) or `_datetime_utc` / `_datetime_local` when storing both timezones. The `_on` suffix is also acceptable for date-only fields.
  ```
  created_at, updated_at, deleted_at
  created_datetime, modified_datetime, deleted_datetime
  event_datetime_utc, event_datetime_local
  ```

- **Booleans** — prefix with a verb that forms a yes/no question: `is_`, `has_`, `can_`, `should_`.
  ```
  is_deleted, has_verified_email, can_edit, should_track_user
  creative_id, is_deleted
  region_id, should_track_user
  ```

- **Foreign keys** — name as `<referenced_table>_id`.
  ```
  user_id, ad_campaign_id, order_id
  ```

- **Primary keys** — use `id` (not `user_id` on the `user` table itself — that creates ambiguity in JOINs).

- **Indexes** — `idx_<table>_<columns>`. Unique constraints: `uq_<table>_<columns>`.
  ```
  idx_ad_placement_campaign_id
  uq_user_profile_email
  ```

- **Junction/association tables** — name after both entities in alphabetical order.
  ```
  campaign_tag  (not tag_campaign, not campaign_tags_map)
  ```

---

## Schema Design

### Normalisation

- Start at **3NF** (Third Normal Form) by default. Each non-key column must depend on the whole key and nothing but the key.
- Denormalise only for measured read performance gains, and document the trade-off explicitly.

### Primary Keys

- Prefer **`BIGSERIAL`** (PostgreSQL) / **`BIGINT AUTO_INCREMENT`** (MySQL) for single-node tables.
- Use **`UUID`** (`gen_random_uuid()`) for distributed systems or when rows are exposed in URLs (prevents enumeration attacks).
- Never use a mutable business value (email, phone, SSN) as a primary key.

### Timestamps

- Always store timestamps in **UTC**. Convert to local time in the application layer.
- Every table must have `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`.
- Mutable tables must have `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` — keep it current via a trigger or ORM hook.
- Implement **soft deletes** with `deleted_at TIMESTAMPTZ` rather than physical deletes for any business-critical entity.

```sql
-- Standard audit columns on every table
id          BIGSERIAL    PRIMARY KEY,
created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
deleted_at  TIMESTAMPTZ  -- NULL means active
```

### Constraints — enforce at the database layer, not just application code

- `NOT NULL` on every column that must have a value.
- `UNIQUE` constraints for natural uniqueness (email, slug, external_id).
- `FOREIGN KEY` constraints with explicit `ON DELETE` behaviour (`RESTRICT` is the safe default; never `CASCADE` without deliberate design).
- `CHECK` constraints for bounded domains (e.g., `CHECK (status IN ('active', 'paused', 'archived'))`).

### Nullability

- Be deliberate: a `NULL` should mean "genuinely unknown", not "not applicable" or "we forgot".
- Prefer `NOT NULL` with a sentinel default over nullable columns where the domain allows it.

### Anti-patterns

| Anti-pattern | Why it's harmful | Alternative |
|---|---|---|
| Comma-separated values in a column | Violates 1NF; can't index, filter, or JOIN efficiently | Junction table |
| EAV (Entity-Attribute-Value) | No type safety, no constraints, catastrophic query performance | Proper columns or JSONB with a schema |
| Storing JSON blobs for queryable data | Can't index individual fields efficiently | Structured columns; use `JSONB` only for genuinely schemaless data |
| `SELECT *` in application queries | Fetches unused columns, breaks if schema changes, bloats network I/O | Explicit column list |
| Nullable foreign keys to model optionality | Leaks optional relationships into the core table | Separate association table |
| `INT` primary keys on distributed tables | Sequence contention, enumerable IDs | `UUID` or distributed sequence |

---

## SQL Authoring

### Use `RETURNING` to eliminate round-trips

After a write or update, use `RETURNING` to fetch the resulting row state in the same statement. This eliminates a follow-up `SELECT` and avoids a race condition on the returned data.

```sql
-- Insert and immediately get the auto-generated id and defaults
INSERT INTO ad_targeting (ad_id, geo_location_lat, geo_location_long)
VALUES (7, 47.6062, -122.3321)
RETURNING id, created_at, geo_location_lat, geo_location_long;

-- Update and confirm new state
UPDATE ad_campaign
SET status = 'paused', updated_at = now()
WHERE id = 42
RETURNING id, status, updated_at;
```

### Always use bind parameters (never string interpolation)

```java
// ✅ Correct — parameterised query
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM user_profile WHERE email = ?"
);
ps.setString(1, email);

// ❌ Wrong — SQL injection vector
String query = "SELECT * FROM user_profile WHERE email = '" + email + "'";
```

### Explicit column lists on INSERT

```sql
-- ✅ Schema changes won't silently break this
INSERT INTO ad_campaign (name, status, created_at) VALUES (?, ?, now());

-- ❌ Breaks if column order or count changes
INSERT INTO ad_campaign VALUES (?, ?, now());
```

### Prefer `EXISTS` over `COUNT` for existence checks

```sql
-- ✅ Short-circuits on first match — O(1) best case
SELECT EXISTS (SELECT 1 FROM user_profile WHERE email = ?);

-- ❌ Scans all matching rows to count them
SELECT COUNT(*) FROM user_profile WHERE email = ?;
```

### Paginate large result sets

```sql
-- Keyset pagination (preferred — stable, index-friendly)
SELECT id, name, created_at
FROM ad_campaign
WHERE id > :last_seen_id
ORDER BY id
LIMIT 50;

-- Offset pagination (avoid beyond page ~100 — full scan cost grows linearly)
SELECT id, name FROM ad_campaign ORDER BY id LIMIT 50 OFFSET 5000;
```

### Use transactions explicitly for multi-statement writes

```sql
BEGIN;
  INSERT INTO customer_order (customer_id, total) VALUES (?, ?) RETURNING id INTO :order_id;
  INSERT INTO order_line_item (order_id, sku, qty) VALUES (:order_id, ?, ?);
COMMIT;
```

---

## Migrations & Version Control

- **Always version migrations** — every schema change must have a numbered, ordered migration script.
- Store the **canonical schema** in `db/<engine>/` so any engineer can recreate the database locally from scratch with a single command.
- Write migration scripts in `db/<engine>/` using sequential numbering: `001-create-user.sql`, `002-add-email-index.sql`.
- Use **Liquibase** (see [`liquibase/README.md`](liquibase/README.md)) for tracked, audited, multi-environment deployments.
- **Never edit an already-applied migration.** Create a new one instead. Liquibase detects checksum changes and will fail the deployment.
- Every `ALTER TABLE` that adds a column must include a `DEFAULT` or the column must be `NOT NULL` with backfill handled in a separate migration step.
- For large tables, use `CREATE INDEX CONCURRENTLY` (PostgreSQL) to avoid locking reads during index creation.

```sql
-- ✅ Non-blocking index creation
CREATE INDEX CONCURRENTLY idx_ad_placement_campaign_id ON ad_placement (campaign_id);

-- ❌ Locks the entire table during creation
CREATE INDEX idx_ad_placement_campaign_id ON ad_placement (campaign_id);
```

---

## Indexing

- Index all **foreign key columns** — missing FK indexes cause full-table scans on every JOIN and CASCADE operation.
- Index every column used in a `WHERE`, `ORDER BY`, or `JOIN ON` clause that appears in high-frequency queries.
- Use **partial indexes** to index only the rows that queries actually touch:
  ```sql
  CREATE INDEX idx_ad_campaign_active ON ad_campaign (created_at)
  WHERE is_deleted = false;
  ```
- Use **composite indexes** and order columns from highest to lowest cardinality, matching the query predicate order.
- Avoid over-indexing: each index adds write overhead on `INSERT`/`UPDATE`/`DELETE`. Audit unused indexes with:
  ```sql
  SELECT schemaname, tablename, indexname, idx_scan
  FROM pg_stat_user_indexes
  WHERE idx_scan = 0
  ORDER BY schemaname, tablename;
  ```
- Run `EXPLAIN (ANALYZE, BUFFERS)` before merging any query that touches a table with more than 10K rows.

---

## Performance

- **Measure before optimising.** Use `EXPLAIN (ANALYZE, BUFFERS)` to understand actual execution plans. Never guess.
- Keep transactions **short**. Long-running transactions hold locks, block VACUUM, and cause replication lag.
- Avoid `N+1` queries — fetch related data in a single JOIN or a single `IN (...)` query rather than one query per row.
- Use **connection pooling** (HikariCP, PgBouncer). Never open a new connection per request.
  - Pool size formula: `(cpu_cores × 2) + effective_spindle_count`
- For batch inserts, use a single multi-row `INSERT` or `COPY` rather than individual row inserts:
  ```sql
  -- ✅ Single round-trip
  INSERT INTO visiting_user (name, email) VALUES
    ('alice', 'alice@example.com'),
    ('bob',   'bob@example.com'),
    ('carol', 'carol@example.com');
  ```
- For read-heavy workloads, offload analytics queries to a read replica or a columnar store (Redshift, BigQuery). Never run long OLAP queries against the OLTP primary.
- Set **statement timeouts** in your application connection to prevent runaway queries from starving the connection pool:
  ```sql
  SET statement_timeout = '30s';
  ```

---

## Security

- **Never interpolate user input into SQL strings.** Always use parameterised queries / prepared statements.
- Grant the **minimum required privileges** to each application role:
  ```sql
  -- Application service account: read/write only, no DDL
  GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
  REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
  ```
- Use **separate roles** for migrations (DDL permissions) and application runtime (DML only).
- Do not store plaintext secrets (passwords, API keys) in any column. Store hashed credentials (`bcrypt`, `argon2`) and encrypt sensitive PII at the application layer before writing.
- Rotate database credentials via a secrets manager (AWS Secrets Manager, Vault). Never hardcode credentials in code or config files.
- Enable **SSL/TLS** for all database connections in production. Reject plaintext connections at the server level (`ssl = on`, `ssl_min_protocol_version = TLSv1.2`).
- Audit privileged operations with `pg_audit` (PostgreSQL) or equivalent.

---

## Refs

- [Database naming conventions (Stack Overflow)](https://stackoverflow.com/questions/7662/database-table-and-column-naming-conventions)
- [SQL best practices — performance (Microsoft Dynamics AX)](https://docs.microsoft.com/en-us/dynamicsax-2012/developer/best-practice-performance-optimizations-database-design-and-operations)
- [Preventing SQL injection in Java (Sqreen)](https://blog.sqreen.com/preventing-sql-injections-in-java-and-other-vulnerabilities/)
- [Use The Index, Luke — SQL indexing guide](https://use-the-index-luke.com/)
- [HikariCP pool sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [PostgreSQL lock types](https://www.postgresql.org/docs/current/explicit-locking.html)
- [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
