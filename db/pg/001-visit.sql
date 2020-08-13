create table visiting_user(
    user_id serial PRIMARY KEY,
    user_name VARCHAR(255),
    created timestamptz DEFAULT CURRENT_TIMESTAMP
);

create TABLE museum_visit (
    user_id INT references visiting_user(user_id),
    visit_id serial PRIMARY KEY,
    museum_name VARCHAR(255),
    department VARCHAR(255),
    visit_start_tz timestamptz,
    visit_start_local timestamp,
    visit_end_tz timestamptz,
    visit_end_local timestamp,
    created timestamptz DEFAULT CURRENT_TIMESTAMP,
    visit_history JSON
);

-- https://www.postgresql.org/docs/9.4/datatype-json.html
-- CREATE INDEX visit_history_key_idx
-- ON museum_visit (visit_history->>history_hash)
