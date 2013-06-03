CREATE TABLE arch_process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NULL,
  endDate NUMBER(19, 0) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE arch_activity_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processInstanceId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  stateId INT NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  description TEXT,
  PRIMARY KEY (tenantid, id)
)
GO

ALTER TABLE arch_activity_instance ADD CONSTRAINT fk_act_inst_proc_inst_Id FOREIGN KEY (tenantId, processInstanceId) REFERENCES arch_process_instance (tenantId, id)
GO
