CREATE TABLE process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR2(50 CHAR) NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0),
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  callerType VARCHAR2(50 CHAR),
  interruptingEventId NUMBER(19, 0),
  migration_plan NUMBER(19, 0),
  stringIndex1 VARCHAR2(50 CHAR),
  stringIndex2 VARCHAR2(50 CHAR),
  stringIndex3 VARCHAR2(50 CHAR),
  stringIndex4 VARCHAR2(50 CHAR),
  stringIndex5 VARCHAR2(50 CHAR),
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
  kind VARCHAR2(25 CHAR) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  stateId INT NOT NULL,
  stateName VARCHAR2(50 CHAR),
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
  gatewayType VARCHAR2(50 CHAR),
  hitBys VARCHAR2(255 CHAR),
  stateCategory VARCHAR2(50 CHAR) NOT NULL,
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR2(255 CHAR),
  sequential NUMBER(1),
  loopDataInputRef VARCHAR2(255 CHAR),
  loopDataOutputRef VARCHAR2(255 CHAR),
  dataInputItemRef VARCHAR2(255 CHAR),
  dataOutputItemRef VARCHAR2(255 CHAR),
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
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid, deleted);

CREATE TABLE connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(10 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  executionOrder INT,
  exceptionMessage VARCHAR2(255 CHAR),
  stackTrace CLOB,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	eventInstanceId NUMBER(19, 0) NOT NULL,
  	eventInstanceName VARCHAR2(50 CHAR),
  	messageName VARCHAR2(255 CHAR),
  	targetProcess VARCHAR2(255 CHAR),
  	targetFlowNode VARCHAR2(255 CHAR),
  	signalName VARCHAR2(255 CHAR),
  	errorCode VARCHAR2(255 CHAR),
  	executionDate NUMBER(19, 0), 
  	jobTriggerName VARCHAR2(255 CHAR),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE waiting_event (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	eventType VARCHAR2(50 CHAR),
  	messageName VARCHAR2(255 CHAR),
  	signalName VARCHAR2(255 CHAR),
  	errorCode VARCHAR2(255 CHAR),
  	processName VARCHAR2(150 CHAR),
  	flowNodeName VARCHAR2(50 CHAR),
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
  	correlation1 VARCHAR2(128 CHAR),
  	correlation2 VARCHAR2(128 CHAR),
  	correlation3 VARCHAR2(128 CHAR),
  	correlation4 VARCHAR2(128 CHAR),
  	correlation5 VARCHAR2(128 CHAR),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);

CREATE TABLE message_instance (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	messageName VARCHAR2(255 CHAR) NOT NULL,
  	targetProcess VARCHAR2(255 CHAR) NOT NULL,
  	targetFlowNode VARCHAR2(255 CHAR) NULL,
  	locked NUMBER(1) NOT NULL,
  	handled NUMBER(1) NOT NULL,
  	processDefinitionId NUMBER(19, 0) NOT NULL,
  	flowNodeName VARCHAR2(255 CHAR),
  	correlation1 VARCHAR2(128 CHAR),
  	correlation2 VARCHAR2(128 CHAR),
  	correlation3 VARCHAR2(128 CHAR),
  	correlation4 VARCHAR2(128 CHAR),
  	correlation5 VARCHAR2(128 CHAR),
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
  	elem_name VARCHAR2(255 CHAR) NOT NULL,
  	inst_scope NUMBER(1) NOT NULL,
  	inst_id NUMBER(19, 0) NOT NULL,
  	def_id NUMBER(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE ref_biz_data_inst (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	name VARCHAR2(255 CHAR) NOT NULL,
  	proc_inst_id NUMBER(19, 0),
  	fn_inst_id NUMBER(19, 0),
  	data_id NUMBER(19, 0),
  	data_classname VARCHAR2(255 CHAR) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);


ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT UK_Ref_Biz_Data_Inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantId);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE multi_biz_data (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	idx NUMBER(19, 0) NOT NULL,
  	data_id NUMBER(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
);

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;
