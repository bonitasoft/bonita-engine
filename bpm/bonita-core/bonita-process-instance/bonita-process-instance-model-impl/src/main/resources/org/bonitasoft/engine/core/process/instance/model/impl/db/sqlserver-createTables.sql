CREATE TABLE process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  description VARCHAR(255),
  startDate NUMERIC(19, 0) NOT NULL,
  startedBy NUMERIC(19, 0) NOT NULL,
  startedByDelegate NUMERIC(19, 0) NOT NULL,
  endDate NUMERIC(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(50) NOT NULL,
  lastUpdate NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0),
  rootProcessInstanceId NUMERIC(19, 0),
  callerId NUMERIC(19, 0),
  callerType VARCHAR(50),
  interruptingEventId NUMERIC(19, 0),
  migration_plan NUMERIC(19, 0),
  stringIndex1 VARCHAR(50),
  stringIndex2 VARCHAR(50),
  stringIndex3 VARCHAR(50),
  stringIndex4 VARCHAR(50),
  stringIndex5 VARCHAR(50),
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

CREATE TABLE flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  flownodeDefinitionId NUMERIC(19, 0) NOT NULL,
  kind VARCHAR(25) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
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
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR(255),
  sequential BIT,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMERIC(19, 0),
  executedByDelegate NUMERIC(19, 0),
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

CREATE TABLE transition_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(255) NOT NULL,
  source NUMERIC(19, 0),
  terminal BIT NOT NULL,
  stable BIT ,
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  description VARCHAR(255),
  deleted BIT DEFAULT 0,
  token_ref_id NUMERIC(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE connector_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0) NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  executionOrder INT,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE event_trigger_instance (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	eventInstanceId NUMERIC(19, 0) NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	timerType VARCHAR(10),
  	timerValue NUMERIC(19, 0),
  	messageName VARCHAR(255),
  	targetProcess VARCHAR(255),
  	targetFlowNode VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE waiting_event (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventType VARCHAR(50),
  	messageName VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	processName VARCHAR(150),
  	flowNodeName VARCHAR(50),
  	flowNodeDefinitionId NUMERIC(19, 0),
  	subProcessId NUMERIC(19, 0),
  	processDefinitionId NUMERIC(19, 0),
  	rootProcessInstanceId NUMERIC(19, 0),
  	parentProcessInstanceId NUMERIC(19, 0),
  	flowNodeInstanceId NUMERIC(19, 0),
  	relatedActivityInstanceId NUMERIC(19, 0),
  	locked BIT,
  	active BIT,
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE message_instance (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	messageName VARCHAR(255) NOT NULL,
  	targetProcess VARCHAR(255) NOT NULL,
  	targetFlowNode VARCHAR(255) NULL,
  	locked BIT NOT NULL,
  	handled BIT NOT NULL,
  	processDefinitionId NUMERIC(19, 0) NOT NULL,
  	flowNodeName VARCHAR(255),
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
)
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

CREATE TABLE hidden_activity (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	activityId NUMERIC(19, 0) NOT NULL,
  	userId NUMERIC(19, 0) NOT NULL,
  	UNIQUE (tenantid, activityId, userId),
  	PRIMARY KEY (tenantid, id)
)
GO
