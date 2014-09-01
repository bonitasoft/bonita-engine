CREATE TABLE document (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  author BIGINT,
  creattiondate BIGINT NOT NULL,
  hascontent BOOLEAN NOT NULL,
  filename VARCHAR(255),
  mimetype VARCHAR(255),
  url VARCHAR(255),
  content LONGBLOB,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;

ALTER TABLE document_mapping ADD CONSTRAINT fk_docmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;