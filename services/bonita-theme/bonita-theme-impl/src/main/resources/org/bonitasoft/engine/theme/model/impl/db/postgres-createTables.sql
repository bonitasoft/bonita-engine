CREATE TABLE theme (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  isDefault BOOLEAN NOT NULL,
  content BYTEA NOT NULL,
  cssContent BYTEA,
  type VARCHAR(50) NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);
