CREATE TABLE profile (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(50),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)
GO

CREATE TABLE profileentry (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  profileId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  parentId NUMERIC(19, 0),
  index_ NUMERIC(19, 0),
  type NVARCHAR(50),
  page NVARCHAR(50),
  UNIQUE (tenantId, parentId, profileId, name),
  PRIMARY KEY (tenantId, id)
)
GO

CREATE TABLE profilemember (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  profileId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
)
GO
