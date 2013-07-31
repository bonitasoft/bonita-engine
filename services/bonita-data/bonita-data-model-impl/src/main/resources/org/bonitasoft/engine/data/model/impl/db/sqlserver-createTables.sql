CREATE TABLE datasource (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  implementationclassname NVARCHAR(100) NOT NULL,
  state NVARCHAR(50) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_datasource_name ON datasource (name, id)
GO
CREATE INDEX idx_datasource_version ON datasource (version, id)
GO

CREATE TABLE datasourceparameter (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  datasourceId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  value_ NVARCHAR(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_datasourceparameter_datasourceid ON datasourceparameter (datasourceid, id)
GO
ALTER TABLE datasourceparameter ADD CONSTRAINT fk_dsparam_dsid FOREIGN KEY (tenantid, datasourceid) REFERENCES datasource(tenantid, id) ON DELETE CASCADE
GO
