CREATE TABLE business_app (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  path VARCHAR2(255) NOT NULL,
  description VARCHAR2(1024),
  iconPath VARCHAR2(255),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  updatedBy NUMBER(19, 0) NOT NULL,
  state VARCHAR2(30) NOT NULL,
  UNIQUE (tenantId, name, version),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255),
  UNIQUE (tenantId, applicationId, name),
  PRIMARY KEY (tenantId, id)
);

