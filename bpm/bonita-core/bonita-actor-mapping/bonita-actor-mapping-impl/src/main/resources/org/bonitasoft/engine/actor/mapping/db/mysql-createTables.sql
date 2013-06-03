CREATE TABLE actor (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  initiator BOOLEAN,
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE actormember (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  actorId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
