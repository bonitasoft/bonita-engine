CREATE TABLE document (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  author INT8,
  creationdate INT8 NOT NULL,
  hascontent BOOLEAN NOT NULL,
  filename VARCHAR(255),
  mimetype VARCHAR(255),
  url VARCHAR(255),
  content BYTEA,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processinstanceid INT8 NOT NULL,
  documentid INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
ALTER TABLE document_mapping ADD CONSTRAINT fk_docmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;