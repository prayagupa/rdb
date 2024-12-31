CREATE TABLE customer_reviews_search (
    review_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT,
    review_date TIMESTAMP DEFAULT current_timestamp()
);

-- https://www.cockroachlabs.com/docs/stable/full-text-search
-- only available after v23
ALTER TABLE customer_reviews_search ADD COLUMN review_text_search TSVECTOR AS (to_tsvector('english', review_text)) STORED;
-- CREATE INDEX ON customer_reviews_search USING GIN (to_tsvector('english', review_text));

-- CREATE INDEX ON customer_reviews_search USING GIN (review_text_search));

INSERT INTO customer_reviews_search (customer_id, product_id, rating, review_text)
VALUES (1, 101, 5, 'Amazing product! Highly recommend.'),
       (2, 101, 4, 'Very good quality, but a bit expensive.'),
       (3, 101, 3, 'Average product. Does the job.'),
       (1, 102, 2, 'Not satisfied with the performance.'),
       (2, 102, 1, 'Terrible! Would not buy again.');

-- search
SELECT * FROM customer_reviews_search
WHERE review_text @@ 'quality';
