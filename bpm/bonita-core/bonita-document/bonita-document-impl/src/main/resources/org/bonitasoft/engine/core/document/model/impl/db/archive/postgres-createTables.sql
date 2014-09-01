CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  sourceObjectId INT8,
  processinstanceid INT8 NOT NULL,
  documentid INT8 NOT NULL,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);