- use singular nouns for database tables
ex. ad_campaign, ad__placement

- while writing SQLs, use RETURNING to fetch data after Write/ Update operation is done that way 
I don't have to make another call to fetch latest version of data.
```
INSERT INTO ad_targeting (ad_id, geo_location_lat, geo_location_long) VALUES(7, 47, -122)
RETURNING id, geo_location_lat, geo_location_long;
```

- always keep a full version of schema in folder `db/`. so that someone can recreate the database locally
- always write migration scripts inside `db/` folder

read:
---
- https://docs.microsoft.com/en-us/dynamicsax-2012/developer/best-practice-performance-optimizations-database-design-and-operations
- https://stackoverflow.com/questions/7662/database-table-and-column-naming-conventions
- https://blog.sqreen.com/preventing-sql-injections-in-java-and-other-vulnerabilities/
