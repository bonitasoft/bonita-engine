CREATE TABLE group_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  parentPath VARCHAR2(255),
  displayName VARCHAR2(75),
  description VARCHAR2(1024),
  iconName VARCHAR2(50),
  iconPath VARCHAR2(50),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE role (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(75),
  description VARCHAR2(1024),
  iconName VARCHAR2(50),
  iconPath VARCHAR2(50),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  enabled NUMBER(1) NOT NULL,
  userName VARCHAR2(255) NOT NULL,
  password VARCHAR2(60),
  firstName VARCHAR2(255),
  lastName VARCHAR2(255),
  title VARCHAR2(50),
  jobTitle VARCHAR2(255),
  managerUserId NUMBER(19, 0),
  delegeeUserName VARCHAR2(50),
  iconName VARCHAR2(50),
  iconPath VARCHAR2(50),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  lastConnection NUMBER(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_contactinfo (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  email VARCHAR2(255),
  phone VARCHAR2(50),
  mobile VARCHAR2(50),
  fax VARCHAR2(50),
  building VARCHAR2(50),
  room VARCHAR2(50),
  address VARCHAR2(255),
  zipCode VARCHAR2(50),
  city VARCHAR2(50),
  state VARCHAR2(50),
  country VARCHAR2(50),
  website VARCHAR2(50),
  personal NUMBER(1) NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;


CREATE TABLE custom_usr_inf_def (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75) NOT NULL,
  description VARCHAR2(1024),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);

CREATE TABLE custom_usr_inf_val (
  id NUMBER(19, 0) NOT NULL,
  tenantid NUMBER(19, 0) NOT NULL,
  definitionId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  value VARCHAR2(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE;

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
);
