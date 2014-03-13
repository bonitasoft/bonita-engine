CREATE TABLE page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided BOOLEAN,
  lastModificationDate BIGINT NOT NULL,
  content LONGBLOB,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
