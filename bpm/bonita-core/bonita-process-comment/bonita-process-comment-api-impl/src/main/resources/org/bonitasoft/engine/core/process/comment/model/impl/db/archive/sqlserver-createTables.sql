CREATE TABLE arch_process_comment(
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(255) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_process_comment_on_tenant_and_sourceObjectId on arch_process_comment (tenantid, sourceobjectid)
GO
CREATE INDEX idx2_arch_process_comment_on_tenant_and_procInst_and_archDate on arch_process_comment (tenantid, processinstanceid, archivedate)
GO
