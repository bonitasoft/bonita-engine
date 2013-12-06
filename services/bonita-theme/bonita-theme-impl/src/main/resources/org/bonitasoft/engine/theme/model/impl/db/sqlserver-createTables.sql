CREATE TABLE theme (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  isDefault BIT NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  cssContent VARBINARY(MAX) NOT NULL,
  type NVARCHAR(50) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
)
GO