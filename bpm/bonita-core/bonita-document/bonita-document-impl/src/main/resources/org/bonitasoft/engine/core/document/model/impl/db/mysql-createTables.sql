CREATE TABLE document (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  author BIGINT,
  creationdate BIGINT NOT NULL,
  hascontent BOOLEAN NOT NULL,
  filename VARCHAR(255),
  mimetype VARCHAR(255),
  url VARCHAR(1024),
  content LONGBLOB,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;