CREATE TABLE visiting_user(
    user_id INT IDENTITY(1, 1) PRIMARY KEY,
    user_name VARCHAR(255),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create TABLE museum_visit (
    user_id INT REFERENCES visiting_user(user_id),
    visit_id int IDENTITY(1, 1) PRIMARY KEY,
    museum_name VARCHAR(255),
    department VARCHAR(255),
    visit_start TIMESTAMP,
    visit_end TIMESTAMP,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
