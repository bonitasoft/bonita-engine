CREATE TABLE employee (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  laptopid BIGINT,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE saemployee (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  employeeid BIGINT NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE laptop (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(50) NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE address (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  address VARCHAR(50) NOT NULL,
  employeeid BIGINT NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE project (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE employeeprojectmapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  employeeid BIGINT NOT NULL,
  projectid BIGINT NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

