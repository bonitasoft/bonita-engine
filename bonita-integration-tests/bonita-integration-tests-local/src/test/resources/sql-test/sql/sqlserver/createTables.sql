CREATE TABLE p_employee (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  age INT NOT NULL,
  laptopid NUMERIC(19, 0),
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE saemployee (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  employeeid NUMERIC(19, 0) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE laptop (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  brand NVARCHAR(50) NOT NULL,
  model NVARCHAR(50) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE p_address (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  address NVARCHAR(50) NOT NULL,
  employeeid NUMERIC(19, 0) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE project (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE employeeprojectmapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  employeeid NUMERIC(19, 0) NOT NULL,
  projectid NUMERIC(19, 0) NOT NULL,
  archivedate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE human (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    firstname NVARCHAR(80) NULL,
    lastname NVARCHAR(80) NULL,
    age INT,
    parent_id NUMERIC(19, 0) DEFAULT NULL,
    car_id NUMERIC(19, 0) DEFAULT NULL,
    discriminant NVARCHAR(10) NOT NULL,
    PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE car (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    brand NVARCHAR(80) NULL,
    PRIMARY KEY (tenantid, id)
)
GO
