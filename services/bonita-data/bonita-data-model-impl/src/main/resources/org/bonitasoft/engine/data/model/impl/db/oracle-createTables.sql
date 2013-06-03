CREATE TABLE datasource (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  implementationclassname VARCHAR2(100) NOT NULL,
  state VARCHAR2(50) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datasource_name ON datasource (name);
CREATE INDEX idx_datasource_version ON datasource (version);

CREATE TABLE datasourceparameter (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  datasourceId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  value_ VARCHAR2(1024) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE datasourceparameter ADD CONSTRAINT fk_dsparam_dsid FOREIGN KEY (tenantid, datasourceid) REFERENCES datasource(tenantid, id) ON DELETE CASCADE;