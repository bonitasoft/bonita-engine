CREATE TABLE actor (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  description VARCHAR2(1024 CHAR),
  initiator NUMBER(1),
  CONSTRAINT UK_Actor UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE actormember (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  actorId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
