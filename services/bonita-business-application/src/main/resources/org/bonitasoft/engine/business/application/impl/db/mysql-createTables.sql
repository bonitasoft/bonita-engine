CREATE TABLE business_app (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  path VARCHAR(255) NOT NULL,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  updatedBy BIGINT NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId BIGINT,
  displayName VARCHAR(255)
) ENGINE = INNODB;

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version);

CREATE INDEX idx_app_name ON business_app (name, tenantid);

CREATE TABLE business_app_page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  applicationId BIGINT NOT NULL,
  pageId BIGINT NOT NULL,
  name VARCHAR(255)
) ENGINE = INNODB;

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_name UNIQUE (tenantId, applicationId, name);

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

-- forein keys are create in bonita-persistence-db/postCreateStructure.sql