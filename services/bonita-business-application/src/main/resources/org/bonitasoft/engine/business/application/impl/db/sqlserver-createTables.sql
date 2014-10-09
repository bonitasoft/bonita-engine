CREATE TABLE business_app (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  path NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(255),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  updatedBy NUMERIC(19, 0) NOT NULL,
  state NVARCHAR(30) NOT NULL,
  homePageId NUMERIC(19, 0),
  profileId NUMERIC(19, 0),
  displayName NVARCHAR(255) NOT NULL
)
GO

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version)
GO

CREATE INDEX idx_app_name ON business_app (name, tenantid)
GO

CREATE TABLE business_app_page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  pageId NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(255) NOT NULL
)
GO

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token)
GO

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid)
GO
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid)
GO

CREATE TABLE business_app_menu (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  applicationPageId NUMERIC(19, 0),
  parentId NUMERIC(19, 0),
  index_ NUMERIC(19, 0)
)
GO

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id)
GO

CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid)
GO
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid)
GO

-- forein keys are create in bonita-persistence-db/postCreateStructure.sql