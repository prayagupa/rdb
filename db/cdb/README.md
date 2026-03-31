# CockroachDB

Operational reference for CockroachDB — a cloud-native distributed SQL database built on the Raft consensus protocol. Compatible with PostgreSQL wire protocol.

---

## Table of Contents

- [Overview](#overview)
- [Local Setup](#local-setup)
- [Connecting](#connecting)
- [Key Differences from PostgreSQL](#key-differences-from-postgresql)

---

## Overview

CockroachDB provides:
- **Horizontal scalability** — add nodes to scale reads and writes linearly.
- **Serializable isolation** by default (strongest SQL isolation level).
- **Multi-region active-active** — survive full datacenter failures with zero RPO.
- **PostgreSQL-compatible wire protocol** — most PostgreSQL drivers and ORMs work out of the box.

Use CockroachDB when you need geo-distributed writes or need to survive node/zone failures without manual failover.

---

## Local Setup

The local CockroachDB instance runs via Docker Compose in insecure mode (suitable for development only).

```bash
# Start the CockroachDB container
docker-compose up local_cdb
```

See `docker-compose.yml` for port and volume configuration.

---

## Connecting

```bash
# Shell into the running container
docker exec -it rdb-local_cdb-1 /bin/sh

# Open the CockroachDB SQL shell (insecure mode)
cockroach sql --insecure --host=localhost:26257
```

> **Note:** Insecure mode disables TLS and authentication. Never use `--insecure` in production. Use certificate-based auth with `cockroach cert` commands.

---

## Key Differences from PostgreSQL

| Behaviour | PostgreSQL | CockroachDB |
|---|---|---|
| Default isolation | `READ COMMITTED` | `SERIALIZABLE` |
| Sequences | Strictly monotonic | May have gaps under contention (use `UUID` or `gen_random_uuid()` for distributed PKs) |
| `SERIAL` / `BIGSERIAL` | Standard auto-increment | Uses distributed sequences — gaps are expected |
| Schema changes | Blocking `ALTER TABLE` | Online schema changes (non-blocking) |
| `RETURNING` | Supported | Supported |
| Transactions | Single-node optimised | Distributed — higher latency for cross-range transactions |
| `EXPLAIN` | `EXPLAIN ANALYZE` | `EXPLAIN ANALYZE (DISTSQL)` shows distributed execution plan |

**Refs:**
- [CockroachDB docs](https://www.cockroachlabs.com/docs/)
- [CockroachDB vs PostgreSQL](https://www.cockroachlabs.com/docs/stable/postgresql-compatibility.html)
