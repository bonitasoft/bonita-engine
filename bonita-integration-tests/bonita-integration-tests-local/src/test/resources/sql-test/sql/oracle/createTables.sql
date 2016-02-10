CREATE TABLE p_employee (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  age INT NOT NULL,
  laptopid NUMBER(19, 0),
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE saemployee (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  employeeid NUMBER(19, 0) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE laptop (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  brand VARCHAR2(50 CHAR) NOT NULL,
  model VARCHAR2(50 CHAR) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE p_address (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  address VARCHAR2(50 CHAR) NOT NULL,
  employeeid NUMBER(19, 0) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE project (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE employeeprojectmapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  employeeid NUMBER(19, 0) NOT NULL,
  projectid NUMBER(19, 0) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE human (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    firstname VARCHAR2(80) NULL,
    lastname VARCHAR2(80) NULL,
    age INT,
    parent_id NUMBER(19, 0) DEFAULT NULL,
    car_id NUMBER(19, 0) DEFAULT NULL,
    discriminant VARCHAR2(10) NOT NULL,
    PRIMARY KEY (tenantid, id)
);
CREATE TABLE car (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    brand VARCHAR2(80) NULL,
    PRIMARY KEY (tenantid, id)
);
