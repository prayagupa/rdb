# MySQL

Operational reference for MySQL — local Docker setup, CLI access, and connection strategy benchmarks.

---

## Table of Contents

- [Local Setup](#local-setup)
- [Connecting](#connecting)
- [Running Migrations](#running-migrations)
- [Benchmarks — Connection Strategies](#benchmarks--connection-strategies)

---

## Local Setup

```bash
# Start MySQL container (see docker-compose.yml)
docker-compose up mysqldb
```

Docker image: [hub.docker.com/_/mysql](https://hub.docker.com/_/mysql/)

---

## Connecting

```bash
# Connect via TCP from host machine
mysql -h 127.0.0.1 -u root -p   # password: r00t

# Connect via exec into the running container
docker exec -it <container_id> mysql -u root -p
```

---

## Running Migrations

```bash
# Apply schema/seed SQL from within the container
# (after connecting via exec above)
source /path/to/db/mysql/1.sql
```

---

## Benchmarks — Connection Strategies

These benchmarks compare the cost of **shared connection pooling** versus **opening a new connection per request**, isolating the connection overhead from query execution time.

### Shared Connection Pool

One connection reused across all requests:

```
total:   157ms
average:   1ms per request
```

### Per-Request Connection

A new TCP connection is opened for every request. Includes OS-level socket setup, authentication handshake, and session initialisation overhead.

```
total:   259ms
average:   2ms per request
```

> **Takeaway:** Per-request connections add ~65% overhead in this test. At scale, connection creation latency accumulates and exhausts the server's `max_connections` limit. Always use a connection pool (e.g., HikariCP, c3p0, DBCP) in production. See the [Connection Pooling](../../README.md#connection-pooling) section for sizing guidance.
