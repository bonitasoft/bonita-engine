CREATE TABLE profile (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR2(1024),
  iconPath VARCHAR2(50),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR2(1024),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0),
  type VARCHAR2(50),
  page VARCHAR2(50),
  UNIQUE (tenantId, parentId, profileId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profilemember (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);