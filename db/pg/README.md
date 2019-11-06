postgres
-------

```bash
brew install postgresql

λ psql -h analytics.???.us-east-1.rds.amazonaws.com -U postgres -W
Password: 
psql (11.4)
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-AES256-GCM-SHA384, bits: 256, compression: off)
Type "help" for help.

postgres=> SELECT current_database();
 current_database 
------------------
 postgres
(1 row)

postgres=> \connect museum_visit;
Password for user postgres: 
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-AES256-GCM-SHA384, bits: 256, compression: off)
You are now connected to database "museum_visit" as user "postgres".

postgres=> \l
                                   List of databases
     Name     |  Owner   | Encoding |   Collate   |    Ctype    |   Access privileges   
--------------+----------+----------+-------------+-------------+-----------------------
 museum_visit | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
 postgres     | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
 rdsadmin     | rdsadmin | UTF8     | en_US.UTF-8 | en_US.UTF-8 | rdsadmin=CTc/rdsadmin
 template0    | rdsadmin | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/rdsadmin          +
              |          |          |             |             | rdsadmin=CTc/rdsadmin
 template1    | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
              |          |          |             |             | postgres=CTc/postgres
(5 rows)

museum_visit=> select count(*) users, max(created)-min(created) time_taken from visiting_user;
 users |   time_taken    
-------+-----------------
 32075 | 00:49:41.048941

museum_visit=> select count(*) users, max(created)-min(created) time_taken from visiting_user;
 users |   time_taken    
-------+-----------------
 38788 | 01:00:00.766083
 
museum_visit=> select count(*) users, max(created)-min(created) time_taken from visiting_user;
 users |   time_taken    
-------+-----------------
 50601 | 01:18:24.258239

```


```sql
-- v11.4, db.m4.large, 8 GB RAM, 2 vCPU
-- using parallelism
-- {"@timestamp":"2019-08-03T14:34:57.895-07:00","@version":"1","message":"HikariPool-1 - Pool stats (total=10, active=7, idle=3, waiting=0)","logger_name":"com.zaxxer.hikari.pool.HikariPool","thread_name":"HikariPool-1 housekeeper","level":"DEBUG","level_value":10000}
-- time taken for 100K users creation: 1350,129ms
museum_visit=> select count(*), max(created) - min(created) time_taken from visiting_user;
 count  |   time_taken    
--------+-----------------
 100000 | 00:22:29.981824
(1 row)
```

RedShift columnar db
-------

```sql
-- dc2.large

-- {"@timestamp":"2019-08-03T20:33:54.961-07:00","@version":"1","message":"HikariPool-1 - Pool stats (total=10, active=7, idle=3, waiting=0)","logger_name":"com.zaxxer.hikari.pool.HikariPool","thread_name":"HikariPool-1 housekeeper","level":"DEBUG","level_value":10000}

select count(*), max(created) - min(created) time_taken from visiting_user;
count time_taken
15666 00:25:13.762115

select count(*), max(created) - min(created) time_taken from visiting_user;
count   time_taken
44130   02:44:54.269513

time taken for 100K users creation: 22440,607ms
```
