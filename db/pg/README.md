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

```