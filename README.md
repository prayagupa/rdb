
database requests perf

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

[oracle 12 db](https://docs.oracle.com/database/121/CNCPT/intro.htm#CNCPT001)
------

see [oracle/README.md](db/oracle/README.md)

```bash
aws rds describe-db-instances --profile aws-default --region us-west-2

-- add proper firewall to fix Can't connect to MySQL server on
-- also not just firewall-group, which will work only if the internet gateway is there for your Vitual Private Cloud(VPC) - https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Internet_Gateway.html
-- also also also make sure to add subnetwork to route table - https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html
-- also make sure VPN is not screwing up things

mysql -h duwamish.<<12>>.us-west-2.rds.amazonaws.com -P 3306 -u root -p
```

takes `~200ms` could be because of oracle connection is via on VPN.
Weird that `WHERE ROWNUM <=1` adds increases latency.

for testing h2-oracle [http://www.h2database.com/html/features.html](http://www.h2database.com/html/features.html)
