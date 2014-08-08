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
  displayName VARCHAR(255),
  UNIQUE (tenantId, name, version),
  PRIMARY KEY (tenantId, id)
);

CREATE INDEX idx_app_name ON business_app (name, tenantid);

CREATE TABLE business_app_page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  name VARCHAR(255),
  UNIQUE (tenantId, applicationId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);

-- forein keys are create in bonita-persistence-db/postCreateStructure.sql