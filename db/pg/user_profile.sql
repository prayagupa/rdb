-- resource: https://medium.com/@syfe.tech/harnessing-pg-vector-for-building-a-recommendation-system-in-postgres-2cde7891f938
CREATE EXTENSION vector; -- Enable the extension in our postgres db

CREATE TABLE user_profiles (
    user_id SERIAL PRIMARY KEY,
    name varchar(50),
    age INT,
    gender VARCHAR(10),
    income BIGINT,
    profile_vector vector(3) -- Will contain the normalized vector for the user
);

--The profile_vector will contain the following:
--1. normalized age.
--2. normalized gender.
--3. normalized income range.

-- Formula: normalized_score = (x - min) / (max - min)
-- Example: Assuming our age range is from 18 to 100 years,
-- the normalized age for a 25-year-old would be calculated as
-- (25 - 18) / (100 - 18) = 0.1

-- Gender, being a categorical attribute, is encoded into a numeric format.
-- We use binary encoding for simplicity.

-- With an assumed income range of $0 to $250,000, an income of $30,000 is
-- normalized as (30000 - 0) / (250000 - 0) = 0.12.

INSERT INTO user_profiles (name, age, gender, income, profile_vector)
VALUES
("Upadhyay", 25, 'Male', 30000, ARRAY[0.1, 0, 0.12]),
("Prayag", 30, 'Female', 60000, ARRAY[0.2, 1, 0.24]),
("Amygdala", 45, 'Male', 120000, ARRAY[0.45, 0, 0.48]),
("Neo", 55, 'Female', 200000, ARRAY[0.62, 1, 0.8]),
("Michael", 35, 'Male', 75000, ARRAY[0.28, 0, 0.3]),
("Scott", 40, 'Female', 50000, ARRAY[0.37, 1, 0.2]),
("Kevin", 65, 'Male', 150000, ARRAY[0.79, 0, 0.6]),
("Malone", 75, 'Female', 250000, ARRAY[0.95, 1, 1]),
("Dwight", 50, 'Male', 100000, ARRAY[0.54, 0, 0.4]),
("Schrute", 60, 'Female', 80000, ARRAY[0.70, 1, 0.32]);


-- Cosine Similarity Approach
-- Cosine similarity measures the cosine of the angle between two vectors.
--postgres=# SELECT * FROM user_profiles ORDER BY profile_vector <=> '[0.1, 0, 0.12]' LIMIT 2;
-- user_id | age | gender | income | profile_vector
-----------+-----+--------+--------+----------------
--       1 |  25 | Male   |  30000 | [0.1,0,0.12]
--       5 |  35 | Male   |  75000 | [0.28,0,0.3]
--(2 rows)

-- Euclidean Distance Approach
-- Euclidean distance, on the other hand, measures the ‘straight-line’ distance
-- This approach is particularly useful when the magnitude of the vectors is not as important
-- as the direction in which they point.
-- between two points in multi-dimensional space.

-- This method is more sensitive to the magnitude of the attributes.
--postgres=# SELECT *, (profile_vector <-> '[0.1, 0, 0.12]') as distance FROM user_profiles ORDER BY distance LIMIT 2;
-- user_id | age | gender | income | profile_vector |      distance
-----------+-----+--------+--------+----------------+--------------------
--       1 |  25 | Male   |  30000 | [0.1,0,0.12]   |                  0
--       5 |  35 | Male   |  75000 | [0.28,0,0.3]   | 0.2545584445286602
--(2 rows)

-- https://medium.com/@mauricio/optimizing-ivfflat-indexing-with-pgvector-in-postgresql-755d142e54f5

--postgres=# explain SELECT *, (profile_vector <-> '[0.1, 0, 0.12]') as distance FROM user_profiles ORDER BY distance LIMIT 2;
--                                 QUERY PLAN
-------------------------------------------------------------------------------
-- Limit  (cost=25.98..25.98 rows=2 width=94)
--   ->  Sort  (cost=25.98..27.75 rows=710 width=94)
--         Sort Key: ((profile_vector <-> '[0.1,0,0.12]'::vector))
--         ->  Seq Scan on user_profiles  (cost=0.00..18.88 rows=710 width=94)
