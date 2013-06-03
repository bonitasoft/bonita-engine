CREATE TABLE processsupervisor (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processDefId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
