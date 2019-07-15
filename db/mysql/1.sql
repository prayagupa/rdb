create database updupd;
use updupd;

create TABLE Inventory (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    warehouse VARCHAR(20),
    sku VARCHAR(20),
    qty int
);
-- ALTER TABLE Inventory ADD PRIMARY KEY(id);

insert into Inventory (warehouse, sku, qty) values('De Moines', 'sku-1', 88);
insert into Inventory (warehouse, sku, qty) values('De Moines', 'sku-2', 99);
insert into Inventory (warehouse, sku, qty) values('Seattle', 'sku-2', 99);
insert into Inventory (warehouse, sku, qty) values('Luyata', 'sku-3', 11);

create TABLE Config(
        config_id INTEGER AUTO_INCREMENT PRIMARY KEY,
        config_uuid VARCHAR(64) NOT NULL,
        a_id INTEGER NOT NULL,
        environment VARCHAR(32) NOT NULL,
        requested_on DATETIME NOT NULL,
        requested_by VARCHAR(64) NOT NULL,
        approved_on DATETIME,
        approved_by VARCHAR(64),
        rejected_on DATETIME,
        rejected_by VARCHAR(64),
        canceled_on DATETIME,
        canceled_by VARCHAR(64),
        created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
