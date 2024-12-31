CREATE TABLE customer_reviews (
    review_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- A TSVECTOR (Text Search Vector) in PostgreSQL is a data type used
-- for full-text search.
ALTER TABLE customer_reviews ADD COLUMN review_text_search TSVECTOR;

CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE
ON customer_reviews FOR EACH ROW EXECUTE FUNCTION
tsvector_update_trigger(review_text_search, 'pg_catalog.english', review_text);

-- GIN stands for Generalized Inverted Index, a type of index in PostgreSQL
-- that is especially useful for full-text search and other applications
-- requiring the indexing of composite data types.
CREATE INDEX idx_review_text_search ON customer_reviews
USING GIN(review_text_search);

-- Trigram for determining the similarity of alphanumeric text based on trigram matching
-- https://www.postgresql.org/docs/current/pgtrgm.html
CREATE EXTENSION pg_trgm;
CREATE INDEX trgm_idx_review_text_search ON customer_reviews USING GIN (review_text_search);
CREATE INDEX trgm_idx_review_text ON customer_reviews USING GIN (review_text gin_trgm_ops);

-- insert review data
INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (1, 101, 5, 'Amazing product! Highly recommend.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (2, 101, 4, 'Very good quality, but a bit expensive.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (3, 101, 3, 'Average product. Does the job.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (1, 102, 2, 'Not satisfied with the performance.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (2, 102, 1, 'Terrible! Would not buy again.');

-- search by token that must exist like expensive
-- searching by expens won't work
SELECT * FROM customer_reviews
WHERE product_id = 101
AND review_text_search @@ to_tsquery('Expensive');

--
SELECT * FROM customer_reviews
WHERE product_id = 101
AND review_text_search @@ to_tsquery('qual:*');

SELECT * FROM customer_reviews
WHERE product_id = 102
AND review_text_search @@ to_tsquery('Terible');

SET pg_trgm.similarity_threshold = 0.2;

WITH keyword_search AS (
    SELECT * FROM customer_reviews
    WHERE review_text_search @@ to_tsquery('Terible')
)
SELECT * FROM keyword_search
UNION ALL
SELECT * FROM customer_reviews
WHERE review_text % 'Terible'
AND NOT EXISTS (SELECT 1 FROM keyword_search);


-- fuzzy
--SELECT similarity('Terible', 'Terrible');
-- similarity
--------------
--        0.7

--SELECT levenshtein('Terible', 'Terrible');
-- levenshtein
---------------
--           1

--postgres=# SELECT show_trgm('quality');
--                show_trgm
-------------------------------------------
-- {"  q"," qu",ali,ity,lit,qua,"ty ",ual}

--
--SELECT word_similarity('qty', 'quality');
-- word_similarity
-------------------
--            0.25
