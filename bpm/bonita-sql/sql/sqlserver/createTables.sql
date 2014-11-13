CREATE TABLE actor (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(75),
  description NVARCHAR(MAX),
  initiator BIT,
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE actormember (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  actorId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE category (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  creator NUMERIC(19, 0),
  description NVARCHAR(MAX),
  creationDate NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE processcategorymapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  categoryid NUMERIC(19, 0) NOT NULL,
  processid NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
)
GO

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE
GO
CREATE TABLE migration_plan (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255) NOT NULL,
  source_name NVARCHAR(50) NOT NULL,
  source_version NVARCHAR(50) NOT NULL,
  target_name NVARCHAR(50) NOT NULL,
  target_version NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE arch_process_comment(
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(255) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_process_comment on arch_process_comment (tenantid, sourceobjectid)
GO
CREATE INDEX idx2_arch_process_comment on arch_process_comment (tenantid, processinstanceid, archivedate)
GO
CREATE TABLE process_comment (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(255) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(150) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  deploymentDate NUMERIC(19, 0) NOT NULL,
  deployedBy NUMERIC(19, 0) NOT NULL,
  activationState NVARCHAR(30) NOT NULL,
  configurationState NVARCHAR(30) NOT NULL,
  migrationDate NUMERIC(19, 0),
  displayName NVARCHAR(75),
  displayDescription NVARCHAR(255),
  lastUpdateDate NUMERIC(19, 0),
  categoryId NUMERIC(19, 0),
  iconPath NVARCHAR(255),
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
)
GO
CREATE TABLE arch_document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0),
  sourceObjectId NUMERIC(19, 0),
  documentName NVARCHAR(50) NOT NULL,
  documentAuthor NUMERIC(19, 0),
  documentCreationDate NUMERIC(19, 0) NOT NULL,
  documentHasContent BIT NOT NULL,
  documentContentFileName NVARCHAR(255),
  documentContentMimeType NVARCHAR(255),
  contentStorageId NVARCHAR(50),
  documentURL NVARCHAR(255),
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, ID)
)
GO
CREATE TABLE document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0),
  documentName NVARCHAR(50) NOT NULL,
  documentAuthor NUMERIC(19, 0),
  documentCreationDate NUMERIC(19, 0) NOT NULL,
  documentHasContent BIT NOT NULL,
  documentContentFileName NVARCHAR(255),
  documentContentMimeType NVARCHAR(255),
  contentStorageId NVARCHAR(50),
  documentURL NVARCHAR(255),
  PRIMARY KEY (tenantid, ID)
)
GO
CREATE TABLE arch_process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255),
  startDate NUMERIC(19, 0) NOT NULL,
  startedBy NUMERIC(19, 0) NOT NULL,
  startedBySubstitute NUMERIC(19, 0) NOT NULL,
  endDate NUMERIC(19, 0) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  stateId INT NOT NULL,
  lastUpdate NUMERIC(19, 0) NOT NULL,
  rootProcessInstanceId NUMERIC(19, 0),
  callerId NUMERIC(19, 0),
  migration_plan NUMERIC(19, 0),
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  stringIndex1 NVARCHAR(50),
  stringIndex2 NVARCHAR(50),
  stringIndex3 NVARCHAR(50),
  stringIndex4 NVARCHAR(50),
  stringIndex5 NVARCHAR(50),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId)
GO
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate)
GO
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId)
GO

CREATE TABLE arch_flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  flownodeDefinitionId NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  sourceObjectId NUMERIC(19, 0),
  archiveDate NUMERIC(19, 0) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(75),
  displayDescription NVARCHAR(255),
  stateId INT NOT NULL,
  stateName NVARCHAR(50),
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
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef NVARCHAR(255),
  loopDataOutputRef NVARCHAR(255),
  description NVARCHAR(255),
  sequential BIT,
  dataInputItemRef NVARCHAR(255),
  dataOutputItemRef NVARCHAR(255),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMERIC(19, 0),
  executedBySubstitute NUMERIC(19, 0),
  activityInstanceId NUMERIC(19, 0),
  aborting BIT NOT NULL,
  triggeredByEvent BIT,
  interrupting BIT,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy)
GO
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind)
GO
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId)
GO

CREATE TABLE arch_transition_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  source NUMERIC(19, 0),
  target NUMERIC(19, 0),
  state NVARCHAR(50),
  terminal BIT NOT NULL,
  stable BIT ,
  stateCategory NVARCHAR(50) NOT NULL,
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255),
  sourceObjectId NUMERIC(19, 0),
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid)
GO

