alter session set "_ORACLE_SCRIPT"=true;
create user duwamish identified by Duwamish9;
grant connect, resource, dba to duwamish;
grant create session to duwamish with admin option;
grant unlimited tablespace to duwamish;

ALTER SESSION SET CURRENT_SCHEMA = duwamish;
