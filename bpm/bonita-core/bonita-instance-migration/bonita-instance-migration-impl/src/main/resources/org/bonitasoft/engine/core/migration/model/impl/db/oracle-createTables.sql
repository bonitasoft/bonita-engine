CREATE TABLE migration_plan (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR) NOT NULL,
  source_name VARCHAR2(50 CHAR) NOT NULL,
  source_version VARCHAR2(50 CHAR) NOT NULL,
  target_name VARCHAR2(50 CHAR) NOT NULL,
  target_version VARCHAR2(50 CHAR) NOT NULL,
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
