
database properties
----------

https://en.wikipedia.org/wiki/ACID#Characteristics


| Property |  Desc  |
|-----------|-------|
| A | all or nothing // on power failures, errors, and crashes trxn is either COMMITED or ROLLED BACK |
| C | ensures that any transaction will bring the database from one valid state to another. |
| I | concurrent execution of transactions results in a system state that would be obtained if transactions were executed serially, i.e., one after the other |
| D | once a transaction has been committed, it will remain so, even in the event of power loss, crashes, or errors |


[ACID and database transactions?](http://stackoverflow.com/a/3740307/432903)

[Strong consistency](https://distributedalgorithm.wordpress.com/2015/06/13/two-phase-commit/)/ [Consensus Protocol/2 Phase commit(2PC) in Database Transactions](https://en.wikipedia.org/wiki/Two-phase_commit_protocol#Basic_algorithm)
--------------------------------------------
_DB, LMU 2013_

[Distributed algos and protocols](https://www.cl.cam.ac.uk/teaching/0809/DistSys/3-algs.pdf)

- [Commit request phase](http://the-paper-trail.org/blog/consensus-protocols-two-phase-commit/), voting phase
- Commit phase, completion phase

```
Master Node/Co-ordinator                                 Secondary Node/ Cohort
                              QUERY TO COMMIT
                     -------------------------------->
                     
                     
                             Execute The Transaction
                          (adds to UNDO_LOG & REDO_LOG)
                                   &                     prepare*/abort*
                                VOTE YES/NO           
                     <-------------------------------
                     
commit*/abort*                COMMIT/ROLLBACK
                     -------------------------------->
                     
                              ACKNOWLEDGMENT            commit*/abort*
                     <--------------------------------  
end
```

![](http://the-paper-trail.org/blog/wp-content/uploads/2010/01/tpc-fault-free-phase-1.png)

**2PC disadvantage: **

```
The greatest disadvantage of the 2PC protocol is that it is a blocking protocol. 

If the master(coordinator) fails permanently, some secondaries(cohorts) will never resolve their 
transactions: 
After a secondary has sent an agreement message to the master, it will block until a commit or 
rollback is received.
```

https://en.wikipedia.org/wiki/Partition_(database)

[When and why are relational database joins expensive?](https://stackoverflow.com/a/174047/432903)

[The Join Operation](http://use-the-index-luke.com/sql/join)

[Performance Considerations for Join Queries](https://www.cloudera.com/documentation/enterprise/5-9-x/topics/impala_perf_joins.html)

Relational Algebra/ [JOINs](https://goo.gl/cH4lSN)
--------------------------------------------------

[What is Cartesian product in relational algebra?](https://en.wikipedia.org/wiki/Relational_algebra)

```
weights = { 
             [packageA1, 100g], [packageA2, 200g]
          }
shippingDates = { 
             [packageB1, 07-2016], [packageB2, 08-2016], [packageB3, 09-2016]
          }

weights * shippingDates = { {[packageA1, 100g], [packageB1, 07-2016]}, 
                            {[packageA1, 100g], [packageB2, 08-2016]},
                            {[packageA1, 100g], [packageB3, 09-2016]},
                            
                            {[packageA2, 200g], [packageB1, 07-2016]},
                            {[packageA2, 200g], [packageB2, 08-2016]},
                            {[packageA2, 200g], [packageB3, 09-2016]}
                            
                            
```

[What is the difference between `LEFT JOIN` and `LEFT OUTER JOIN`?](http://stackoverflow.com/a/4401540/432903), JWN, 07-2016

Intersection: [Set1 `INNER JOIN` Set2](https://goo.gl/qZUi8K)

(A-B) U (B-A):  [Set1 `OUTER JOIN` Set2](https://goo.gl/IbGzK3)

```
The result of a left outer join (or simply left join) for tables A and B always contains all rows 
of the "left" table (A), even if the join-condition does not find any matching row in the "right" table (B). 
```

[`LEFT` OUTER `JOIN` operation](http://docs.oracle.com/javadb/10.4.2.1/ref/rrefsqlj18922.html) (INTV, HUM 06-08-2016)

```
It preserves the unmatched rows from the first (left) table, 
joining them with a NULL row in the shape of the second (right) table.
```


![SQL joins](http://i.stack.imgur.com/VQ5XP.png)

[INNER JOIN vs. Natural JOIN (JOIN ON), speed-wise?](http://stackoverflow.com/a/4841554/432903)

[Set difference `(A-B)` in SQL – M SQL Server](https://timsinajaya.wordpress.com/2010/09/30/set-difference-in-sql-m-sql-server/)

```sql
SELECT DISTINCT A.*
  FROM (A LEFT OUTER JOIN B on A.ID=B.ID) 
    WHERE B.ID IS NULL
```

data indexing
----

- https://devcenter.heroku.com/articles/postgresql-indexes

partitioning
-------------

- https://www.postgresql.org/docs/10/ddl-partitioning.html
- https://blog.timescale.com/blog/scaling-partitioning-data-postgresql-10-explained-cd48a712a9a1/

data archiving
--------------

- https://aws.amazon.com/blogs/database/archiving-data-from-relational-databases-to-amazon-glacier-via-aws-dms/
- https://aws.amazon.com/dms/
- https://www.fusionbox.com/blog/detail/postgresql-wal-archiving-with-wal-g-and-s3-complete-walkthrough/644/

- https://www.percona.com/blog/2019/07/10/wal-retention-and-clean-up-pg_archivecleanup/
- https://www.postgresql.org/docs/9.1/sql-vacuum.html
- https://www.postgresql.org/docs/9.3/continuous-archiving.html
- https://www.postgresql.org/docs/11/routine-vacuuming.html
- https://confluence.atlassian.com/kb/optimize-and-improve-postgresql-performance-with-vacuum-analyze-and-reindex-885239781.html
- https://docs.microsoft.com/en-us/azure/backup/backup-azure-database-postgresql

[database connection pooling](http://stackoverflow.com/a/4041136/432907)
-------------------

```
Database connection pooling is a method used to keep database TCP connections open so they can 
be reused by others.
```

https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing

[Calculating connection pool](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing#the-formula)

```bash
max_connections = ((os_core_count * 2) + effective_spindle_count)

If your little 4-Core i7 server with one hard disk: 
connection pool = ((4 * 2) + 1) = 9
```

[databases connection transport protocols](https://unix.stackexchange.com/a/32138/17781)
 - TCP
 - Socket (Unix socket file connection to local server)

https://www.datastax.com/dev/blog/binary-protocol

database page
--------------

- Database stores all records inside a fixed-size disk unit which is commonly called a "page" 
(Some database engine sometimes calls it a "block" instead).
- the internal basic structure to organize the data in the database files.

- https://www.postgresql.org/docs/8.0/storage-page-layout.html
- https://dev.mysql.com/doc/internals/en/innodb-page-structure.html

transaction locking
--------------------

- http://www.methodsandtools.com/archive/archive.php?id=83

- https://en.wikipedia.org/wiki/Relational_algebra#Aggregation

High Availability(A), Load Balancing, and Data replication
--------

- https://www.postgresql.org/docs/9.3/high-availability.html
- https://wiki.postgresql.org/wiki/Streaming_Replication

db performance
------------

- https://www.datadoghq.com/blog/100x-faster-postgres-performance-by-changing-1-line/

database designs
-------

- https://github.com/parayaluyanta/SOA/blob/master/system_design/photo_sharing_service.md#step-4-data-model-definition-design

relational databases
--

- [mysql/README.md](db/mysql/README.md)
- [pg/README.md](db/pg/README.md)
- https://azure.microsoft.com/en-us/services/time-series-insights/
- https://azure.microsoft.com/en-us/services/synapse-analytics/
- [oracle/README.md](db/oracle/README.md)

database services
--

- https://docs.microsoft.com/en-us/azure/azure-sql/database/elastic-pool-overview
