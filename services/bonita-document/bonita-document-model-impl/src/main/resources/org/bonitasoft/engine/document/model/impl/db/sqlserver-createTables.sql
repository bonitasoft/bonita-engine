CREATE TABLE document_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  documentId VARCHAR(50) NOT NULL,
  content IMAGE NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO