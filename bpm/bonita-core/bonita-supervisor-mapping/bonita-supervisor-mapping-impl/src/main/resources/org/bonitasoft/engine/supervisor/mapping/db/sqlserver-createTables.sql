CREATE TABLE processsupervisor (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processDefId NUMERIC(19, 0) NOT NULL,
  memberid NUMERIC(19, 0) NOT NULL,
  membertype VARCHAR(10) NOT NULL,
  UNIQUE (tenantid, processDefId, memberid, membertype),
  PRIMARY KEY (tenantid, id)
)
GO