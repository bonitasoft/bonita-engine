CREATE TABLE arch_document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(10 CHAR) NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);