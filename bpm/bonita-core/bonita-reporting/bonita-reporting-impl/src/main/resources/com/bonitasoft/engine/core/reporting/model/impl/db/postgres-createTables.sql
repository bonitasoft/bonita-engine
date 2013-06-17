CREATE TABLE report (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  installationDate INT8 NOT NULL,
  installedBy INT8 NOT NULL,
  provided BOOLEAN,
  lastModificationDate INT8 NOT NULL,
  screenshot BYTEA,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
