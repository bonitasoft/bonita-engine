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

CREATE INDEX idx_role_name ON role (tenantid, name)
GO

CREATE TABLE user_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  enabled BIT NOT NULL,
  userName NVARCHAR(255) NOT NULL,
  password NVARCHAR(60),
  firstName NVARCHAR(255),
  lastName NVARCHAR(255),
  title NVARCHAR(50),
  jobTitle NVARCHAR(255),
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

CREATE INDEX idx_user_name ON user_ (tenantid, userName)
GO

CREATE TABLE user_contactinfo (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  email NVARCHAR(255),
  phone NVARCHAR(50),
  mobile NVARCHAR(50),
  fax NVARCHAR(50),
  building NVARCHAR(50),
  room NVARCHAR(50),
  address NVARCHAR(255),
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


CREATE TABLE custom_usr_inf_def (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  description NVARCHAR(MAX),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name)
GO

CREATE TABLE custom_usr_inf_val (
  id NUMERIC(19, 0) NOT NULL,
  tenantid NUMERIC(19, 0) NOT NULL,
  definitionId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  value NVARCHAR(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
)
GO
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE
GO
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE
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
