CREATE TABLE configuration (
  tenant_id NUMERIC(19, 0) NOT NULL,
  content_type  NVARCHAR(50) NOT NULL,
  resource_name  NVARCHAR(120) NOT NULL,
  resource_content  VARBINARY(MAX) NOT NULL
)
GO
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name)
GO
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type)
GO

CREATE TABLE contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(20) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  val VARBINARY(MAX)
)
GO
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId)
GO
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid)
GO
CREATE INDEX idx_cd_scope_name ON contract_data (kind, scopeId, name, tenantid)
GO

CREATE TABLE arch_contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(20) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  val VARBINARY(MAX),
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL
)
GO
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId)
GO
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid)
GO
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid)
GO

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
CREATE TABLE arch_process_comment(
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(512) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid)
GO
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid)
GO
CREATE TABLE process_comment (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(512) NOT NULL,
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
  displayName NVARCHAR(75),
  displayDescription NVARCHAR(255),
  lastUpdateDate NUMERIC(19, 0),
  categoryId NUMERIC(19, 0),
  iconPath NVARCHAR(255),
  content_tenantid NUMERIC(19, 0) NOT NULL,
  content_id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
)
GO
CREATE TABLE process_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE arch_document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0),
  processinstanceid NUMERIC(19, 0) NOT NULL,
  documentid NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  version NVARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, ID)
)
GO
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid)
GO
CREATE TABLE document (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  author NUMERIC(19, 0),
  creationdate NUMERIC(19, 0) NOT NULL,
  hascontent BIT NOT NULL,
  filename NVARCHAR(255),
  mimetype NVARCHAR(255),
  url NVARCHAR(1024),
  content VARBINARY(MAX),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0) NOT NULL,
  documentid NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  version NVARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
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
  startedBy NUMERIC(19, 0) NULL,
  startedBySubstitute NUMERIC(19, 0) NOT NULL,
  endDate NUMERIC(19, 0) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  stateId INT NOT NULL,
  lastUpdate NUMERIC(19, 0) NOT NULL,
  rootProcessInstanceId NUMERIC(19, 0),
  callerId NUMERIC(19, 0),
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  stringIndex1 NVARCHAR(255),
  stringIndex2 NVARCHAR(255),
  stringIndex3 NVARCHAR(255),
  stringIndex4 NVARCHAR(255),
  stringIndex5 NVARCHAR(255),
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
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255),
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
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy)
GO
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3)
GO
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind)
GO
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId)
GO

CREATE TABLE arch_connector_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0) NOT NULL,
  containerType NVARCHAR(10) NOT NULL,
  connectorId NVARCHAR(255) NOT NULL,
  version NVARCHAR(50) NOT NULL,
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
  stringIndex1 NVARCHAR(255),
  stringIndex2 NVARCHAR(255),
  stringIndex3 NVARCHAR(255),
  stringIndex4 NVARCHAR(255),
  stringIndex5 NVARCHAR(255),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (tenantid, processdefinitionid, stateid)
GO

CREATE TABLE flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  flownodeDefinitionId NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255),
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
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId)
GO
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4)
GO
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid)
GO

CREATE TABLE connector_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  containerId NUMERIC(19, 0) NOT NULL,
  containerType NVARCHAR(10) NOT NULL,
  connectorId NVARCHAR(255) NOT NULL,
  version NVARCHAR(50) NOT NULL,
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
  	kind NVARCHAR(15) NOT NULL,
  	eventInstanceId NUMERIC(19, 0) NOT NULL,
  	eventInstanceName NVARCHAR(50),
  	messageName NVARCHAR(255),
  	targetProcess NVARCHAR(255),
  	targetFlowNode NVARCHAR(255),
  	signalName NVARCHAR(255),
  	errorCode NVARCHAR(255),
  	executionDate NUMERIC(19, 0), 
  	jobTriggerName NVARCHAR(255),
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

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    kind NVARCHAR(15) NOT NULL,
    name NVARCHAR(255) NOT NULL,
    orig_proc_inst_id NUMERIC(19, 0),
    orig_fn_inst_id NUMERIC(19, 0),
    data_id NUMERIC(19, 0),
    data_classname NVARCHAR(255) NOT NULL
)
GO
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id)
GO
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id)
GO
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id)
GO

