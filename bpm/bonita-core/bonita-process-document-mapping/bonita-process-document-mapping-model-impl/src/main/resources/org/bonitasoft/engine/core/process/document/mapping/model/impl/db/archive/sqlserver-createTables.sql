CREATE TABLE arch_document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0),
  sourceObjectId NUMERIC(19, 0),
  documentName VARCHAR(50) NOT NULL,
  documentAuthor NUMERIC(19, 0) NOT NULL,
  documentCreationDate BIGINT NOT NULL,
  documentHasContent VARCHAR(1) NOT NULL,
  documentContentFileName VARCHAR(50) NOT NULL,
  documentContentMimeType VARCHAR(50),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(100),
  archiveDate BIGINT,
  PRIMARY KEY (tenantid, id)
)
GO
