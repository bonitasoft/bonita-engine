CREATE TABLE p_employee (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  laptopid INT8,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE saemployee (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  employeeid INT8 NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE laptop (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(50) NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE p_address (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  address VARCHAR(50) NOT NULL,
  employeeid INT8 NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE project (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE employeeprojectmapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  employeeid INT8 NOT NULL,
  projectid INT8 NOT NULL,
  archivedate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE human (
    tenantid INT8 NOT NULL,
    id INT8 NOT NULL,
    firstname VARCHAR(80) NULL,
    lastname VARCHAR(80) NULL,
    age INT,
    parent_id INT8 DEFAULT NULL,
    car_id INT8 DEFAULT NULL,
    discriminant VARCHAR(10) NOT NULL,
    PRIMARY KEY (tenantid, id)
);
CREATE TABLE car (
    tenantid INT8 NOT NULL,
    id INT8 NOT NULL,
    brand VARCHAR(80) NULL,
    PRIMARY KEY (tenantid, id)
);
