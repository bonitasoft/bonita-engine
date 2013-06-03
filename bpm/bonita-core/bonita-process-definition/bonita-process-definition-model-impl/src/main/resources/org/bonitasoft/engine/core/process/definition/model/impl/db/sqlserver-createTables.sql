CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description TEXT,
  deploymentDate NUMERIC(19, 0) NOT NULL,
  deployedBy NUMERIC(19, 0) NOT NULL,
  state VARCHAR(30) NOT NULL,
  migrationDate NUMERIC(19, 0),
  displayName VARCHAR(75),
  displayDescription TEXT,
  lastUpdateDate NUMERIC(19, 0),
  categoryId NUMERIC(19, 0),
  iconPath VARCHAR(255),
  PRIMARY KEY (tenantid, id)
);
