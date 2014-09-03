CREATE TABLE document (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  author BIGINT,
  creationdate BIGINT NOT NULL,
  hascontent BOOLEAN NOT NULL,
  filename VARCHAR(255),
  mimetype VARCHAR(255),
  url VARCHAR(255),
  content LONGBLOB NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) NOT NULL,
  PRIMARY KEY (tenantid, id)
);