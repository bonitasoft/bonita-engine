CREATE TABLE document_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  documentId NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX),
  PRIMARY KEY (tenantid, id)
)
GO