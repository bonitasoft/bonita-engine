CREATE TABLE document (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  author NUMBER(19, 0),
  creationdate NUMBER(19, 0) NOT NULL,
  hascontent NUMBER(1)  NOT NULL,
  filename VARCHAR2(255),
  mimetype VARCHAR2(255),
  url VARCHAR2(1024),
  content BLOB,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR2(1024),
  version VARCHAR2(10) NOT NULL,
  PRIMARY KEY (tenantid, ID)
);