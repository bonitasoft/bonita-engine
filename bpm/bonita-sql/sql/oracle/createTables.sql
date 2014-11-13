CREATE TABLE actor (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(75),
  description VARCHAR2(1024),
  initiator NUMBER(1),
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE actormember (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  actorId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE category (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  creator NUMBER(19, 0),
  description VARCHAR22(1024),
  creationDate NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE processcategorymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  categoryid NUMBER(19, 0) NOT NULL,
  processid NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;
CREATE TABLE migration_plan (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255) NOT NULL,
  source_name VARCHAR2(50) NOT NULL,
  source_version VARCHAR2(50) NOT NULL,
  target_name VARCHAR2(50) NOT NULL,
  target_version VARCHAR2(50) NOT NULL,
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_process_comment(
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(255) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_process_comment on arch_process_comment (tenantid, sourceobjectid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (tenantid, processinstanceid, archivedate);
CREATE TABLE process_comment (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(255) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE process_definition (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  description VARCHAR2(255),
  deploymentDate NUMBER(19, 0) NOT NULL,
  deployedBy NUMBER(19, 0) NOT NULL,
  activationState VARCHAR2(30) NOT NULL,
  configurationState VARCHAR2(30) NOT NULL,
  migrationDate NUMBER(19, 0),
  displayName VARCHAR2(75),
  displayDescription VARCHAR2(255),
  lastUpdateDate NUMBER(19, 0),
  categoryId NUMBER(19, 0),
  iconPath VARCHAR2(255),
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
);
CREATE TABLE arch_document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0),
  sourceObjectId NUMBER(19, 0),
  documentName VARCHAR2(50) NOT NULL,
  documentAuthor NUMBER(19, 0),
  documentCreationDate NUMBER(19, 0) NOT NULL,
  documentHasContent NUMBER(1) NOT NULL,
  documentContentFileName VARCHAR2(255),
  documentContentMimeType VARCHAR2(255),
  contentStorageId VARCHAR2(50),
  documentURL VARCHAR2(255),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0),
  documentName VARCHAR2(50) NOT NULL,
  documentAuthor NUMBER(19, 0),
  documentCreationDate NUMBER(19, 0) NOT NULL,
  documentHasContent NUMBER(1) NOT NULL,
  documentContentFileName VARCHAR2(255),
  documentContentMimeType VARCHAR2(255),
  contentStorageId VARCHAR2(50),
  documentURL VARCHAR2(255),
  PRIMARY KEY (tenantid, ID)
);
CREATE TABLE arch_process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255),
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
  stringIndex1 VARCHAR2(50),
  stringIndex2 VARCHAR2(50),
  stringIndex3 VARCHAR2(50),
  stringIndex4 VARCHAR2(50),
  stringIndex5 VARCHAR2(50),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(75),
  displayDescription VARCHAR2(255),
  stateId INT NOT NULL,
  stateName VARCHAR2(50),
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
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR2(255),
  loopDataOutputRef VARCHAR2(255),
  description VARCHAR2(255),
  sequential NUMBER(1),
  dataInputItemRef VARCHAR2(255),
  dataOutputItemRef VARCHAR2(255),
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
  name VARCHAR2(255) NOT NULL,
  source NUMBER(19, 0),
  target NUMBER(19, 0),
  state VARCHAR2(50),
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  stateCategory VARCHAR2(50) NOT NULL,
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid);

CREATE TABLE arch_connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10) NOT NULL,
  connectorId VARCHAR2(255) NOT NULL,
  version VARCHAR2(10) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  activationEvent VARCHAR2(30),
  state VARCHAR2(50),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);
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
  	kind VARCHAR2(15) NOT NULL,
  	name VARCHAR2(255) NOT NULL,
  	proc_inst_id NUMBER(19, 0),
  	fn_inst_id NUMBER(19, 0),
  	data_id NUMBER(19, 0),
  	data_classname VARCHAR2(255) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);


ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data_inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantid);
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
CREATE TABLE report (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR22(1024),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  screenshot BLOB,
  content BLOB,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE processsupervisor (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processDefId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE business_app (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  path VARCHAR2(255) NOT NULL,
  description VARCHAR22(1024),
  iconPath VARCHAR2(255),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  updatedBy NUMBER(19, 0) NOT NULL,
  state VARCHAR2(30) NOT NULL,
  homePageId NUMBER(19, 0),
  displayName VARCHAR2(255) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version);

CREATE INDEX idx_app_name ON business_app (name, tenantid);

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_name UNIQUE (tenantId, applicationId, name);

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE command (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR22(1024),
  IMPLEMENTATION VARCHAR2(100) NOT NULL,
  system NUMBER(1),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50),
	description VARCHAR2(50),
	transientData NUMBER(1),
	className VARCHAR2(100),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60),
	namespace VARCHAR2(100),
	element VARCHAR2(60),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50) NOT NULL,
	archiveDate NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId,containerId, sourceObjectId);

CREATE TABLE arch_data_mapping (
    tenantid NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60),
	dataName VARCHAR2(50),
	dataInstanceId NUMBER(19, 0) NOT NULL,
	archiveDate NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_mapping ON arch_data_mapping (tenantId,containerId, dataInstanceId, sourceObjectId);
CREATE INDEX idx2_arch_data_mapping ON arch_data_mapping (tenantid, containerId, containerType);
CREATE TABLE data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50),
	description VARCHAR2(50),
	transientData NUMBER(1),
	className VARCHAR2(100),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60),
	namespace VARCHAR2(100),
	element VARCHAR2(60),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datai_container ON data_instance (containerId, containerType, tenantId);

