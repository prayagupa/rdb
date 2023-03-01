CREATE DATABASE museumdb;

-- https://www.postgresql.org/docs/8.0/sql-createuser.html
CREATE ROLE mu WITH
    LOGIN
    SUPERUSER
    INHERIT
    CREATEDB
    CREATEROLE
    REPLICATION;
ALTER USER mu WITH PASSWORD 'mu';
COMMENT ON ROLE mu IS 'Museum User';

-- https://stackoverflow.com/a/22486012/432903
GRANT CONNECT ON DATABASE museumdb TO mu;

-- login with 
-- psql -h 127.0.0.1 -p 5432 -U mu postgres

CREATE SCHEMA museumdb;
GRANT USAGE ON SCHEMA museumdb TO mu;

