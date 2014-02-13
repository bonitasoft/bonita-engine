CREATE TABLE human (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    firstname VARCHAR2(80) NULL,
    lastname VARCHAR2(80) NULL,
    age INT,
    parent_id NUMBER(19, 0) DEFAULT NULL,
    car_id NUMBER(19, 0) DEFAULT NULL,
    discriminant VARCHAR2(10) NOT NULL,
    deleted NUMBER(1),
    PRIMARY KEY (tenantid, id)
);
CREATE TABLE car (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    brand VARCHAR2(80) NULL,
    PRIMARY KEY (tenantid, id)
);
