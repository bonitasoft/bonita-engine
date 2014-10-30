CREATE TABLE arch_process_comment(
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8,
  processInstanceId INT8 NOT NULL,
  postDate INT8 NOT NULL,
  content VARCHAR(255) NOT NULL,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceObjectId, tenantid);;
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);
