CREATE TABLE document_content (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  documentId VARCHAR2(50) NOT NULL,
  content BLOB,
  PRIMARY KEY (tenantid, id)
);