CREATE TABLE processsupervisor (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processDefId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
