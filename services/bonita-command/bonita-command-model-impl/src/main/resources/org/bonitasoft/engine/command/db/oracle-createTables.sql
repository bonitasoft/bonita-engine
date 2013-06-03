CREATE TABLE command (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR2(1024),
  IMPLEMENTATION VARCHAR2(100) NOT NULL,
  system NUMBER(1) ,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
