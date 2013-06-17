CREATE TABLE report (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR22(1024),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1) ,
  lastModificationDate NUMBER(19, 0) NOT NULL,
  screenshot BLOB,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
