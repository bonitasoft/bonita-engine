CREATE TABLE theme (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  isDefault BOOLEAN NOT NULL,
  content LONGBLOB NOT NULL,
  cssContent LONGBLOB NOT NULL,
  type VARCHAR(50) NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
)ENGINE = INNODB;