CREATE TABLE arch_document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0),
  sourceObjectId NUMERIC(19, 0),
  documentName VARCHAR(50) NOT NULL,
  documentAuthor NUMERIC(19, 0),
  documentCreationDate NUMERIC(19, 0) NOT NULL,
  documentHasContent BIT NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(50),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, ID)
)
GO