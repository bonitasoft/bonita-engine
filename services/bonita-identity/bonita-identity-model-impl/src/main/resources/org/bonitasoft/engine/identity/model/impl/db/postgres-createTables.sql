CREATE TABLE group_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  parentPath VARCHAR(50),
  displayName VARCHAR(75),
  description TEXT,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE role (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_role_name ON role (tenantid, name);

CREATE TABLE user_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  enabled BOOLEAN NOT NULL,
  userName VARCHAR(50) NOT NULL,
  password VARCHAR(60),
  firstName VARCHAR(50),
  lastName VARCHAR(50),
  title VARCHAR(50),
  jobTitle VARCHAR(50),
  managerUserId INT8,
  delegeeUserName VARCHAR(50),
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  lastConnection INT8,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_user_name ON user_ (tenantid, userName);

CREATE TABLE user_contactinfo (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8 NOT NULL,
  email VARCHAR(50),
  phone VARCHAR(50),
  mobile VARCHAR(50),
  fax VARCHAR(50),
  building VARCHAR(50),
  room VARCHAR(50),
  address VARCHAR(50),
  zipCode VARCHAR(50),
  city VARCHAR(50),
  state VARCHAR(50),
  country VARCHAR(50),
  website VARCHAR(50),
  personal BOOLEAN NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal);


CREATE TABLE p_metadata_def (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_p_metadata_def_name ON p_metadata_def (name);

CREATE TABLE p_metadata_val (
  tenantid INT8 NOT NULL,
  metadataName VARCHAR(50) NOT NULL,
  userName VARCHAR(50) NOT NULL,
  value VARCHAR(50),
  PRIMARY KEY (tenantid, metadataName, userName)
);

CREATE TABLE user_membership (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  assignedBy INT8,
  assignedDate INT8,
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
);
