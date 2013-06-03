CREATE TABLE human (
    tenantid INT8 NOT NULL,
    id INT8 NOT NULL,
    firstname VARCHAR(80) NULL,
    lastname VARCHAR(80) NULL,
    age INT,
    parent_id INT8 DEFAULT NULL,
    car_id INT8 DEFAULT NULL,
    discriminant VARCHAR(10) NOT NULL,
    deleted BOOLEAN,
    PRIMARY KEY (tenantid, id)
);
CREATE TABLE car (
    tenantid INT8 NOT NULL,
    id INT8 NOT NULL,
    brand VARCHAR(80) NULL,
    PRIMARY KEY (tenantid, id)
);
