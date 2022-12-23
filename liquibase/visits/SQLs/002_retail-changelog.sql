--changeset prayag.upa:4 labels:retail-store context:retail-store
--comment: add state column
alter table person add column state varchar(2)
--rollback ALTER TABLE person DROP COLUMN state;

