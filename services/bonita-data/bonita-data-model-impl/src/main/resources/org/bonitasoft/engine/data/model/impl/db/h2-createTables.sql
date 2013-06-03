CREATE TABLE datasource (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  implementationclassname VARCHAR(100) NOT NULL,
  state VARCHAR(50) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datasource_name ON datasource (name);
CREATE INDEX idx_datasource_version ON datasource (version);

CREATE TABLE datasourceparameter (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  datasourceId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  value_ LONGVARCHAR NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datasourceparameter_datasourceid ON datasourceparameter (datasourceid);
ALTER TABLE datasourceparameter ADD CONSTRAINT fk_dsparam_dsid FOREIGN KEY (tenantid, datasourceid) REFERENCES datasource(tenantid, id) ON DELETE CASCADE;