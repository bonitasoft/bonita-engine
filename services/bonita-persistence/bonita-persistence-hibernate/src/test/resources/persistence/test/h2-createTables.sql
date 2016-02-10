CREATE TABLE human (
    tenantid BIGINT NOT NULL,
    id BIGINT NOT NULL,
    firstname VARCHAR(80) NULL,
    lastname VARCHAR(80) NULL,
    age INT,
    parent_id BIGINT DEFAULT NULL,
    car_id BIGINT DEFAULT NULL,
    discriminant VARCHAR(10) NOT NULL,
    PRIMARY KEY (tenantid, id)
);
CREATE TABLE car (
    tenantid BIGINT NOT NULL,
    id BIGINT NOT NULL,
    brand VARCHAR(80) NULL,
    PRIMARY KEY (tenantid, id)
);
