CREATE TABLE arch_process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  migration_plan NUMBER(19, 0),
  sourceObjectId NUMBER(19, 0) NOT NULL,
  stringIndex1 VARCHAR2(50 CHAR),
  stringIndex2 VARCHAR2(50 CHAR),
  stringIndex3 VARCHAR2(50 CHAR),
  stringIndex4 VARCHAR2(50 CHAR),
  stringIndex5 VARCHAR2(50 CHAR),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  stateId INT NOT NULL,
  stateName VARCHAR2(50 CHAR),
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
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR2(255 CHAR),
  loopDataOutputRef VARCHAR2(255 CHAR),
  description VARCHAR2(255 CHAR),
  sequential NUMBER(1),
  dataInputItemRef VARCHAR2(255 CHAR),
  dataOutputItemRef VARCHAR2(255 CHAR),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMBER(19, 0),
  executedBySubstitute NUMBER(19, 0),
  activityInstanceId NUMBER(19, 0),
  aborting NUMBER(1) NOT NULL,
  triggeredByEvent NUMBER(1),
  interrupting NUMBER(1),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_transition_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  source NUMBER(19, 0),
  target NUMBER(19, 0),
  state VARCHAR2(50 CHAR),
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  stateCategory VARCHAR2(50 CHAR) NOT NULL,
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (rootContainerId, tenantid);

CREATE TABLE arch_connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(10 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);
