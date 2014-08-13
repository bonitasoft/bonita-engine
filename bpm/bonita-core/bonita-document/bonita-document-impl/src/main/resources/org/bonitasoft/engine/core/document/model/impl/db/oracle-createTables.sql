CREATE TABLE document_content (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  documentId VARCHAR2(50) NOT NULL,
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0),
  documentName VARCHAR2(50) NOT NULL,
  documentAuthor NUMBER(19, 0),
  documentCreationDate NUMBER(19, 0) NOT NULL,
  documentHasContent NUMBER(1)  NOT NULL,
  documentContentFileName VARCHAR2(255),
  documentContentMimeType VARCHAR2(255),
  contentStorageId VARCHAR2(50),
  documentURL VARCHAR2(255),
  PRIMARY KEY (tenantid, ID)
);