--liquibase formatted sql

--changeset prayag.upa:1 labels:retail-store context:retail-store
--comment: add person 
create table IF NOT EXISTS person (
    id int primary key auto_increment not null,
    name varchar(50) not null,
    address1 varchar(50),
    address2 varchar(50),
    city varchar(30)
)
--rollback DROP TABLE person;

--changeset prayag.upa:2 labels:retail-store context:retail-store
--comment: add company
create table IF NOT EXISTS company (
    id int primary key auto_increment not null,
    name varchar(50) not null,
    address1 varchar(50),
    address2 varchar(50),
    city varchar(30)
)
--rollback DROP TABLE company;

--changeset prayag.upa:3 labels:retail-store context:retail-store
--comment: add country column

alter table person add column country varchar(2)
--rollback ALTER TABLE person DROP COLUMN country;

