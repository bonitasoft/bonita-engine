CREATE TABLE document (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  author NUMBER(19, 0),
  creationdate NUMBER(19, 0) NOT NULL,
  hascontent NUMBER(1)  NOT NULL,
  filename VARCHAR2(255),
  mimetype VARCHAR2(255),
  url VARCHAR2(255),
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
ALTER TABLE document_mapping ADD CONSTRAINT fk_docmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;
