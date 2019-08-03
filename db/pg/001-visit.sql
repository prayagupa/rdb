create table visiting_user(
    user_id serial PRIMARY KEY,
    user_name VARCHAR(255),
    created timestamp DEFAULT CURRENT_TIMESTAMP
);

create TABLE museum_visit (
    user_id INT references visiting_user(user_id),
    visit_id serial PRIMARY KEY,
    museum_name VARCHAR(255),
    department VARCHAR(255),
    visit_start timestamp,
    visit_end timestamp,
    created timestamp DEFAULT CURRENT_TIMESTAMP
);
