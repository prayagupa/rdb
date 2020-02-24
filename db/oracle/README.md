

[oracle 12 db](https://docs.oracle.com/database/121/CNCPT/intro.htm#CNCPT001)
------

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


- https://www.oracle.com/database/technologies/rac.html
- https://aws.amazon.com/blogs/database/amazon-aurora-as-an-alternative-to-oracle-rac/
- https://docs.oracle.com/cd/E18283_01/server.112/e17110/bgprocesses.htm

```bash
sudo mkdir -p /data/oracle
sudo chmod 777 -R /data

## share /data folder with docker for mounting

# git clone https://github.com/oracle/docker-images.git
$ ll OracleDatabase/SingleInstance/dockerfiles/12.2.0.1/
total 6747696
-rw-r--r--  1 updupdupd  184630988          62 Feb 22 15:31 Checksum.ee
-rw-r--r--  1 updupdupd  184630988          62 Feb 22 15:31 Checksum.se2
-rw-r--r--  1 updupdupd  184630988        3462 Feb 22 15:31 Dockerfile
-rwxr-xr-x  1 updupdupd  184630988        1050 Feb 22 15:31 checkDBStatus.sh
-rwxr-xr-x  1 updupdupd  184630988         905 Feb 22 15:31 checkSpace.sh
-rwxr-xr-x  1 updupdupd  184630988        2953 Feb 22 15:31 createDB.sh
-rw-r--r--  1 updupdupd  184630988        6878 Feb 22 15:31 db_inst.rsp
-rw-r--r--  1 updupdupd  184630988        9204 Feb 22 15:31 dbca.rsp.tmpl
-rwxr-xr-x  1 updupdupd  184630988        2495 Feb 22 15:31 installDBBinaries.sh
-rw-r--r--@ 1 updupdupd  184630988  3453696911 Feb 22 15:58 linuxx64_12201_database.zip
-rwxr-xr-x  1 updupdupd  184630988        6526 Feb 22 15:31 runOracle.sh
-rwxr-xr-x  1 updupdupd  184630988        1015 Feb 22 15:31 runUserScripts.sh
-rwxr-xr-x  1 updupdupd  184630988         758 Feb 22 15:31 setPassword.sh
-rwxr-xr-x  1 updupdupd  184630988         941 Feb 22 15:31 setupLinuxEnv.sh
-rwxr-xr-x  1 updupdupd  184630988         678 Feb 22 15:31 startDB.sh


cd OracleDatabase/SingleInstance/dockerfiles
./buildDockerImage.sh -v 12.2.0.1 -e

$ docker images
REPOSITORY                                                             TAG                            IMAGE ID            CREATED             SIZE
oracle/database                                                        12.2.0.1-ee                    bcb7c9f64985        32 hours ago        6.11GB
oraclelinux                                                            7-slim                         c3d869388183        5 weeks ago         117MB

docker run --name oracle \                                                                                                                             
-p 1521:1521 -p 5500:5500 \                                                                            
-e ORACLE_SID=xe \                                                                                     
-e ORACLE_PDB=duwamishpdb \                                                                               
-e ORACLE_PWD=Duwamish9 \                                                                                
-e ORACLE_CHARACTERSET=AL32UTF8 \                                                                      
-v /data/oracle:/opt/oracle/oradata \                                                                           
oracle/database:12.2.0.1-ee

## SecureShell into oracle container
[oracle@6be299c2c700 ~]$ ps aux | grep oracle
oracle       1  0.0  0.1  11696  2472 ?        Ss   08:35   0:00 /bin/bash /opt/oracle/runOracle.sh
oracle      26  0.0  0.5 215296 11012 ?        Ssl  08:35   0:00 /opt/oracle/product/12.2.0.1/dbhome_1/bin/tnslsnr LISTENER -inherit

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

[oracle@6be299c2c700 ~]$ ls -l /docker-entrypoint-initdb.d
lrwxrwxrwx 1 root root 19 Feb 23 00:15 /docker-entrypoint-initdb.d -> /opt/oracle/scripts

sqlplus duwamish/Duwamish9@//localhost:1521/ORCLCDB as sysdba ## sqlplus system/oracle@//localhost:1521/xe
SQL> connect sys/Duwamish9@localhost:1521/ORCLCDB

select * from v$version;
"CORE	12.1.0.2.0	Production"

SELECT owner, table_name FROM dba_tables;

[oracle@99de476b8016 ~]$ lsnrctl 

LSNRCTL for Linux: Version 12.2.0.1.0 - Production on 25-FEB-2019 01:58:43

Copyright (c) 1991, 2016, Oracle.  All rights reserved.

Welcome to LSNRCTL, type "help" for information.

LSNRCTL> help
The following operations are available
An asterisk (*) denotes a modifier or extended command:

start           stop            status          services        
servacls        version         reload          save_config     
trace           spawn           quit            exit            
set*            show*           

LSNRCTL> status
Connecting to (DESCRIPTION=(ADDRESS=(PROTOCOL=IPC)(KEY=EXTPROC1)))
STATUS of the LISTENER
------------------------
Alias                     LISTENER
Version                   TNSLSNR for Linux: Version 12.2.0.1.0 - Production
Start Date                25-FEB-2019 01:46:12
Uptime                    0 days 0 hr. 12 min. 42 sec
Trace Level               off
Security                  ON: Local OS Authentication
SNMP                      OFF
Listener Parameter File   /opt/oracle/product/12.2.0.1/dbhome_1/network/admin/listener.ora
Listener Log File         /opt/oracle/diag/tnslsnr/99de476b8016/listener/alert/log.xml
Listening Endpoints Summary...
  (DESCRIPTION=(ADDRESS=(PROTOCOL=ipc)(KEY=EXTPROC1)))
  (DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=0.0.0.0)(PORT=1521)))
  (DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=99de476b8016)(PORT=5500))(Security=(my_wallet_directory=/opt/oracle/admin/ORCLCDB/xdb_wallet))(Presentation=HTTP)(Session=RAW))
Services Summary...
Service "82af308615e70949e053020012aca602" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
Service "ORCLCDB" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
Service "ORCLCDBXDB" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
Service "duwamish" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
The command completed successfully

LSNRCTL> services
Connecting to (DESCRIPTION=(ADDRESS=(PROTOCOL=IPC)(KEY=EXTPROC1)))
Services Summary...
Service "82af308615e70949e053020012aca602" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
    Handler(s):
      "DEDICATED" established:0 refused:0 state:ready
         LOCAL SERVER
Service "ORCLCDB" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
    Handler(s):
      "DEDICATED" established:0 refused:0 state:ready
         LOCAL SERVER
Service "ORCLCDBXDB" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
    Handler(s):
      "D000" established:0 refused:0 current:0 max:1022 state:ready
         DISPATCHER <machine: 99de476b8016, pid: 2169>
         (ADDRESS=(PROTOCOL=tcp)(HOST=99de476b8016)(PORT=39203))
Service "duwamish" has 1 instance(s).
  Instance "ORCLCDB", status READY, has 1 handler(s) for this service...
    Handler(s):
      "DEDICATED" established:0 refused:0 state:ready
         LOCAL SERVER
The command completed successfully


[oracle@99de476b8016 ~]$ cat /opt/oracle/product/12.2.0.1/dbhome_1/network/admin/listener.ora 
LISTENER = 
(DESCRIPTION_LIST = 
  (DESCRIPTION = 
    (ADDRESS = (PROTOCOL = IPC)(KEY = EXTPROC1)) 
    (ADDRESS = (PROTOCOL = TCP)(HOST = 0.0.0.0)(PORT = 1521)) 
  ) 
) 

DEDICATED_THROUGH_BROKER_LISTENER=ON
DIAG_ADR_ENABLED = off


[oracle@99de476b8016 ~]$ cat /opt/oracle/product/12.2.0.1/dbhome_1/network/admin/tnsnames.ora 
ORCLCDB=localhost:1521/ORCLCDB
DUWAMISHPDB= 
(DESCRIPTION = 
  (ADDRESS = (PROTOCOL = TCP)(HOST = 0.0.0.0)(PORT = 1521))
  (CONNECT_DATA =
    (SERVER = DEDICATED)
    (SERVICE_NAME = DUWAMISHPDB)
  )
)
```

```sql
/*create user: sql file*/

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

##
# docker pull sath89/oracle-12c
# docker run -d -p 8080:8080 -p 1521:1521 sath89/oracle-12c

```
