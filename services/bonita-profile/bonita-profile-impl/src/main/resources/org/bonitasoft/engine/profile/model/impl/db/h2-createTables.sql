CREATE TABLE profile (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  iconPath VARCHAR(50),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  parentId BIGINT,
  index_ BIGINT,
  type VARCHAR(50),
  page VARCHAR(50),
  UNIQUE (tenantId, parentId, profileId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profilemember (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);