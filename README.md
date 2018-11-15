
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

insert into Inventory VALUES('De Moines', 'sku-1', 88);
insert into Inventory VALUES('Seattle', 'sku-2', 99);
insert into Inventory VALUES('Tacoma', 'sku-3', 11);
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

oracle
------

```
-- https://github.com/MaksymBilenko/docker-oracle-apex

docker pull sath89/oracle-12c
docker run -d -p 8080:8080 -p 1521:1521 sath89/oracle-12c
sqlplus system/oracle@//localhost:1521/xe
SYSTEM@oracle

select * from v$version;
"CORE	12.1.0.2.0	Production"


CREATE TABLE Inventory (id NUMBER(10) NOT NULL, warehouse VARCHAR(20), sku VARCHAR(20), qty NUMBER(10));
ALTER TABLE Inventory ADD (CONSTRAINT inv_pk PRIMARY KEY (ID));
CREATE SEQUENCE inv_pk START WITH 1;

CREATE TABLE CustomerOrder (id NUMBER, name VARCHAR(20), active VARCHAR(2), created TIMESTAMP);
INSERT INTO CustomerOrder VALUES(1, 'steve jobs', '01', CURRENT_TIMESTAMP);

```

takes `~200ms` could be because of oracle connection is via on VPN.
Weird that `WHERE ROWNUM <=1` adds increases latency.