CREATE TABLE arch_multi_biz_data (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    idx NUMERIC(19, 0) NOT NULL,
    data_id NUMERIC(19, 0) NOT NULL
)
GO
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id)
GO
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE
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
  token NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(255),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  updatedBy NUMERIC(19, 0) NOT NULL,
  state NVARCHAR(30) NOT NULL,
  homePageId NUMERIC(19, 0),
  profileId NUMERIC(19, 0),
  layoutId NUMERIC(19, 0),
  themeId NUMERIC(19, 0),
  displayName NVARCHAR(255) NOT NULL
)
GO

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version)
GO

CREATE INDEX idx_app_token ON business_app (token, tenantid)
GO
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid)
GO
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid)
GO

CREATE TABLE business_app_page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  pageId NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(255) NOT NULL
)
GO

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token)
GO

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid)
GO
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid)
GO

CREATE TABLE business_app_menu (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  applicationPageId NUMERIC(19, 0),
  parentId NUMERIC(19, 0),
  index_ NUMERIC(19, 0)
)
GO

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id)
GO

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid)
GO
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid)
GO
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid)
GO

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

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId)
GO
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId)
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
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name)
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
CREATE INDEX idx_dependency_name ON dependency (name, id)
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
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid, id)
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
CREATE INDEX idx_pdependency_name ON pdependency (name, id)
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
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid, id)
GO
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE
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
  name NVARCHAR(125) NOT NULL,
  parentPath NVARCHAR(255),
  displayName NVARCHAR(255),
  description NVARCHAR(MAX),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  iconid NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_group_name ON group_ (tenantid, parentPath, name);
GO
CREATE TABLE role (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255),
  description NVARCHAR(MAX),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  iconid NUMERIC(19, 0),
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
  userName NVARCHAR(255) NOT NULL,
  password NVARCHAR(60),
  firstName NVARCHAR(255),
  lastName NVARCHAR(255),
  title NVARCHAR(50),
  jobTitle NVARCHAR(255),
  managerUserId NUMERIC(19, 0),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  iconid NUMERIC(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_user_name ON user_ (tenantid, userName)
GO

CREATE TABLE user_login (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  lastConnection NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
)
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
  address NVARCHAR(255),
  zipCode NVARCHAR(50),
  city NVARCHAR(255),
  state NVARCHAR(255),
  country NVARCHAR(255),
  website NVARCHAR(255),
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
  log_timestamp NUMERIC(19, 0) NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId NVARCHAR(255) NOT NULL,
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

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId, id)
GO
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id)
GO
CREATE TABLE page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  contentName NVARCHAR(280) NOT NULL,
  content VARBINARY(MAX),
  contentType NVARCHAR(50) NOT NULL,
  processDefinitionId NUMERIC(19,0) NOT NULL
)
GO

ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
GO

ALTER TABLE page ADD CONSTRAINT  uk_page UNIQUE  (tenantId, name, processDefinitionId)
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
  information NVARCHAR(MAX),
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
  page NVARCHAR(255),
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
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE form_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process NUMERIC(19, 0) NOT NULL,
  type INT NOT NULL,
  task NVARCHAR(255),
  page_mapping_tenant_id NUMERIC(19, 0),
  page_mapping_id NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  lastUpdatedBy NUMERIC(19, 0),
  target NVARCHAR(16) NOT NULL,
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE page_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  key_ NVARCHAR(255) NOT NULL,
  pageId NUMERIC(19, 0) NULL,
  url NVARCHAR(1024) NULL,
  urladapter NVARCHAR(255) NULL,
  page_authoriz_rules NVARCHAR(MAX) NULL,
  lastUpdateDate NUMERIC(19, 0) NULL,
  lastUpdatedBy NUMERIC(19, 0) NULL,
  CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
  PRIMARY KEY (tenantId, id)
)
GO
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id)
GO
CREATE TABLE proc_parameter (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process_id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  value NVARCHAR(MAX) NULL,
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE bar_resource (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process_id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  type NVARCHAR(16) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name)
GO
CREATE TABLE tenant_resource (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  type NVARCHAR(16) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name)
GO
CREATE TABLE icon (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  mimetype NVARCHAR(255) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
)
GO
