CREATE TABLE process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255),
  startDate NUMERIC(19, 0) NOT NULL,
  startedBy NUMERIC(19, 0) NOT NULL,
  startedBySubstitute NUMERIC(19, 0) NOT NULL,
  endDate NUMERIC(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateCategory NVARCHAR(50) NOT NULL,
  lastUpdate NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0),
  rootProcessInstanceId NUMERIC(19, 0),
  callerId NUMERIC(19, 0),
  callerType NVARCHAR(50),
  interruptingEventId NUMERIC(19, 0),
  migration_plan NUMERIC(19, 0),
  stringIndex1 NVARCHAR(50),
  stringIndex2 NVARCHAR(50),
  stringIndex3 NVARCHAR(50),
  stringIndex4 NVARCHAR(50),
  stringIndex5 NVARCHAR(50),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE token (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processInstanceId NUMERIC(19, 0) NOT NULL,
  ref_id NUMERIC(19, 0) NOT NULL,
  parent_ref_id NUMERIC(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_token ON token(tenantid,processInstanceId)
GO

CREATE TABLE flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  flownodeDefinitionId NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(75),
  displayDescription NVARCHAR(255),
  stateId INT NOT NULL,
  stateName NVARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BIT NOT NULL,
  stable BIT ,
  actorId NUMERIC(19, 0) NULL,
  assigneeId NUMERIC(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  expectedEndDate NUMERIC(19, 0),
  claimedDate NUMERIC(19, 0),
  priority TINYINT,
  gatewayType NVARCHAR(50),
  hitBys NVARCHAR(255),
  stateCategory NVARCHAR(50) NOT NULL,
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description NVARCHAR(255),
  sequential BIT,
  loopDataInputRef NVARCHAR(255),
  loopDataOutputRef NVARCHAR(255),
  dataInputItemRef NVARCHAR(255),
  dataOutputItemRef NVARCHAR(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMERIC(19, 0),
  executedBySubstitute NUMERIC(19, 0),
  activityInstanceId NUMERIC(19, 0),
  state_executing BIT DEFAULT 0,
  abortedByBoundary NUMERIC(19, 0),
  triggeredByEvent BIT,
  interrupting BIT,
  deleted BIT DEFAULT 0,
  tokenCount INT NOT NULL,
  token_ref_id NUMERIC(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId)
GO
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4)
GO

CREATE TABLE connector_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0) NOT NULL,
  containerType NVARCHAR(10) NOT NULL,
  connectorId NVARCHAR(255) NOT NULL,
  version NVARCHAR(10) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  activationEvent NVARCHAR(30),
  state NVARCHAR(50),
  executionOrder INT,
  exceptionMessage NVARCHAR(255),
  stackTrace NVARCHAR(MAX),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent)
GO

CREATE TABLE event_trigger_instance (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	eventInstanceId NUMERIC(19, 0) NOT NULL,
  	kind NVARCHAR(15) NOT NULL,
  	timerType NVARCHAR(10),
  	timerValue NUMERIC(19, 0),
  	messageName NVARCHAR(255),
  	targetProcess NVARCHAR(255),
  	targetFlowNode NVARCHAR(255),
  	signalName NVARCHAR(255),
  	errorCode NVARCHAR(255),
  	PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE waiting_event (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	kind NVARCHAR(15) NOT NULL,
  	eventType NVARCHAR(50),
  	messageName NVARCHAR(255),
  	signalName NVARCHAR(255),
  	errorCode NVARCHAR(255),
  	processName NVARCHAR(150),
  	flowNodeName NVARCHAR(50),
  	flowNodeDefinitionId NUMERIC(19, 0),
  	subProcessId NUMERIC(19, 0),
  	processDefinitionId NUMERIC(19, 0),
  	rootProcessInstanceId NUMERIC(19, 0),
  	parentProcessInstanceId NUMERIC(19, 0),
  	flowNodeInstanceId NUMERIC(19, 0),
  	relatedActivityInstanceId NUMERIC(19, 0),
  	locked BIT,
  	active BIT,
  	progress TINYINT,
  	correlation1 NVARCHAR(128),
  	correlation2 NVARCHAR(128),
  	correlation3 NVARCHAR(128),
  	correlation4 NVARCHAR(128),
  	correlation5 NVARCHAR(128),
  	PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active)
GO

CREATE TABLE message_instance (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	messageName NVARCHAR(255) NOT NULL,
  	targetProcess NVARCHAR(255) NOT NULL,
  	targetFlowNode NVARCHAR(255) NULL,
  	locked BIT NOT NULL,
  	handled BIT NOT NULL,
  	processDefinitionId NUMERIC(19, 0) NOT NULL,
  	flowNodeName NVARCHAR(255),
  	correlation1 NVARCHAR(128),
  	correlation2 NVARCHAR(128),
  	correlation3 NVARCHAR(128),
  	correlation4 NVARCHAR(128),
  	correlation5 NVARCHAR(128),
  	PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3)
GO

CREATE TABLE pending_mapping (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	activityId NUMERIC(19, 0) NOT NULL,
  	actorId NUMERIC(19, 0),
  	userId NUMERIC(19, 0),
  	PRIMARY KEY (tenantid, id)
)
GO
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId)
GO

CREATE TABLE hidden_activity (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	activityId NUMERIC(19, 0) NOT NULL,
  	userId NUMERIC(19, 0) NOT NULL,
  	UNIQUE (tenantid, activityId, userId),
  	PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE breakpoint (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name NVARCHAR(255) NOT NULL,
  	inst_scope BIT NOT NULL,
  	inst_id NUMERIC(19, 0) NOT NULL,
  	def_id NUMERIC(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE ref_biz_data_inst (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	kind NVARCHAR(15) NOT NULL,
  	name NVARCHAR(255) NOT NULL,
  	proc_inst_id NUMERIC(19, 0),
  	fn_inst_id NUMERIC(19, 0),
  	data_id NUMERIC(19, 0),
  	data_classname NVARCHAR(255) NOT NULL
)
GO

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id)
GO

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id)
GO

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data PRIMARY KEY (tenantid, id)
GO

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data UNIQUE(name, proc_inst_id, fn_inst_id, tenantid )
GO

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE
GO
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE
GO

CREATE TABLE multi_biz_data (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	idx NUMERIC(19, 0) NOT NULL,
  	data_id NUMERIC(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
)
GO

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE
GO
