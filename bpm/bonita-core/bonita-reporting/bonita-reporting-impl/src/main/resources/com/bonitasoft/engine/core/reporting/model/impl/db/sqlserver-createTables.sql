CREATE TABLE report (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  screenshot VARBINARY(MAX),
  content VARBINARY(MAX),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)
GO
