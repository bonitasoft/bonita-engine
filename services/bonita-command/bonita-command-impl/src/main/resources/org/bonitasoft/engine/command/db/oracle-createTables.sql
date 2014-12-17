CREATE TABLE command (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  IMPLEMENTATION VARCHAR2(100 CHAR) NOT NULL,
  system NUMBER(1),
  CONSTRAINT UK_Command UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
