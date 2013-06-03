CREATE TABLE category (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator VARCHAR(50) NULL,
  description TEXT NULL,
  creationDate NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name)
)
GO
CREATE TABLE processcategorymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  categoryid NUMBER(19, 0) NOT NULL,
  processid NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
)
GO
ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;
GO