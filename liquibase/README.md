
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

