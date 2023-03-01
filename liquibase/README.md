
- cd liquibase/visits
- CICD in dev: https://www.liquibase.org/get-started/running-your-first-update

```
$ liquibase update
####################################################
##   _     _             _ _                      ##
##  | |   (_)           (_) |                     ##
##  | |    _  __ _ _   _ _| |__   __ _ ___  ___   ##
##  | |   | |/ _` | | | | | '_ \ / _` / __|/ _ \  ##
##  | |___| | (_| | |_| | | |_) | (_| \__ \  __/  ##
##  \_____/_|\__, |\__,_|_|_.__/ \__,_|___/\___|  ##
##              | |                               ##
##              |_|                               ##
##                                                ## 
##  Get documentation at docs.liquibase.com       ##
##  Get certified courses at learn.liquibase.com  ## 
##  Free schema change activity reports at        ##
##      https://hub.liquibase.com                 ##
##                                                ##
####################################################
Starting Liquibase at 15:53:17 (version 4.18.0 #5864 built at 2022-12-02 18:02+0000)
Liquibase Version: 4.18.0
Liquibase Community 4.18.0 by Liquibase
Running Changeset: retail-changelog.sql::1::prayag.upa
Running Changeset: retail-changelog.sql::2::prayag.upa
Running Changeset: retail-changelog.sql::3::prayag.upa
Liquibase command 'update' was executed successfully.

```

- check http://localhost:8080/frame.jsp?jsessionid=30f3328bf193e4be0f5181e89c7f87c2
- verify SQL
```
select * from databasechangelog;
```

- promote: 
```
liquibase --url=jdbc:h2:tcp://localhost:9090/mem:integration update
```

- rollback: https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html

```sql
liquibase rollbackCount 2
```


- diff https://www.liquibase.org/get-started/developer-workflow

```sql
liquibase diff
####################################################
##   _     _             _ _                      ##
##  | |   (_)           (_) |                     ##
##  | |    _  __ _ _   _ _| |__   __ _ ___  ___   ##
##  | |   | |/ _` | | | | | '_ \ / _` / __|/ _ \  ##
##  | |___| | (_| | |_| | | |_) | (_| \__ \  __/  ##
##  \_____/_|\__, |\__,_|_|_.__/ \__,_|___/\___|  ##
##              | |                               ##
##              |_|                               ##
##                                                ## 
##  Get documentation at docs.liquibase.com       ##
##  Get certified courses at learn.liquibase.com  ## 
##  Free schema change activity reports at        ##
##      https://hub.liquibase.com                 ##
##                                                ##
####################################################
Starting Liquibase at 16:29:17 (version 4.18.0 #5864 built at 2022-12-02 18:02+0000)
Liquibase Version: 4.18.0
Liquibase Community 4.18.0 by Liquibase

Diff Results:
Reference Database: DBUSER @ jdbc:h2:tcp://localhost:9090/mem:integration (Default Schema: PUBLIC)
Comparison Database: DBUSER @ jdbc:h2:tcp://localhost:9090/mem:dev (Default Schema: PUBLIC)
Compared Schemas: PUBLIC
Product Name: EQUAL
Product Version: EQUAL
Missing Catalog(s): NONE
Unexpected Catalog(s): NONE
Changed Catalog(s): 
     INTEGRATION
          name changed from 'INTEGRATION' to 'DEV'
Missing Column(s): NONE
Unexpected Column(s): 
     PUBLIC.COMPANY.ADDRESS1
     PUBLIC.PERSON.ADDRESS1
     PUBLIC.COMPANY.ADDRESS2
     PUBLIC.PERSON.ADDRESS2
     PUBLIC.DATABASECHANGELOG.AUTHOR
     PUBLIC.COMPANY.CITY
     PUBLIC.PERSON.CITY
     PUBLIC.DATABASECHANGELOG.COMMENTS
     PUBLIC.DATABASECHANGELOG.CONTEXTS
     PUBLIC.PERSON.COUNTRY
     PUBLIC.DATABASECHANGELOG.DATEEXECUTED
     PUBLIC.DATABASECHANGELOG.DEPLOYMENT_ID
     PUBLIC.DATABASECHANGELOG.DESCRIPTION
     PUBLIC.DATABASECHANGELOG.EXECTYPE
     PUBLIC.DATABASECHANGELOG.FILENAME
     PUBLIC.COMPANY.ID
     PUBLIC.DATABASECHANGELOG.ID
     PUBLIC.DATABASECHANGELOGLOCK.ID
     PUBLIC.PERSON.ID
     PUBLIC.DATABASECHANGELOG.LABELS
     PUBLIC.DATABASECHANGELOG.LIQUIBASE
     PUBLIC.DATABASECHANGELOGLOCK.LOCKED
     PUBLIC.DATABASECHANGELOGLOCK.LOCKEDBY
     PUBLIC.DATABASECHANGELOGLOCK.LOCKGRANTED
     PUBLIC.DATABASECHANGELOG.MD5SUM
     PUBLIC.COMPANY.NAME
     PUBLIC.PERSON.NAME
     PUBLIC.DATABASECHANGELOG.ORDEREXECUTED
     PUBLIC.PERSON.STATE
     PUBLIC.DATABASECHANGELOG.TAG
Changed Column(s): NONE
Missing Foreign Key(s): NONE
Unexpected Foreign Key(s): NONE
Changed Foreign Key(s): NONE
Missing Index(s): NONE
Unexpected Index(s): 
     PRIMARY_KEY_6 UNIQUE  ON PUBLIC.COMPANY(ID)
     PRIMARY_KEY_67B UNIQUE  ON PUBLIC.PERSON(ID)
     PRIMARY_KEY_D UNIQUE  ON PUBLIC.DATABASECHANGELOGLOCK(ID)
Changed Index(s): NONE
Missing Primary Key(s): NONE
Unexpected Primary Key(s): 
     CONSTRAINT_6 on PUBLIC.COMPANY(ID)
     CONSTRAINT_8 on PUBLIC.PERSON(ID)
     PK_DATABASECHANGELOGLOCK on PUBLIC.DATABASECHANGELOGLOCK(ID)
Changed Primary Key(s): NONE
Missing Schema(s): NONE
Unexpected Schema(s): NONE
Changed Schema(s): NONE
Missing Sequence(s): NONE
Unexpected Sequence(s): NONE
Changed Sequence(s): NONE
Missing Table(s): NONE
Unexpected Table(s): 
     COMPANY
     DATABASECHANGELOG
     DATABASECHANGELOGLOCK
     PERSON
Changed Table(s): NONE
Missing Unique Constraint(s): NONE
Unexpected Unique Constraint(s): NONE
Changed Unique Constraint(s): NONE
Missing View(s): NONE
Unexpected View(s): NONE
Changed View(s): NONE
Liquibase command 'diff' was executed successfully.
```


- https://docs.liquibase.com/workflows/liquibase-community/migrate-with-sql.html?_ga=2.181088999.1317746150.1671730403-1851318663.1671730403
