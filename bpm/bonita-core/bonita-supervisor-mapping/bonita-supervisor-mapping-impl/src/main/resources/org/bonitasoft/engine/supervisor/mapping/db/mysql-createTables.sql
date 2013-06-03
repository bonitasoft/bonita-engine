CREATE TABLE processsupervisor (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processDefId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
