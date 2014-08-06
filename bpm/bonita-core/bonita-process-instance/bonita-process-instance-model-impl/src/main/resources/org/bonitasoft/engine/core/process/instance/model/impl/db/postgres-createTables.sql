CREATE TABLE process_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedBySubstitute INT8 NOT NULL,
  endDate INT8 NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(50) NOT NULL,
  lastUpdate INT8 NOT NULL,
  containerId INT8,
  rootProcessInstanceId INT8,
  callerId INT8,
  callerType VARCHAR(50),
  interruptingEventId INT8,
  migration_plan INT8,
  stringIndex1 VARCHAR(50),
  stringIndex2 VARCHAR(50),
  stringIndex3 VARCHAR(50),
  stringIndex4 VARCHAR(50),
  stringIndex5 VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE token (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processInstanceId INT8 NOT NULL,
  ref_id INT8 NOT NULL,
  parent_ref_id INT8 NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_token ON token(tenantid,processInstanceId);

CREATE TABLE flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId INT8 NULL,
  assigneeId INT8 DEFAULT 0 NOT NULL,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR(255),
  sequential BOOLEAN,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy INT8,
  executedBySubstitute INT8,
  activityInstanceId INT8,
  state_executing BOOLEAN DEFAULT FALSE,
  abortedByBoundary INT8,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  deleted BOOLEAN DEFAULT FALSE,
  tokenCount INT NOT NULL,
  token_ref_id INT8 NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);

CREATE TABLE connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  executionOrder INT,
  exceptionMessage VARCHAR(255),
  stackTrace TEXT,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	eventInstanceId INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	timerType VARCHAR(10),
  	timerValue INT8,
  	messageName VARCHAR(255),
  	targetProcess VARCHAR(255),
  	targetFlowNode VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE waiting_event (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventType VARCHAR(50),
  	messageName VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	processName VARCHAR(150),
  	flowNodeName VARCHAR(50),
  	flowNodeDefinitionId INT8,
  	subProcessId INT8,
  	processDefinitionId INT8,
  	rootProcessInstanceId INT8,
  	parentProcessInstanceId INT8,
  	flowNodeInstanceId INT8,
  	relatedActivityInstanceId INT8,
  	locked BOOLEAN,
  	active BOOLEAN,
  	progress SMALLINT,
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);

CREATE TABLE message_instance (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	messageName VARCHAR(255) NOT NULL,
  	targetProcess VARCHAR(255) NOT NULL,
  	targetFlowNode VARCHAR(255) NULL,
  	locked BOOLEAN NOT NULL,
  	handled BOOLEAN NOT NULL,
  	processDefinitionId INT8 NOT NULL,
  	flowNodeName VARCHAR(255),
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);

CREATE TABLE pending_mapping (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	activityId INT8 NOT NULL,
  	actorId INT8,
  	userId INT8,
  	PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE hidden_activity (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	activityId INT8 NOT NULL,
  	userId INT8 NOT NULL,
  	UNIQUE (tenantid, activityId, userId),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE breakpoint (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR(255) NOT NULL,
  	inst_scope BOOLEAN NOT NULL,
  	inst_id INT8 NOT NULL,
  	def_id INT8 NOT NULL,
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE ref_biz_data_inst (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id INT8,
  	fn_inst_id INT8,
  	data_id INT,
  	data_classname VARCHAR(255) NOT NULL,
  	PRIMARY KEY (tenantid, id)
);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uniq_ref_biz_data_proc UNIQUE (tenantid, proc_inst_id, name);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uniq_ref_biz_data_fn UNIQUE (tenantid, fn_inst_id, name);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE multi_biz_data (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	idx INT8 NOT NULL,
  	data_id INT8 NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
);

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;
