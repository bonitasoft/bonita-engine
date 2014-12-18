CREATE TABLE document (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  author NUMBER(19, 0),
  creationdate NUMBER(19, 0) NOT NULL,
  hascontent NUMBER(1) NOT NULL,
  filename VARCHAR2(255 CHAR),
  mimetype VARCHAR2(255 CHAR),
  url VARCHAR2(1024 CHAR),
  content BLOB,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(10 CHAR) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);