CREATE TABLE theme (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  content BLOB NOT NULL,
  cssContent BLOB,
  type VARCHAR2(50 CHAR) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);