CREATE TABLE arch_connector_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0) NOT NULL,
  containerType NVARCHAR(10) NOT NULL,
  connectorId NVARCHAR(255) NOT NULL,
  version NVARCHAR(10) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  activationEvent NVARCHAR(30),
  state NVARCHAR(50),
  sourceObjectId NUMERIC(19, 0),
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType)
GO
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


ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id)
GO
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data_inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantid)
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
CREATE TABLE report (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  screenshot VARBINARY(MAX),
  content VARBINARY(MAX),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE processsupervisor (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processDefId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE business_app (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  path NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(255),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  updatedBy NUMERIC(19, 0) NOT NULL,
  state NVARCHAR(30) NOT NULL,
  homePageId NUMERIC(19, 0),
  displayName NVARCHAR(255) NOT NULL
)
GO

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version)
GO

CREATE INDEX idx_app_name ON business_app (name, tenantid)
GO

CREATE TABLE business_app_page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  pageId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL
)
GO

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_name UNIQUE (tenantId, applicationId, name)
GO

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid)
GO
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid)
GO

-- forein keys are create in bonita-persistence-db/postCreateStructure.sql
CREATE TABLE command (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  IMPLEMENTATION NVARCHAR(100) NOT NULL,
  system BIT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE arch_data_instance (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	name NVARCHAR(50),
	description NVARCHAR(50),
	transientData BIT,
	className NVARCHAR(100),
	containerId NUMERIC(19, 0),
	containerType NVARCHAR(60),
	namespace NVARCHAR(100),
	element NVARCHAR(60),
	intValue INT,
	longValue NUMERIC(19, 0),
	shortTextValue NVARCHAR(255),
	booleanValue BIT,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue VARBINARY(MAX),
	clobValue NVARCHAR(MAX),
	discriminant NVARCHAR(50) NOT NULL,
	archiveDate NUMERIC(19, 0) NOT NULL,
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId,containerId, sourceObjectId)
GO

CREATE TABLE arch_data_mapping (
    tenantid NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	containerId NUMERIC(19, 0),
	containerType NVARCHAR(60),
	dataName NVARCHAR(50),
	dataInstanceId NUMERIC(19, 0) NOT NULL,
	archiveDate NUMERIC(19, 0) NOT NULL,
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_data_mapping ON arch_data_mapping (tenantId,containerId, dataInstanceId, sourceObjectId)
GO
CREATE INDEX idx2_arch_data_mapping ON arch_data_mapping (tenantid, containerId, containerType)
GO
CREATE TABLE data_instance (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	name NVARCHAR(50),
	description NVARCHAR(50),
	transientData BIT,
	className NVARCHAR(100),
	containerId NUMERIC(19, 0),
	containerType NVARCHAR(60),
	namespace NVARCHAR(100),
	element NVARCHAR(60),
	intValue INT,
	longValue NUMERIC(19, 0),
	shortTextValue NVARCHAR(255),
	booleanValue BIT,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue VARBINARY(MAX),
	clobValue NVARCHAR(MAX),
	discriminant NVARCHAR(50) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_datai_container ON data_instance (containerId, containerType, tenantId)
GO

CREATE TABLE data_mapping (
    tenantid NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	containerId NUMERIC(19, 0),
	containerType NVARCHAR(60),
	dataName NVARCHAR(50),
	dataInstanceId NUMERIC(19, 0) NOT NULL,
	UNIQUE (tenantId, containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_datamapp_container ON data_mapping (containerId, containerType, tenantId)
GO
CREATE TABLE dependency (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(150) NOT NULL,
  description NVARCHAR(MAX),
  filename NVARCHAR(255) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_dependency_name ON dependency (name)
GO

CREATE TABLE dependencymapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  artifactid NUMERIC(19, 0) NOT NULL,
  artifacttype NVARCHAR(50) NOT NULL,
  dependencyid NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid)
GO
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE
GO
CREATE TABLE pdependency (
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL UNIQUE,
  description NVARCHAR(MAX),
  filename NVARCHAR(255) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependency_name ON pdependency (name)
GO

CREATE TABLE pdependencymapping (
  id NUMERIC(19, 0) NOT NULL,
  artifactid NUMERIC(19, 0) NOT NULL,
  artifacttype NVARCHAR(50) NOT NULL,
  dependencyid NUMERIC(19, 0) NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid)
GO
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE
GO
CREATE TABLE document_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  documentId NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE external_identity_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  externalId NVARCHAR(50) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE group_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  parentPath NVARCHAR(255),
  displayName NVARCHAR(75),
  description NVARCHAR(MAX),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE role (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(75),
  description NVARCHAR(MAX),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_role_name ON role (tenantid, name)
GO

CREATE TABLE user_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  enabled BIT NOT NULL,
  userName NVARCHAR(50) NOT NULL,
  password NVARCHAR(60),
  firstName NVARCHAR(50),
  lastName NVARCHAR(50),
  title NVARCHAR(50),
  jobTitle NVARCHAR(50),
  managerUserId NUMERIC(19, 0),
  delegeeUserName NVARCHAR(50),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  lastConnection NUMERIC(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_user_name ON user_ (tenantid, userName)
GO

CREATE TABLE user_contactinfo (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  email NVARCHAR(255),
  phone NVARCHAR(50),
  mobile NVARCHAR(50),
  fax NVARCHAR(50),
  building NVARCHAR(50),
  room NVARCHAR(50),
  address NVARCHAR(50),
  zipCode NVARCHAR(50),
  city NVARCHAR(50),
  state NVARCHAR(50),
  country NVARCHAR(50),
  website NVARCHAR(50),
  personal BIT NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
)
GO
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE
GO
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal)
GO


CREATE TABLE custom_usr_inf_def (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  description NVARCHAR(MAX),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name)
GO

CREATE TABLE custom_usr_inf_val (
  id NUMERIC(19, 0) NOT NULL,
  tenantid NUMERIC(19, 0) NOT NULL,
  definitionId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  value NVARCHAR(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
)
GO
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE
GO
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE
GO

CREATE TABLE user_membership (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  assignedBy NUMERIC(19, 0),
  assignedDate NUMERIC(19, 0),
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE queriable_log (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  timeStamp NUMERIC(19, 0) NOT NULL,
  year SMALLINT NOT NULL,
  month TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId NVARCHAR(50) NOT NULL,
  threadNumber NUMERIC(19, 0) NOT NULL,
  clusterNode NVARCHAR(50),
  productVersion NVARCHAR(50) NOT NULL,
  severity NVARCHAR(50) NOT NULL,
  actionType NVARCHAR(50) NOT NULL,
  actionScope NVARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage NVARCHAR(255) NOT NULL,
  callerClassName NVARCHAR(200),
  callerMethodName NVARCHAR(80),
  numericIndex1 NUMERIC(19, 0),
  numericIndex2 NUMERIC(19, 0),
  numericIndex3 NUMERIC(19, 0),
  numericIndex4 NUMERIC(19, 0),
  numericIndex5 NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE queriablelog_p (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  queriableLogId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  stringValue NVARCHAR(255),
  blobId NUMERIC(19, 0),
  valueType NVARCHAR(30),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId)
GO
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id)
GO
CREATE TABLE page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  contentName NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE blob_ (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	blobValue VARBINARY(MAX),
	PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE platform (
  id NUMERIC(19, 0) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  previousVersion NVARCHAR(50) NOT NULL,
  initialVersion NVARCHAR(50) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
)
GO

CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  defaultTenant BIT NOT NULL,
  iconname NVARCHAR(50),
  iconpath NVARCHAR(255),
  name NVARCHAR(50) NOT NULL,
  status NVARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
)
GO
CREATE TABLE platformCommand (
  id NUMERIC(19, 0) PRIMARY KEY,
  name NVARCHAR(50) NOT NULL UNIQUE,
  description NVARCHAR(MAX),
  IMPLEMENTATION NVARCHAR(100) NOT NULL
)
GO
CREATE TABLE profile (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  isDefault BIT NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)
GO

CREATE TABLE profileentry (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  profileId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50),
  description NVARCHAR(MAX),
  parentId NUMERIC(19, 0),
  index_ NUMERIC(19, 0),
  type NVARCHAR(50),
  page NVARCHAR(50),
  custom BIT DEFAULT 0,
  PRIMARY KEY (tenantId, id)
)
GO

CREATE INDEX indexProfileEntry ON profileentry(tenantId, parentId, profileId)
GO

CREATE TABLE profilemember (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  profileId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE job_desc (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobclassname NVARCHAR(100) NOT NULL,
  jobname NVARCHAR(100) NOT NULL,
  description NVARCHAR(50),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE job_param (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobDescriptorId NUMERIC(19, 0) NOT NULL,
  key_ NVARCHAR(50) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE job_log (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobDescriptorId NUMERIC(19, 0) NOT NULL,
  retryNumber NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  lastMessage NVARCHAR(MAX),
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantid, id)
)
GO

ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE
GO
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE
GO
CREATE TABLE theme (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  isDefault BIT NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  cssContent VARBINARY(MAX),
  type NVARCHAR(50) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
)
GO
