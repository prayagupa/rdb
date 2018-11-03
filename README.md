
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

create table Inventory (warehouse VARCHAR(20), sku VARCHAR(20), qty int);

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


oracle
------

```
select * from v$version;

"CORE	12.1.0.2.0	Production"
```