CREATE TABLE human (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    firstname VARCHAR(80) NULL,
    lastname VARCHAR(80) NULL,
    age INT,
    parent_id NUMERIC(19, 0) DEFAULT NULL,
    car_id NUMERIC(19, 0) DEFAULT NULL,
    discriminant VARCHAR(10) NOT NULL,
    deleted BIT,
    PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE car (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    brand VARCHAR(80) NULL,
    PRIMARY KEY (tenantid, id)
)
GO
