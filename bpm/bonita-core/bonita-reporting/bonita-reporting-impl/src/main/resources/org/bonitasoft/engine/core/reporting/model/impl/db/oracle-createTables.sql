CREATE TABLE report (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  screenshot BLOB,
  content BLOB,
  CONSTRAINT UK_Report UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
