
database requests perf

**start [db/mysql](https://hub.docker.com/_/mysql/)**

```
docker-compose up
```


```
docker exec -it 0cad383c1249 mysql -u root -p
#mysql -u root -p

create database updupd;
use updupd;

CREATE TABLE Inventory (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, warehouse VARCHAR(20), sku VARCHAR(20), qty int);
-- ALTER TABLE Inventory ADD PRIMARY KEY(id);

insert into Inventory (warehouse, sku, qty) VALUES('De Moines', 'sku-1', 88);
insert into Inventory (warehouse, sku, qty) VALUES('Seattle', 'sku-2', 99);
insert into Inventory (warehouse, sku, qty) VALUES('Tacoma', 'sku-3', 11);
```

results:

shared connection:

```
================ shared =======================
=================total: 157ms=======================
=================average: 1ms=======================
================ shared =======================
```

One connection for each req:

note there is cost of creating connection at run time as well.

```
  ================ Individual ==========================
  =================total: 259ms=======================
  =================average: 2ms=============
  ================ Individual ==========================
```

```
CREATE TABLE Config(
        config_id INTEGER AUTO_INCREMENT PRIMARY KEY,
        config_uuid VARCHAR(64) NOT NULL,
        a_id INTEGER NOT NULL,
        environment VARCHAR(32) NOT NULL,
        requested_on DATETIME NOT NULL,
        requested_by VARCHAR(64) NOT NULL,
        approved_on DATETIME,
        approved_by VARCHAR(64),
        rejected_on DATETIME,
        rejected_by VARCHAR(64),
        canceled_on DATETIME,
        canceled_by VARCHAR(64),
        created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

[oracle 12 db]()
------

```bash
-- https://github.com/MaksymBilenko/docker-oracle-apex
-- sudo mkdir -p /data/oracle
-- share /data folder with docker for mounting

# docker pull sath89/oracle-12c
# docker run -d -p 8080:8080 -p 1521:1521 sath89/oracle-12c

# git clone https://github.com/oracle/docker-images.git
$ ll OracleDatabase/SingleInstance/dockerfiles/12.2.0.1/
total 6747696
-rw-r--r--  1 a1353612  184630988          62 Feb 22 15:31 Checksum.ee
-rw-r--r--  1 a1353612  184630988          62 Feb 22 15:31 Checksum.se2
-rw-r--r--  1 a1353612  184630988        3462 Feb 22 15:31 Dockerfile
-rwxr-xr-x  1 a1353612  184630988        1050 Feb 22 15:31 checkDBStatus.sh
-rwxr-xr-x  1 a1353612  184630988         905 Feb 22 15:31 checkSpace.sh
-rwxr-xr-x  1 a1353612  184630988        2953 Feb 22 15:31 createDB.sh
-rw-r--r--  1 a1353612  184630988        6878 Feb 22 15:31 db_inst.rsp
-rw-r--r--  1 a1353612  184630988        9204 Feb 22 15:31 dbca.rsp.tmpl
-rwxr-xr-x  1 a1353612  184630988        2495 Feb 22 15:31 installDBBinaries.sh
-rw-r--r--@ 1 a1353612  184630988  3453696911 Feb 22 15:58 linuxx64_12201_database.zip
-rwxr-xr-x  1 a1353612  184630988        6526 Feb 22 15:31 runOracle.sh
-rwxr-xr-x  1 a1353612  184630988        1015 Feb 22 15:31 runUserScripts.sh
-rwxr-xr-x  1 a1353612  184630988         758 Feb 22 15:31 setPassword.sh
-rwxr-xr-x  1 a1353612  184630988         941 Feb 22 15:31 setupLinuxEnv.sh
-rwxr-xr-x  1 a1353612  184630988         678 Feb 22 15:31 startDB.sh


cd OracleDatabase/SingleInstance/dockerfiles
./buildDockerImage.sh -v 12.2.0.1 -e

$ docker images
REPOSITORY                                                             TAG                            IMAGE ID            CREATED             SIZE
oracle/database                                                        12.2.0.1-ee                    bcb7c9f64985        32 hours ago        6.11GB
oraclelinux                                                            7-slim                         c3d869388183        5 weeks ago         117MB

docker run --name oracle \                                                                                                                             
-p 1521:1521 -p 5500:5500 \                                                                            
-e ORACLE_SID=xe \                                                                                     
-e ORACLE_PDB=duwamish \                                                                               
-e ORACLE_PWD=Duwamish9 \                                                                                
-e ORACLE_CHARACTERSET=AL32UTF8 \                                                                      
-v /data/oracle:/opt/oracle/oradata \                                                                           
oracle/database:12.2.0.1-ee 

[oracle@7156661d8155 ~]$ echo $ORACLE_HOME/
/opt/oracle/product/12.2.0.1/dbhome_1/

[oracle@7156661d8155 ~]$ ls -l /opt/oracle/
total 72
drwxr-x--- 3 oracle oinstall 4096 Feb 23 19:41 admin
drwxr-x--- 2 oracle oinstall 4096 Feb 23 19:41 audit
drwxr-x--- 4 oracle oinstall 4096 Feb 23 19:45 cfgtoollogs
-rwxr-xr-x 1 oracle dba      1050 Feb 22 23:31 checkDBStatus.sh
drwxr-xr-x 2 oracle dba      4096 Feb 23 00:22 checkpoints
-rwxr-xr-x 1 oracle dba      2953 Feb 22 23:31 createDB.sh
-rw-r--r-- 1 oracle dba      9204 Feb 22 23:31 dbca.rsp.tmpl
drwxrwxr-x 1 oracle dba      4096 Feb 23 00:22 diag
drwxrwx--- 1 oracle dba      4096 Feb 23 00:22 oraInventory
drwxrwxrwx 4 oracle oinstall  128 Feb 23 19:41 oradata
drwxr-xr-x 1 oracle dba      4096 Feb 23 00:15 product
-rwxr-xr-x 1 oracle dba      6526 Feb 22 23:31 runOracle.sh
-rwxr-xr-x 1 oracle dba      1015 Feb 22 23:31 runUserScripts.sh
drwxr-xr-x 1 oracle dba      4096 Feb 23 00:15 scripts
-rwxr-xr-x 1 oracle dba       758 Feb 22 23:31 setPassword.sh
-rwxr-xr-x 1 oracle dba       678 Feb 22 23:31 startDB.sh

sqlplus system/oracle@//localhost:1521/xe
SYSTEM@oracle

select * from v$version;
"CORE	12.1.0.2.0	Production"

/*create user*/
create user duwamish identified by duwamish;
grant connect, resource, dba to duwamish;
grant create session to duwamish with admin option;
grant unlimited tablespace to duwamish;

/**/
select * from dba_profiles; 

CREATE TABLE customer (
    id NUMBER(10) NOT NULL, 
    name VARCHAR(130), 
    address VARCHAR(130), 
    loyalty_point NUMBER(10), 
    username VARCHAR(130)
);

CREATE TABLE Inventory (id NUMBER(10) NOT NULL, warehouse VARCHAR(20), sku VARCHAR(20), qty NUMBER(10));
ALTER TABLE Inventory ADD (CONSTRAINT inv_pk PRIMARY KEY (ID));
CREATE SEQUENCE inv_pk START WITH 1;

CREATE TABLE CustomerOrder (id NUMBER, name VARCHAR(20), active VARCHAR(2), created TIMESTAMP);
INSERT INTO CustomerOrder VALUES(1, 'steve jobs', '01', CURRENT_TIMESTAMP);

```

```bash
aws rds describe-db-instances --profile aws-default --region us-west-2

-- add proper firewall to fix Can't connect to MySQL server on
-- also make sure VPN is not screwing up things

mysql -h duwamish.<<12>>.us-west-2.rds.amazonaws.com -P 3306 -u root -p
```

takes `~200ms` could be because of oracle connection is via on VPN.
Weird that `WHERE ROWNUM <=1` adds increases latency.

for testing h2-oracle [http://www.h2database.com/html/features.html](http://www.h2database.com/html/features.html)
