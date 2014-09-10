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
  homePageId NUMBER(19, 0),
  displayName VARCHAR2(255) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version);

CREATE INDEX idx_app_name ON business_app (name, tenantid);

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_name UNIQUE (tenantId, applicationId, name);

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  displayName VARCHAR2(255) NOT NULL,
  applicationPageId NUMBER(19, 0),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0)
);

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);

CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

