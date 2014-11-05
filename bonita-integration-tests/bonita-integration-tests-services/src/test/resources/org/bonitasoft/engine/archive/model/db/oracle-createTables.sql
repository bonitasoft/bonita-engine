CREATE TABLE employee (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
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
  brand VARCHAR2(50) NOT NULL,
  model VARCHAR2(50) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE address (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  address VARCHAR2(50) NOT NULL,
  employeeid NUMBER(19, 0) NOT NULL,
  archivedate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE project (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
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

