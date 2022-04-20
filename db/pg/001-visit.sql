CREATE TABLE IF NOT EXISTS visiting_user(
    user_id serial PRIMARY KEY,
    user_name VARCHAR(255),
    created timestamptz DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS museum_visit (
    user_id INT REFERENCES visiting_user(user_id),
    visit_id serial PRIMARY KEY,
    museum_name VARCHAR(255),
    department VARCHAR(255),
    visit_start_tz TIMESTAMPTZ,
    visit_start_local TIMESTAMP,
    visit_end_tz TIMESTAMPTZ,
    visit_end_local TIMESTAMP,
    created timestamptz DEFAULT CURRENT_TIMESTAMP,
    visit_history JSON
);

-- https://www.postgresql.org/docs/9.4/datatype-json.html
-- CREATE INDEX visit_history_key_idx
-- ON museum_visit (visit_history->>history_hash)
