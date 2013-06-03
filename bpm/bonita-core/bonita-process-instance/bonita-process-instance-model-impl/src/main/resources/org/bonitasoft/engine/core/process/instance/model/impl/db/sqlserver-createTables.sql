CREATE TABLE process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  startDate NUMERIC(19, 0) NOT NULL,
  startedBy NUMERIC(19, 0) NOT NULL,
  endDate NUMERIC(19, 0) NOT NULL,
  lastUpdate NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE activity_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processInstanceId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  stateId INT NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE hidden_activity (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	activityId NUMERIC(19, 0) NOT NULL,
  	userId NUMERIC(19, 0) NOT NULL,
  	UNIQUE (tenantid, id, activityId, userId),
  	PRIMARY KEY (tenantid, id)
)
GO

ALTER TABLE activity_instance ADD CONSTRAINT fk_act_inst_proc_inst_Id FOREIGN KEY (tenantId, processInstanceId) REFERENCES process_instance (tenantId, id)
GO
