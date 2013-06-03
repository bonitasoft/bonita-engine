CREATE TABLE external_identity_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  externalId VARCHAR(50) NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);