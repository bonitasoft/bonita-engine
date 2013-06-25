CREATE TABLE category (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator NUMERIC(19, 0),
  description VARCHAR(MAX),
  creationDate NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE processcategorymapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  categoryid NUMERIC(19, 0) NOT NULL,
  processid NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
)
GO

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE
GO