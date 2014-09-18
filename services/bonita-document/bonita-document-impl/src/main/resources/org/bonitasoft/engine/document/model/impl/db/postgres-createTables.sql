CREATE TABLE document_content (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  documentId VARCHAR(50) NOT NULL,
  content BYTEA,
  PRIMARY KEY (tenantid, id)
);