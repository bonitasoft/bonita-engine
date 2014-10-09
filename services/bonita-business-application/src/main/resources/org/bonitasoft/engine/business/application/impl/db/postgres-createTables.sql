CREATE TABLE business_app (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  path VARCHAR(255) NOT NULL,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  updatedBy INT8 NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId INT8,
  profileId INT8,
  displayName VARCHAR(255) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version);

CREATE INDEX idx_app_name ON business_app (name, tenantid);

CREATE TABLE business_app_page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  token VARCHAR(255) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  applicationPageId INT8,
  parentId INT8,
  index_ INT8
);

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);

CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

-- forein keys are create in bonita-persistence-db/postCreateStructure.sql