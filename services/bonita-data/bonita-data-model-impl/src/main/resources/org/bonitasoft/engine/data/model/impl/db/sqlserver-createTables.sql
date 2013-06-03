CREATE TABLE datasource (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  implementationclassname VARCHAR(100) NOT NULL,
  state VARCHAR(50) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_datasource_name ON datasource (name)
GO
CREATE INDEX idx_datasource_version ON datasource (version)
GO

CREATE TABLE datasourceparameter (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  datasourceid NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  value_ TEXT NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_datasourceparameter_datasourceid ON datasourceparameter (datasourceid)
GO
ALTER TABLE datasourceparameter ADD CONSTRAINT fk_dsparam_dsid FOREIGN KEY (tenantid, datasourceid) REFERENCES datasource(tenantid, id) ON DELETE CASCADE;
GO