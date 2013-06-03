CREATE TABLE external_identity_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  externalId VARCHAR(50) NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;