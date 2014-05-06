CREATE TABLE process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR2(50) NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0),
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  callerType VARCHAR2(50),
  interruptingEventId NUMBER(19, 0),
  migration_plan NUMBER(19, 0),
  stringIndex1 VARCHAR2(50),
  stringIndex2 VARCHAR2(50),
  stringIndex3 VARCHAR2(50),
  stringIndex4 VARCHAR2(50),
  stringIndex5 VARCHAR2(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE token (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processInstanceId NUMBER(19, 0) NOT NULL,
  ref_id NUMBER(19, 0) NOT NULL,
  parent_ref_id NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_token ON token(tenantid,processInstanceId);

CREATE TABLE flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(75),
  displayDescription VARCHAR2(255),
  stateId INT NOT NULL,
  stateName VARCHAR2(50),
  prev_state_id INT NOT NULL,
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  actorId NUMBER(19, 0) NULL,
  assigneeId NUMBER(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  expectedEndDate NUMBER(19, 0),
  claimedDate NUMBER(19, 0),
  priority SMALLINT,
  gatewayType VARCHAR2(50),
  hitBys VARCHAR2(255),
  stateCategory VARCHAR2(50) NOT NULL,
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR2(255),
  sequential NUMBER(1),
  loopDataInputRef VARCHAR2(255),
  loopDataOutputRef VARCHAR2(255),
  dataInputItemRef VARCHAR2(255),
  dataOutputItemRef VARCHAR2(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMBER(19, 0),
  executedBySubstitute NUMBER(19, 0),
  activityInstanceId NUMBER(19, 0),
  state_executing NUMBER(1) DEFAULT 0,
  abortedByBoundary NUMBER(19, 0),
  triggeredByEvent NUMBER(1),
  interrupting NUMBER(1),
  deleted NUMBER(1) DEFAULT 0,
  tokenCount INT NOT NULL,
  token_ref_id NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);

CREATE TABLE connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10) NOT NULL,
  connectorId VARCHAR2(255) NOT NULL,
  version VARCHAR2(10) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  activationEvent VARCHAR2(30),
  state VARCHAR2(50),
  executionOrder INT,
  exceptionMessage VARCHAR2(255),
  stackTrace CLOB,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	eventInstanceId NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15) NOT NULL,
  	timerType VARCHAR2(10),
  	timerValue NUMBER(19, 0),
  	messageName VARCHAR2(255),
  	targetProcess VARCHAR2(255),
  	targetFlowNode VARCHAR2(255),
  	signalName VARCHAR2(255),
  	errorCode VARCHAR2(255),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE waiting_event (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15) NOT NULL,
  	eventType VARCHAR2(50),
  	messageName VARCHAR2(255),
  	signalName VARCHAR2(255),
  	errorCode VARCHAR2(255),
  	processName VARCHAR2(150),
  	flowNodeName VARCHAR2(50),
  	flowNodeDefinitionId NUMBER(19, 0),
  	subProcessId NUMBER(19, 0),
  	processDefinitionId NUMBER(19, 0),
  	rootProcessInstanceId NUMBER(19, 0),
  	parentProcessInstanceId NUMBER(19, 0),
  	flowNodeInstanceId NUMBER(19, 0),
  	relatedActivityInstanceId NUMBER(19, 0),
  	locked NUMBER(1),
  	active NUMBER(1),
  	progress SMALLINT,
  	correlation1 VARCHAR2(128),
  	correlation2 VARCHAR2(128),
  	correlation3 VARCHAR2(128),
  	correlation4 VARCHAR2(128),
  	correlation5 VARCHAR2(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);

CREATE TABLE message_instance (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	messageName VARCHAR2(255) NOT NULL,
  	targetProcess VARCHAR2(255) NOT NULL,
  	targetFlowNode VARCHAR2(255) NULL,
  	locked NUMBER(1) NOT NULL,
  	handled NUMBER(1) NOT NULL,
  	processDefinitionId NUMBER(19, 0) NOT NULL,
  	flowNodeName VARCHAR2(255),
  	correlation1 VARCHAR2(128),
  	correlation2 VARCHAR2(128),
  	correlation3 VARCHAR2(128),
  	correlation4 VARCHAR2(128),
  	correlation5 VARCHAR2(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);

CREATE TABLE pending_mapping (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	activityId NUMBER(19, 0) NOT NULL,
  	actorId NUMBER(19, 0),
  	userId NUMBER(19, 0),
  	PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE hidden_activity (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	activityId NUMBER(19, 0) NOT NULL,
  	userId NUMBER(19, 0) NOT NULL,
  	UNIQUE (tenantid, activityId, userId),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE breakpoint (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR2(255) NOT NULL,
  	inst_scope NUMBER(1) NOT NULL,
  	inst_id NUMBER(19, 0) NOT NULL,
  	def_id NUMBER(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE ref_biz_data_inst (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	name VARCHAR2(255) NOT NULL,
  	proc_inst_id NUMBER(19, 0) NOT NULL,
  	data_id INT NULL,
  	data_classname VARCHAR2(255) NOT NULL,
  	UNIQUE (tenantid, proc_inst_id, name),
  	PRIMARY KEY (tenantid, id)
);
