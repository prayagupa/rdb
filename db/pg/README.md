postgres
-------

```bash
## install psql CLI
brew install postgresql
```

```
## connect to container
root@63750bc48a06:/# psql -U postgres
psql (12.2 (Debian 12.2-1.pgdg100+1))
Type "help" for help.

λ psql -h analytics.???.us-east-1.rds.amazonaws.com -p 5432 -d postgres -U postgres -W
Password: 
psql (11.4)
SSL connection (protocol: TLSv1.2, cipher: ECDHE-RSA-AES256-GCM-SHA384, bits: 256, compression: off)
Type "help" for help.
```

```
postgres=> SELECT current_database();
 current_database 
------------------
 postgres
(1 row)

postgres=# \d
                    List of relations
 Schema |           Name            |   Type   |  Owner
--------+---------------------------+----------+----------
 public | museum_visit              | table    | postgres
 public | museum_visit_visit_id_seq | sequence | postgres
 public | visiting_user             | table    | postgres
 public | visiting_user_user_id_seq | sequence | postgres
(4 rows)

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

##show tz
postgres=# SHOW TIMEZONE;
 TimeZone
----------
 Etc/UTC
(1 row)

postgres=# SELECT CURRENT_TIMESTAMP;
       current_timestamp
-------------------------------
 2020-02-24 01:23:05.308574+00
(1 row)

##

postgres=# \x
Expanded display is on.

postgres=# select * from museum_visit;
-[ RECORD 1 ]-----+------------------------------
user_id           | 1
visit_id          | 1
museum_name       | Dharahara centre
department        | Front door
visit_start_tz    | 2019-08-03 02:00:00+00
visit_start_local | 2019-08-02 18:00:00
visit_end_tz      | 2019-08-03 02:10:00+00
visit_end_local   | 2019-08-02 18:10:00
created           | 2020-02-24 01:34:34.579363+00

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

psql `du` (disk usage)
--

```sql
-- https://wiki.postgresql.org/wiki/Disk_Usage
SELECT nspname || '.' || relname AS "relation",
    pg_size_pretty(pg_relation_size(C.oid)) AS "size"
  FROM pg_class C
  LEFT JOIN pg_namespace N ON (N.oid = C.relnamespace)
  WHERE nspname NOT IN ('pg_catalog', 'information_schema')
  ORDER BY pg_relation_size(C.oid) DESC
  LIMIT 20;
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


export
------

```sql
-- change statement execution timeout settings to make sure you can export within time
COPY (select * from my_table where tb_time_local > '2020-06-01' and tb_time_local < '2500-06-01') 
TO '/Users/upd/Downloads/team_table.csv' DELIMITER ',' CSV HEADER;

```


```bash

initdb - create a new PostgreSQL database cluster
ps aux | grep java

16:18:50.953 [main] INFO io.zonky.test.db.postgres.embedded.DefaultPostgresBinaryResolver - System specific postgres binaries found: postgres-darwin-x86_64.txz
16:18:53.736 [main] INFO io.zonky.test.db.postgres.embedded.EmbeddedPostgres - Postgres binaries at /var/folders/t0/d_6dzlq541v5tm9217k13wyx81n8sq/T/embedded-pg/PG-b5deca9b00e4aec854a9675e6695f78b
 
file = "file:/Users/a1353612/.m2/repository/io/zonky/test/postgres/embedded-postgres-binaries-darwin-amd64/10.15.0-1/embedded-postgres-binaries-darwin-amd64-10.15.0-1.jar!/postgres-darwin-x86_64.txz"

/var/folders/t0/d_6dzlq541v5tm9217k13wyx81n8sq/T/embedded-pg/PG-b5deca9b00e4aec854a9675e6695f78b/bin/pg_ctl -D /tmp/pg_unit_tests/data -l logfile start

Process [/var/folders/t0/d_6dzlq541v5tm9217k13wyx81n8sq/T/embedded-pg/PG-b5deca9b00e4aec854a9675e6695f78b/bin/initdb, -A, trust, -U, postgres, -D, /tmp/pg_unit_tests/data, -E, UTF-8] failed
java.lang.IllegalStateException: Process [/var/folders/t0/d_6dzlq541v5tm9217k13wyx81n8sq/T/embedded-pg/PG-b5deca9b00e4aec854a9675e6695f78b/bin/initdb, -A, trust, -U, postgres, -D, /tmp/pg_unit_tests/data, -E, UTF-8] failed
	at io.zonky.test.db.postgres.embedded.EmbeddedPostgres.system(EmbeddedPostgres.java:632)
	at io.zonky.test.db.postgres.embedded.EmbeddedPostgres.initdb(EmbeddedPostgres.java:252)
	at io.zonky.test.db.postgres.embedded.EmbeddedPostgres.<init>(EmbeddedPostgres.java:159)
	at io.zonky.test.db.postgres.embedded.EmbeddedPostgres$Builder.start(EmbeddedPostgres.java:583)
	at com.testing.EmbeddedPostgresTests.before(EmbededPostgresTests.java:28)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:24)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecutor.runTestClass(JUnitTestClassExecutor.java:110)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecutor.execute(JUnitTestClassExecutor.java:58)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecutor.execute(JUnitTestClassExecutor.java:38)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestClassProcessor.processTestClass(AbstractJUnitTestClassProcessor.java:62)
	at org.gradle.api.internal.tasks.testing.SuiteTestClassProcessor.processTestClass(SuiteTestClassProcessor.java:51)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:36)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:94)
	at com.sun.proxy.$Proxy2.processTestClass(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.processTestClass(TestWorker.java:119)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:36)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)
	at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:182)
	at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:164)
	at org.gradle.internal.remote.internal.hub.MessageHub$Handler.run(MessageHub.java:414)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
	at java.base/java.lang.Thread.run(Thread.java:835)


```