CREATE TABLE data_mapping (
    tenantid NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60),
	dataName VARCHAR2(50),
	dataInstanceId NUMBER(19, 0) NOT NULL,
	UNIQUE (tenantId, containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datamapp_container ON data_mapping (containerId, containerType, tenantId);
CREATE TABLE dependency (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150) NOT NULL,
  description VARCHAR22(1024),
  filename VARCHAR2(255) NOT NULL,
  value_ BLOB NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
CREATE TABLE pdependency (
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL UNIQUE,
  description VARCHAR22(1024),
  filename VARCHAR2(255) NOT NULL,
  value_ BLOB NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE pdependencymapping (
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;
CREATE TABLE document_content (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  documentId VARCHAR2(50) NOT NULL,
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE external_identity_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25) NOT NULL,
  externalId VARCHAR2(50) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE group_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  parentPath VARCHAR2(255),
  displayName VARCHAR2(75),
  description VARCHAR22(1024),
  iconName VARCHAR2(50),
  iconPath VARCHAR2(50),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE role (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(75),
  description VARCHAR22(1024),
  iconName VARCHAR2(50),
  iconPath VARCHAR2(50),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  enabled NUMBER(1) NOT NULL,
  userName VARCHAR2(50) NOT NULL,
  password VARCHAR2(60),
  firstName VARCHAR2(50),
  lastName VARCHAR2(50),
  title VARCHAR2(50),
  jobTitle VARCHAR2(50),
  managerUserId NUMBER(19, 0),
  delegeeUserName VARCHAR2(50),
  iconName VARCHAR2(50),
  iconPath VARCHAR2(50),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  lastConnection NUMBER(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_contactinfo (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  email VARCHAR2(255),
  phone VARCHAR2(50),
  mobile VARCHAR2(50),
  fax VARCHAR2(50),
  building VARCHAR2(50),
  room VARCHAR2(50),
  address VARCHAR2(50),
  zipCode VARCHAR2(50),
  city VARCHAR2(50),
  state VARCHAR2(50),
  country VARCHAR2(50),
  website VARCHAR2(50),
  personal NUMBER(1) NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;


CREATE TABLE custom_usr_inf_def (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75) NOT NULL,
  description VARCHAR22(1024),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);

CREATE TABLE custom_usr_inf_val (
  id NUMBER(19, 0) NOT NULL,
  tenantid NUMBER(19, 0) NOT NULL,
  definitionId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  value VARCHAR2(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE;

CREATE TABLE user_membership (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  assignedBy NUMBER(19, 0),
  assignedDate NUMBER(19, 0),
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE queriable_log (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  timeStamp NUMBER(19, 0) NOT NULL,
  year SMALLINT NOT NULL,
  month SMALLINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear SMALLINT NOT NULL,
  userId VARCHAR2(50) NOT NULL,
  threadNumber NUMBER(19, 0) NOT NULL,
  clusterNode VARCHAR2(50),
  productVersion VARCHAR2(50) NOT NULL,
  severity VARCHAR2(50) NOT NULL,
  actionType VARCHAR2(50) NOT NULL,
  actionScope VARCHAR2(100),
  actionStatus SMALLINT NOT NULL,
  rawMessage VARCHAR2(255) NOT NULL,
  callerClassName VARCHAR2(200),
  callerMethodName VARCHAR2(80),
  numericIndex1 NUMBER(19, 0),
  numericIndex2 NUMBER(19, 0),
  numericIndex3 NUMBER(19, 0),
  numericIndex4 NUMBER(19, 0),
  numericIndex5 NUMBER(19, 0),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE queriablelog_p (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  queriableLogId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  stringValue VARCHAR2(255),
  blobId NUMBER(19, 0),
  valueType VARCHAR2(30),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);
CREATE TABLE page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(255) NOT NULL,
  description VARCHAR22(1024),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  contentName VARCHAR2(50) NOT NULL,
  content BLOB,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE blob_ (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	blobValue BLOB,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE platform (
  id NUMBER(19, 0) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  previousVersion VARCHAR2(50),
  initialVersion VARCHAR2(50) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50) NOT NULL,
  description VARCHAR2(255),
  defaultTenant NUMBER(1) NOT NULL,
  iconname VARCHAR2(50),
  iconpath VARCHAR2(255),
  name VARCHAR2(50) NOT NULL,
  status VARCHAR2(15) NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE platformCommand (
  id NUMBER(19, 0) PRIMARY KEY,
  name VARCHAR2(50) NOT NULL UNIQUE,
  description VARCHAR22(1024),
  IMPLEMENTATION VARCHAR2(100) NOT NULL
);
CREATE TABLE profile (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR22(1024),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50),
  description VARCHAR22(1024),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0),
  type VARCHAR2(50),
  page VARCHAR2(50),
  custom NUMBER(1) DEFAULT 0,
  PRIMARY KEY (tenantId, id)
);

CREATE INDEX indexProfileEntry ON profileentry(tenantId, parentId, profileId);

CREATE TABLE profilemember (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE job_desc (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobclassname VARCHAR2(100) NOT NULL,
  jobname VARCHAR2(100) NOT NULL,
  description VARCHAR2(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_param (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobDescriptorId NUMBER(19, 0) NOT NULL,
  key_ VARCHAR2(50) NOT NULL,
  value_ BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_log (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobDescriptorId NUMBER(19, 0) NOT NULL,
  retryNumber NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  lastMessage VARCHAR22(1024),
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE TABLE theme (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  content BLOB NOT NULL,
  cssContent BLOB,
  type VARCHAR2(50) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);
