# PostgreSQL

Production-grade operational reference for PostgreSQL — local setup, CLI workflows, schema inspection, performance benchmarks, and data export patterns.

---

## Table of Contents

- [Local Setup](#local-setup)
- [Connecting](#connecting)
- [Database Inspection](#database-inspection)
- [Schema & Data Queries](#schema--data-queries)
- [Disk Usage](#disk-usage)
- [Benchmarks](#benchmarks)
  - [PostgreSQL (RDS db.m4.large)](#postgresql-rds-dbm4large)
  - [Redshift (dc2.large)](#redshift-dc2large)
- [Data Export](#data-export)
- [Embedded PostgreSQL (Testing)](#embedded-postgresql-testing)

---

## Local Setup

```bash
# Install psql CLI
brew install postgresql

# Start local PostgreSQL via Docker
docker-compose up pgdb
```

---

## Connecting

```bash
# Connect to a running Docker container
docker exec -it local_pgdb psql -p 5432 -U postgres

# Connect via TCP (local or remote)
psql -h 127.0.0.1 -p 5432 -U postgres

# Connect to RDS instance (prompts for password)
psql -h analytics.<account>.us-east-1.rds.amazonaws.com \
     -p 5432 -d postgres -U postgres -W
# SSL connection (TLSv1.2, ECDHE-RSA-AES256-GCM-SHA384, 256-bit)
```

---

## Database Inspection

```sql
-- List all databases
\list

-- List roles and privileges
\du

-- Show current database
SELECT current_database();

-- List tables in current database
\d

-- Switch database
\connect museum_visit;

-- Enable expanded display (vertical output for wide rows)
\x

-- Show timezone
SHOW TIMEZONE;

-- Show current timestamp
SELECT CURRENT_TIMESTAMP;
```

**Sample output — list of databases on RDS:**

```
                                   List of databases
     Name     |  Owner   | Encoding |   Collate   |    Ctype    |   Access privileges
--------------+----------+----------+-------------+-------------+------------------------
 museum_visit | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
 postgres     | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
 rdsadmin     | rdsadmin | UTF8     | en_US.UTF-8 | en_US.UTF-8 | rdsadmin=CTc/rdsadmin
```

---

## Schema & Data Queries

```sql
-- Row counts and time spread (useful for load testing verification)
SELECT count(*) users, max(created) - min(created) AS time_taken
FROM visiting_user;

-- Date/time functions
SELECT current_date, age('2020-08-18 10:10:10'::timestamp);
-- current_date  | age
-- 2020-08-21    | 2 days 13:49:50

-- Aggregate visits by hour of day
SELECT date_part('hour', visit_start_local) AS hr,
       count(*) AS visit_count
FROM museum_visit
WHERE visit_start_local >= '2020-08-01 00:00:00'
  AND visit_start_local <  '2020-08-02 00:00:00'
GROUP BY hr
ORDER BY visit_count DESC;
```

---

## Disk Usage

```sql
-- Top 20 largest relations (tables + indexes + sequences)
-- https://wiki.postgresql.org/wiki/Disk_Usage
SELECT nspname || '.' || relname AS relation,
       pg_size_pretty(pg_relation_size(c.oid)) AS size
FROM pg_class c
LEFT JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE nspname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_relation_size(c.oid) DESC
LIMIT 20;
```

---

## Benchmarks

### PostgreSQL (RDS db.m4.large)

**Specs:** PostgreSQL v11.4 · db.m4.large · 8 GB RAM · 2 vCPU · HikariCP pool size = 10

**100K concurrent user writes:**

| Users written | Wall time |
|---|---|
| 32,075 | 49m 41s |
| 38,788 | 1h 00m 00s |
| 50,601 | 1h 18m 24s |
| **100,000** | **22m 29s** (with parallelism) |

> With parallelism enabled (7 active connections out of pool of 10), total time for 100K inserts dropped to **22m 29s** (1,350ms end-to-end wall time reported by the application).

**Key log marker:**
```
HikariPool-1 - Pool stats (total=10, active=7, idle=3, waiting=0)
```

---

### Redshift (dc2.large)

**Specs:** Redshift dc2.large · Columnar storage · OLAP-optimised

**100K user writes (same workload as above):**

| Users written | Wall time |
|---|---|
| 15,666 | 25m 13s |
| 44,130 | 2h 44m 54s |
| **100,000** | **~22m 40s** total application time |

> **Takeaway:** Redshift is optimised for analytical read workloads (columnar storage, MPP). OLTP-style high-frequency single-row writes are significantly slower than PostgreSQL at equivalent instance size. Use Redshift for analytics, not as a transactional store.

---

## Data Export

```sql
-- Export query result to CSV (requires superuser or pg_write_server_files)
-- Increase statement_timeout if exporting large datasets
COPY (
  SELECT *
  FROM my_table
  WHERE tb_time_local > '2020-06-01'
    AND tb_time_local < '2500-06-01'
)
TO '/tmp/export.csv'
DELIMITER ',' CSV HEADER;
```

> For RDS/Aurora, use `aws_s3.query_export_to_s3()` instead — direct filesystem access is not available on managed instances.

---

## Embedded PostgreSQL (Testing)

Used for integration tests via `io.zonky.test:embedded-postgres`. The embedded binary is extracted to a temp directory at test startup.

```bash
# Binary location (darwin/amd64)
file = "file:/.../.m2/repository/io/zonky/test/postgres/embedded-postgres-binaries-darwin-amd64/10.15.0-1/...jar!/postgres-darwin-x86_64.txz"

# pg_ctl start command used by the test harness
/tmp/embedded-pg/PG-<hash>/bin/pg_ctl \
  -D /tmp/pg_unit_tests/data -l logfile start

# initdb invocation
/tmp/embedded-pg/PG-<hash>/bin/initdb \
  -A trust -U postgres -D /tmp/pg_unit_tests/data -E UTF-8
```
