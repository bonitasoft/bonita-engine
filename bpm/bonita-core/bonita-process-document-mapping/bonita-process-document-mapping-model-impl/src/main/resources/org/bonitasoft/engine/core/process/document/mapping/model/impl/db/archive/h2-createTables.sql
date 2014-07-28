CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT,
  sourceObjectId BIGINT,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor BIGINT,
  documentCreationDate BIGINT NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, ID)
);