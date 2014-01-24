CREATE TABLE group_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  parentPath NVARCHAR(255),
  displayName NVARCHAR(75),
  description NVARCHAR(MAX),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE role (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(75),
  description NVARCHAR(MAX),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_role_name ON role (tenantid, name, id)
GO

CREATE TABLE user_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  enabled BIT NOT NULL,
  userName NVARCHAR(50) NOT NULL,
  password NVARCHAR(60),
  firstName NVARCHAR(50),
  lastName NVARCHAR(50),
  title NVARCHAR(50),
  jobTitle NVARCHAR(50),
  managerUserId NUMERIC(19, 0),
  delegeeUserName NVARCHAR(50),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  lastConnection NUMERIC(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_user_name ON user_ (tenantid, userName, id)
GO

CREATE TABLE user_contactinfo (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  email NVARCHAR(50),
  phone NVARCHAR(50),
  mobile NVARCHAR(50),
  fax NVARCHAR(50),
  building NVARCHAR(50),
  room NVARCHAR(50),
  address NVARCHAR(50),
  zipCode NVARCHAR(50),
  city NVARCHAR(50),
  state NVARCHAR(50),
  country NVARCHAR(50),
  website NVARCHAR(50),
  personal BIT NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
)
GO
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE
GO
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal)
GO


CREATE TABLE p_metadata_def (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(75),
  description NVARCHAR(MAX),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_p_metadata_def_name ON p_metadata_def (name, id)
GO

CREATE TABLE p_metadata_val (
  tenantid NUMERIC(19, 0) NOT NULL,
  metadataName NVARCHAR(50) NOT NULL,
  userName NVARCHAR(50) NOT NULL,
  value NVARCHAR(50),
  PRIMARY KEY (tenantid, metadataName, userName)
)
GO

CREATE TABLE user_membership (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  assignedBy NUMERIC(19, 0),
  assignedDate NUMERIC(19, 0),
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
)
GO
