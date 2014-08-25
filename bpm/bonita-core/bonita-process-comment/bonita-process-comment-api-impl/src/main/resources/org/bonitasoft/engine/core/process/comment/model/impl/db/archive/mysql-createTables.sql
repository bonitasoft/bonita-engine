CREATE TABLE arch_process_comment(
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(255) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_arch_process_comment_on_tenant_and_sourceObjectId on arch_process_comment (tenantid, sourceobjectid);
CREATE INDEX idx2_arch_process_comment_on_tenant_and_procInst_and_archDate on arch_process_comment (tenantid, processinstanceid, archivedate);

