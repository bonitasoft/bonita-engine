CREATE TABLE profile (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  isDefault BOOLEAN NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  profileId INT8 NOT NULL,
  name VARCHAR(50),
  description TEXT,
  parentId INT8,
  index_ INT8,
  type VARCHAR(50),
  page VARCHAR(50),
  custom BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (tenantId, id)
);

CREATE INDEX indexProfileEntry ON profileentry (tenantId, parentId, profileId);

CREATE TABLE profilemember (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  profileId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);