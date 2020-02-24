
**[db/mysql](https://hub.docker.com/_/mysql/)**
---

```bash
docker-compose up
```

```bash
docker exec -it 0cad383c1249 mysql -u root -p
#mysql -u root -p

## apply SQL in db/mysql/1.sql
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
