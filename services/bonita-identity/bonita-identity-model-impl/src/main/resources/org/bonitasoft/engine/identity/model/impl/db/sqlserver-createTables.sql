CREATE TABLE group_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  parentPath VARCHAR(50) NULL,
  displayName VARCHAR(75) NULL,
  description TEXT NULL,
  iconName VARCHAR(50) NULL,
  iconPath VARCHAR(50) NULL,
  createdBy NUMERIC(19, 0) NULL,
  creationDate NUMERIC(19, 0) NULL,
  lastUpdate NUMERIC(19, 0) NULL,
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE role (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75) NULL,
  description TEXT NULL,
  iconName VARCHAR(50) NULL,
  iconPath VARCHAR(50) NULL,
  createdBy NUMERIC(19, 0) NULL,
  creationDate NUMERIC(19, 0) NULL,
  lastUpdate NUMERIC(19, 0) NULL,  
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_role_name ON role (name)
GO

CREATE TABLE user_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userName VARCHAR(50) NOT NULL,
  password VARCHAR(60) NULL,
  firstName VARCHAR(50) NULL,
  lastName VARCHAR(50) NULL,
  title VARCHAR(50) NULL,
  jobTitle VARCHAR(50) NULL,
  managerUserName VARCHAR(50) NULL,
  delegeeUserName VARCHAR(50) NULL,
  iconName VARCHAR(50) NULL,
  iconPath VARCHAR(50) NULL,
  createdBy NUMERIC(19, 0) NULL,
  creationDate NUMERIC(19, 0) NULL,
  lastUpdate NUMERIC(19, 0) NULL, 
  lastConnection NUMERIC(19, 0) NULL,
  proEmail VARCHAR(50) NULL,
  proPhone VARCHAR(50) NULL,
  proMobile VARCHAR(50) NULL,
  proFax VARCHAR(50) NULL,
  proBuilding VARCHAR(50) NULL,
  proRoom VARCHAR(50) NULL,
  proAddress VARCHAR(50) NULL,
  proZipCode VARCHAR(50) NULL,
  proCity VARCHAR(50) NULL,
  proState VARCHAR(50) NULL,
  proCountry VARCHAR(50) NULL,
  proWebsite VARCHAR(50) NULL,
  persoEmail VARCHAR(50) NULL,
  persoPhone VARCHAR(50) NULL,
  persoMobile VARCHAR(50) NULL,
  persoFax VARCHAR(50) NULL,
  persoBuilding VARCHAR(50) NULL,
  persoRoom VARCHAR(50) NULL,
  persoAddress VARCHAR(50) NULL,
  persoZipCode VARCHAR(50) NULL,
  persoCity VARCHAR(50) NULL,
  persoState VARCHAR(50) NULL,
  persoCountry VARCHAR(50) NULL,
  persoWebsite VARCHAR(50) NULL,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_user_name ON user_ (tenantid, userName);
GO

CREATE TABLE p_metadata_def (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  displayName VARCHAR(75) NULL,
  description TEXT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_p_metadata_def_name ON p_metadata_def (name)
GO

CREATE TABLE p_metadata_val (
  tenantid NUMERIC(19, 0) NOT NULL,
  metadataName VARCHAR(50) NOT NULL,
  userName VARCHAR(50) NOT NULL,
  value VARCHAR(50) NULL,
  PRIMARY KEY (tenantid, metadataName, userName)
)
GO

ALTER TABLE p_metadata_val ADD CONSTRAINT fk_p_md_val_mdId FOREIGN KEY (tenantid, metadataName) REFERENCES p_metadata_def(tenantid, name)
GO
ALTER TABLE p_metadata_val ADD CONSTRAINT fk_p_md_val_usrId FOREIGN KEY (tenantid, userName) REFERENCES user_(tenantid, userName)
GO


CREATE TABLE user_membership (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  assignedBy NUMBER(19, 0),
  assignedDate NUMBER(19, 0),
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
)
GO

ALTER TABLE user_membership ADD CONSTRAINT fk_usr_mbshp_rId FOREIGN KEY (tenantid, roleId) REFERENCES user_(tenantid, id) ON UPDATE CASCADE
GO
ALTER TABLE user_membership ADD CONSTRAINT fk_usr_mbshp_gId FOREIGN KEY (tenantid, groupId) REFERENCES user_(tenantid, id) ON UPDATE CASCADE
GO
ALTER TABLE user_membership ADD CONSTRAINT fk_usr_mbshp_rId FOREIGN KEY (tenantid, userId) REFERENCES user_(tenantid, id) ON UPDATE CASCADE
GO


