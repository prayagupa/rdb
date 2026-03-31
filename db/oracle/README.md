# Oracle Database

Operational reference for Oracle 12c — infrastructure setup, Docker-based local environment, SQL*Plus access, listener configuration, and schema bootstrap.

---

## Table of Contents

- [Overview](#overview)
- [AWS RDS — Connecting to Oracle](#aws-rds--connecting-to-oracle)
- [Local Docker Setup](#local-docker-setup)
  - [Build the Docker Image](#build-the-docker-image)
  - [Run the Container](#run-the-container)
  - [Container Internals](#container-internals)
- [SQL*Plus — Connecting & Inspecting](#sqlplus--connecting--inspecting)
- [Listener Configuration](#listener-configuration)
- [Schema Bootstrap](#schema-bootstrap)
- [Testing with H2 Oracle Compatibility Mode](#testing-with-h2-oracle-compatibility-mode)

---

## Overview

This repo uses **Oracle 12.2.0.1 Enterprise Edition** for local development and integration testing.

- Oracle docs: [Oracle 12c Database Concepts](https://docs.oracle.com/database/121/CNCPT/intro.htm#CNCPT001)
- Oracle RAC: [docs.oracle.com/database/technologies/rac](https://www.oracle.com/database/technologies/rac.html)
- Aurora as Oracle RAC alternative: [AWS blog](https://aws.amazon.com/blogs/database/amazon-aurora-as-an-alternative-to-oracle-rac/)

**Observed latency:** ~200ms on VPN-connected RDS. `WHERE ROWNUM <= 1` counter-intuitively increases latency in some cases due to plan changes.

---

## AWS RDS — Connecting to Oracle

```bash
# Describe RDS instances in a region
aws rds describe-db-instances --profile aws-default --region us-west-2

# Connect to RDS Oracle via MySQL-compatible endpoint
mysql -h duwamish.<account>.us-west-2.rds.amazonaws.com -P 3306 -u root -p
```

**Connectivity checklist for RDS:**
1. Security group inbound rules must allow your IP/CIDR on the DB port.
2. The VPC must have an Internet Gateway attached if connecting from outside the VPC — [VPC Internet Gateway docs](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Internet_Gateway.html).
3. The subnet must be added to the VPC Route Table — [Route Tables docs](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html).
4. Verify VPN is not intercepting or re-routing DB traffic.

---

## Local Docker Setup

### Build the Docker Image

```bash
# Prepare the data directory
sudo mkdir -p /data/oracle
sudo chmod 777 -R /data
# Share /data with Docker (add via Docker Desktop → Preferences → Resources → File Sharing)

# Clone Oracle's official Docker images repo
git clone https://github.com/oracle/docker-images.git

# Verify the 12.2.0.1 EE zip is present
ls OracleDatabase/SingleInstance/dockerfiles/12.2.0.1/
# linuxx64_12201_database.zip  (~3.4 GB)

# Build the image (~6 GB, takes 15-30 minutes)
cd OracleDatabase/SingleInstance/dockerfiles
./buildDockerImage.sh -v 12.2.0.1 -e

# Verify
docker images | grep oracle
# oracle/database   12.2.0.1-ee   bcb7c9f64985   6.11GB
```

### Run the Container

```bash
docker run --name oracle \
  -p 1521:1521 \
  -p 5500:5500 \
  -e ORACLE_SID=xe \
  -e ORACLE_PDB=duwamishpdb \
  -e ORACLE_PWD=Duwamish9 \
  -e ORACLE_CHARACTERSET=AL32UTF8 \
  -v /data/oracle:/opt/oracle/oradata \
  oracle/database:12.2.0.1-ee
```

### Container Internals

```bash
# Shell into the container
docker exec -it oracle bash

# Verify processes
ps aux | grep oracle

# Oracle home
echo $ORACLE_HOME
# /opt/oracle/product/12.2.0.1/dbhome_1/

# Entrypoint scripts location
ls -l /opt/oracle/scripts/   # symlinked from /docker-entrypoint-initdb.d
```

---

## SQL*Plus — Connecting & Inspecting

```bash
# Connect as SYSDBA to the CDB root
sqlplus sys/Duwamish9@//localhost:1521/ORCLCDB as sysdba

# Check Oracle version
SELECT * FROM v$version;
-- CORE 12.1.0.2.0 Production

# List all tables visible to DBA
SELECT owner, table_name FROM dba_tables;

# List user profiles
SELECT * FROM dba_profiles;
```

---

## Listener Configuration

Managed via `lsnrctl`. The listener proxies all incoming connections to the appropriate Oracle service/instance.

```bash
# Inside the container
lsnrctl status
lsnrctl services
```

**`listener.ora` (auto-generated):**

```
LISTENER =
(DESCRIPTION_LIST =
  (DESCRIPTION =
    (ADDRESS = (PROTOCOL = IPC)(KEY = EXTPROC1))
    (ADDRESS = (PROTOCOL = TCP)(HOST = 0.0.0.0)(PORT = 1521))
  )
)
DEDICATED_THROUGH_BROKER_LISTENER = ON
DIAG_ADR_ENABLED = off
```

**`tnsnames.ora` (auto-generated):**

```
ORCLCDB = localhost:1521/ORCLCDB

DUWAMISHPDB =
(DESCRIPTION =
  (ADDRESS = (PROTOCOL = TCP)(HOST = 0.0.0.0)(PORT = 1521))
  (CONNECT_DATA =
    (SERVER = DEDICATED)
    (SERVICE_NAME = DUWAMISHPDB)
  )
)
```

Services registered at startup:

| Service | Instance | Status |
|---|---|---|
| `ORCLCDB` | ORCLCDB | READY |
| `ORCLCDBXDB` | ORCLCDB | READY (HTTP/HTTPS) |
| `duwamish` | ORCLCDB | READY |

---

## Schema Bootstrap

DDL runs automatically from `/docker-entrypoint-initdb.d` (symlinked to `/opt/oracle/scripts`) on first container start.

```sql
-- Create application tables
CREATE TABLE customer (
    id             NUMBER(10)   NOT NULL,
    name           VARCHAR(130),
    address        VARCHAR(130),
    loyalty_point  NUMBER(10),
    username       VARCHAR(130)
);

CREATE TABLE inventory (
    id         NUMBER(10) NOT NULL,
    warehouse  VARCHAR(20),
    sku        VARCHAR(20),
    qty        NUMBER(10)
);
ALTER TABLE inventory ADD (CONSTRAINT inv_pk PRIMARY KEY (id));
CREATE SEQUENCE inv_pk START WITH 1;

CREATE TABLE customer_order (
    id       NUMBER,
    name     VARCHAR(20),
    active   VARCHAR(2),
    created  TIMESTAMP
);

-- Seed data
INSERT INTO customer_order VALUES (1, 'steve jobs', '01', CURRENT_TIMESTAMP);
```

---

## Testing with H2 Oracle Compatibility Mode

For unit/integration tests that must run without a full Oracle instance, H2 supports an Oracle compatibility mode.

- Docs: [H2 Compatibility Modes](http://www.h2database.com/html/features.html)
- JDBC URL: `jdbc:h2:mem:test;MODE=Oracle`
- See `src/test/scala/H2OracleSpec.scala` for usage examples.

> **Caveat:** H2 Oracle mode does not support all Oracle-specific syntax (e.g., `CONNECT BY`, `ROWNUM` semantics, Oracle-specific types). Use it for fast smoke tests only; run the full suite against a real Oracle instance in CI.

**Refs:**
- [Oracle background processes](https://docs.oracle.com/cd/E18283_01/server.112/e17110/bgprocesses.htm)
