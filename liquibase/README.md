# Liquibase — Database Change Management

Reference for using Liquibase to manage schema migrations across environments (dev → integration → production).

---

## Table of Contents

- [Overview](#overview)
- [Setup](#setup)
- [Core Commands](#core-commands)
  - [update](#update)
  - [rollback](#rollback)
  - [diff](#diff)
- [Multi-Environment Promotion](#multi-environment-promotion)
- [Changelog Format](#changelog-format)
- [Verification](#verification)
- [Refs](#refs)

---

## Overview

Liquibase tracks every schema change as an ordered, versioned **changeset**. Each changeset is identified by `id`, `author`, and `filename`. Once applied, it is recorded in the `DATABASECHANGELOG` table — Liquibase will never re-run it.

**Why this matters at scale:**
- Schema changes are **auditable** — every change has an author, timestamp, and checksum.
- Changes are **environment-aware** — promote the same changelog from dev → staging → prod with confidence.
- **Rollback** is first-class — Liquibase generates or executes rollback SQL automatically.

Working directory: `liquibase/visits/`

---

## Setup

```bash
cd liquibase/visits

# H2 web console (for local dev)
./start-h2
# UI available at http://localhost:8080/frame.jsp
```

Configuration files:

| File | Purpose |
|---|---|
| `liquibase.properties` | Default JDBC URL, credentials, changelog path |
| `liquibase.psql.conf` | PostgreSQL-specific overrides |
| `liquibase.sqlcmd.conf` | SQL Server overrides |
| `liquibase.sqlplus.conf` | Oracle SQL*Plus overrides |
| `liquibase.flowfile.yaml` | Declarative flow for multi-step operations |

---

## Core Commands

### update

Applies all pending changesets from the changelog to the target database.

```bash
liquibase update
```

**Sample output:**
```
Running Changeset: retail-changelog.sql::1::prayag.upa
Running Changeset: retail-changelog.sql::2::prayag.upa
Running Changeset: retail-changelog.sql::3::prayag.upa
Liquibase command 'update' was executed successfully.
```

### rollback

Reverts the last N changesets. Requires rollback SQL in the changeset definition or Liquibase-generated DDL.

```bash
# Roll back the last 2 changesets
liquibase rollbackCount 2
```

- [Rollback docs](https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html)

### diff

Compares two databases and reports schema differences — missing tables, columns, indexes, constraints.

```bash
liquibase diff
```

**Sample diff output (integration vs dev):**

```
Reference Database: DBUSER @ jdbc:h2:tcp://localhost:9090/mem:integration
Comparison Database: DBUSER @ jdbc:h2:tcp://localhost:9090/mem:dev

Unexpected Columns:
  PUBLIC.COMPANY.ADDRESS1, PUBLIC.COMPANY.ADDRESS2
  PUBLIC.PERSON.ADDRESS1,  PUBLIC.PERSON.ADDRESS2, PUBLIC.PERSON.COUNTRY, PUBLIC.PERSON.STATE

Unexpected Tables:
  COMPANY, DATABASECHANGELOG, DATABASECHANGELOGLOCK, PERSON

Unexpected Primary Keys:
  CONSTRAINT_6 on PUBLIC.COMPANY(ID)
  CONSTRAINT_8 on PUBLIC.PERSON(ID)
```

Use `diff` to audit what has been applied to one environment but not yet promoted to another.

---

## Multi-Environment Promotion

Liquibase uses the same changelog across all environments. Override the JDBC URL via flag or config file.

```bash
# Apply to integration database
liquibase --url=jdbc:h2:tcp://localhost:9090/mem:integration update

# Apply to production (example)
liquibase \
  --url=jdbc:postgresql://prod-host:5432/mydb \
  --username=dbuser \
  --password=$DB_PASS \
  update
```

**Promotion flow:**

```
dev  →  [liquibase update]  →  integration  →  [liquibase update]  →  production
         (H2 local)                              (PostgreSQL RDS)
```

---

## Changelog Format

Changesets can be written in SQL, XML, YAML, or JSON. This repo uses SQL with Liquibase-formatted comments.

```sql
-- liquibase formatted sql

-- changeset prayag.upa:1
CREATE TABLE company (
    id    INT          NOT NULL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL
);

-- rollback DROP TABLE company;

-- changeset prayag.upa:2
ALTER TABLE company ADD COLUMN address1 VARCHAR(255);

-- rollback ALTER TABLE company DROP COLUMN address1;
```

- [SQL migration guide (Liquibase docs)](https://docs.liquibase.com/workflows/liquibase-community/migrate-with-sql.html)

---

## Verification

After `liquibase update`, verify applied changesets in the audit table:

```sql
SELECT id, author, filename, dateexecuted, exectype, md5sum
FROM databasechangelog
ORDER BY orderexecuted;
```

---

## Refs

- [Liquibase getting started](https://www.liquibase.org/get-started/running-your-first-update)
- [Developer workflow](https://www.liquibase.org/get-started/developer-workflow)
- [Rollback workflows](https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html)
- [SQL migration format](https://docs.liquibase.com/workflows/liquibase-community/migrate-with-sql.html)
