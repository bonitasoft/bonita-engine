CREATE TABLE group_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  parentPath VARCHAR2(255 CHAR),
  displayName VARCHAR2(75 CHAR),
  description VARCHAR2(1024 CHAR),
  iconName VARCHAR2(50 CHAR),
  iconPath VARCHAR2(50 CHAR),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE role (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  description VARCHAR2(1024 CHAR),
  iconName VARCHAR2(50 CHAR),
  iconPath VARCHAR2(50 CHAR),
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
  userName VARCHAR2(255 CHAR) NOT NULL,
  password VARCHAR2(60 CHAR),
  firstName VARCHAR2(255 CHAR),
  lastName VARCHAR2(255 CHAR),
  title VARCHAR2(50 CHAR),
  jobTitle VARCHAR2(255 CHAR),
  managerUserId NUMBER(19, 0),
  delegeeUserName VARCHAR2(50 CHAR),
  iconName VARCHAR2(50 CHAR),
  iconPath VARCHAR2(50 CHAR),
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
  email VARCHAR2(255 CHAR),
  phone VARCHAR2(50 CHAR),
  mobile VARCHAR2(50 CHAR),
  fax VARCHAR2(50 CHAR),
  building VARCHAR2(50 CHAR),
  room VARCHAR2(50 CHAR),
  address VARCHAR2(255 CHAR),
  zipCode VARCHAR2(50 CHAR),
  city VARCHAR2(50 CHAR),
  state VARCHAR2(50 CHAR),
  country VARCHAR2(50 CHAR),
  website VARCHAR2(50 CHAR),
  personal NUMBER(1) NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;


CREATE TABLE custom_usr_inf_def (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE custom_usr_inf_val (
  id NUMBER(19, 0) NOT NULL,
  tenantid NUMBER(19, 0) NOT NULL,
  definitionId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  value VARCHAR2(255 CHAR),
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
