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

-- insert review data
INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (1, 101, 5, 'Amazing product! Highly recommend.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (2, 101, 4, 'Very good quality, but a bit expensive.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (3, 101, 3, 'Average product. Does the job.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (4, 102, 2, 'Not satisfied with the performance.');

INSERT INTO customer_reviews (customer_id, product_id, rating, review_text)
VALUES (5, 102, 1, 'Terrible! Would not buy again.');

-- search by token that must exist like expensive
-- searching by expens won't work
SELECT * FROM customer_reviews
WHERE review_text_search @@ to_tsquery('expensive');

--
SELECT * FROM customer_reviews
WHERE product_id = 102
AND review_text_search @@ to_tsquery('qual:*');
