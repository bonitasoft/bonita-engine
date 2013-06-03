CREATE TABLE category (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator INT8,
  description TEXT,
  creationDate INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE processcategorymapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  categoryid INT8 NOT NULL,
  processid INT8 NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;