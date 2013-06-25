CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate NUMERIC(19, 0) NOT NULL,
  deployedBy NUMERIC(19, 0) NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  migrationDate NUMERIC(19, 0),
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate NUMERIC(19, 0),
  categoryId NUMERIC(19, 0),
  iconPath VARCHAR(255),
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
)
GO
