CREATE TABLE arch_process_comment(
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(255) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
