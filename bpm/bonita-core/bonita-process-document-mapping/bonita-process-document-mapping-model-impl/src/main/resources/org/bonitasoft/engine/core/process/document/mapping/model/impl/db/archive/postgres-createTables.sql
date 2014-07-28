CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processinstanceid INT8,
  sourceObjectId INT8,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor INT8,
  documentCreationDate INT8 NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);