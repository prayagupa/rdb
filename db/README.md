# Database SQL & Benchmarks

SQL schemas, seed data, migration scripts, and performance benchmarks for all supported database engines.

---

## Structure

```
db/
├── pg/          # PostgreSQL — schema, queries, benchmarks
├── mysql/       # MySQL — schema, connection benchmarks
├── oracle/      # Oracle 12c — schema, Docker setup
├── cdb/         # CockroachDB — distributed SQL
├── h2/          # H2 — in-memory database for testing
└── redshift/    # Redshift — columnar OLAP benchmarks
```

---

## Usage

Each subdirectory contains:
- **Schema SQL** — authoritative table definitions to recreate the database from scratch locally.
- **Seed data** — representative data for development and testing.
- **Migration scripts** — numbered in execution order (e.g., `001-visit.sql`, `002-visit_data.sql`).
- **README** — engine-specific setup and operational notes.

See the [root README](../README.md) for cross-engine theory (ACID, indexing, partitioning, HA).
