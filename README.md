
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
```
