CREATE TABLE p_employee (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT NOT NULL,
  laptopid BIGINT,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE saemployee (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  employeeid BIGINT NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE laptop (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(50) NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE p_address (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  address VARCHAR(50) NOT NULL,
  employeeid BIGINT NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE project (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE employeeprojectmapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  employeeid BIGINT NOT NULL,
  projectid BIGINT NOT NULL,
  archivedate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

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
) ENGINE = INNODB;
CREATE TABLE car (
    tenantid BIGINT NOT NULL,
    id BIGINT NOT NULL,
    brand VARCHAR(80) NULL,
    PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
