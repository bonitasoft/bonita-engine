CREATE TABLE configuration (
  tenant_id NUMBER(19, 0) NOT NULL,
  content_type VARCHAR2(50 CHAR) NOT NULL,
  resource_name VARCHAR2(120 CHAR) NOT NULL,
  resource_content BLOB NOT NULL
);
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name);
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type);

CREATE TABLE contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(20 CHAR) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val BLOB
);
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid);

CREATE TABLE arch_contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(20 CHAR) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val BLOB,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);

CREATE TABLE actor (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  description VARCHAR2(1024 CHAR),
  initiator NUMBER(1),
  CONSTRAINT UK_Actor UNIQUE (tenantid, id, scopeId, name),
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
  name VARCHAR2(50 CHAR) NOT NULL,
  creator NUMBER(19, 0),
  description VARCHAR2(1024 CHAR),
  creationDate NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Category UNIQUE (tenantid, name),
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

CREATE TABLE arch_process_comment(
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(512 CHAR) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);
CREATE TABLE process_comment (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(512 CHAR) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE process_definition (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(255 CHAR),
  deploymentDate NUMBER(19, 0) NOT NULL,
  deployedBy NUMBER(19, 0) NOT NULL,
  activationState VARCHAR2(30 CHAR) NOT NULL,
  configurationState VARCHAR2(30 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  lastUpdateDate NUMBER(19, 0),
  categoryId NUMBER(19, 0),
  iconPath VARCHAR2(255 CHAR),
  content_tenantid NUMBER(19, 0) NOT NULL,
  content_id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantId, id),
  CONSTRAINT UK_Process_Definition UNIQUE (tenantId, name, version)
);
CREATE TABLE process_content (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  content CLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(50 CHAR) NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);
CREATE TABLE document (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  author NUMBER(19, 0),
  creationdate NUMBER(19, 0) NOT NULL,
  hascontent NUMBER(1) NOT NULL,
  filename VARCHAR2(255 CHAR),
  mimetype VARCHAR2(255 CHAR),
  url VARCHAR2(1024 CHAR),
  content BLOB,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(50 CHAR) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
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
  sourceObjectId NUMBER(19, 0) NOT NULL,
  stringIndex1 VARCHAR2(255 CHAR),
  stringIndex2 VARCHAR2(255 CHAR),
  stringIndex3 VARCHAR2(255 CHAR),
  stringIndex4 VARCHAR2(255 CHAR),
  stringIndex5 VARCHAR2(255 CHAR),
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
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
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
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);
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
  stringIndex1 VARCHAR2(255 CHAR),
  stringIndex2 VARCHAR2(255 CHAR),
  stringIndex3 VARCHAR2(255 CHAR),
  stringIndex4 VARCHAR2(255 CHAR),
  stringIndex5 VARCHAR2(255 CHAR),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (tenantid, processdefinitionid, stateid);

CREATE TABLE flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
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
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid);

CREATE TABLE connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
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

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    kind VARCHAR2(15 CHAR) NOT NULL,
    name VARCHAR2(255 CHAR) NOT NULL,
    orig_proc_inst_id NUMBER(19, 0),
    orig_fn_inst_id NUMBER(19, 0),
    data_id NUMBER(19, 0),
    data_classname VARCHAR2(255 CHAR) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);

CREATE TABLE arch_multi_biz_data (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	idx NUMBER(19, 0) NOT NULL,
  	data_id NUMBER(19, 0) NOT NULL
);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;

CREATE TABLE report (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  screenshot BLOB,
  content BLOB,
  CONSTRAINT UK_Report UNIQUE (tenantId, name),
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
  token VARCHAR2(50 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  iconPath VARCHAR2(255 CHAR),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  updatedBy NUMBER(19, 0) NOT NULL,
  state VARCHAR2(30 CHAR) NOT NULL,
  homePageId NUMBER(19, 0),
  profileId NUMBER(19, 0),
  layoutId NUMBER(19, 0),
  themeId NUMBER(19, 0),
  displayName VARCHAR2(255 CHAR) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT UK_Business_app UNIQUE (tenantId, token, version);

CREATE INDEX idx_app_token ON business_app (token, tenantid);
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid);
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid);

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  token VARCHAR2(255 CHAR) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT UK_Business_app_page UNIQUE (tenantId, applicationId, token);

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  applicationPageId NUMBER(19, 0),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0)
);

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

CREATE TABLE command (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  IMPLEMENTATION VARCHAR2(100 CHAR) NOT NULL,
  system NUMBER(1),
  CONSTRAINT UK_Command UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50 CHAR),
	description VARCHAR2(50 CHAR),
	transientData NUMBER(1),
	className VARCHAR2(100 CHAR),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60 CHAR),
	namespace VARCHAR2(100 CHAR),
	element VARCHAR2(60 CHAR),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255 CHAR),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50 CHAR) NOT NULL,
	archiveDate NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);

CREATE TABLE data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50 CHAR),
	description VARCHAR2(50 CHAR),
	transientData NUMBER(1),
	className VARCHAR2(100 CHAR),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60 CHAR),
	namespace VARCHAR2(100 CHAR),
	element VARCHAR2(60 CHAR),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255 CHAR),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50 CHAR) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name);

CREATE TABLE dependency (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  filename VARCHAR2(255 CHAR) NOT NULL,
  value_ BLOB NOT NULL,
  CONSTRAINT UK_Dependency UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50 CHAR) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Dependency_Mapping UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
CREATE TABLE pdependency (
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL UNIQUE,
  description VARCHAR2(1024 CHAR),
  filename VARCHAR2(255 CHAR) NOT NULL,
  value_ BLOB NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE pdependencymapping (
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50 CHAR) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_PDependency_Mapping UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;
CREATE TABLE external_identity_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  externalId VARCHAR2(50 CHAR) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_External_Identity_Mapping UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE group_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(125 CHAR) NOT NULL,
  parentPath VARCHAR2(255 CHAR),
  displayName VARCHAR2(255 CHAR),
  description VARCHAR2(1024 CHAR),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  iconid NUMBER(19, 0),
  CONSTRAINT UK_Group UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE role (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
  description VARCHAR2(1024 CHAR),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  iconid NUMBER(19, 0),
  CONSTRAINT UK_Role UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  enabled NUMBER(1) NOT NULL,
  userName VARCHAR2(255 CHAR) NOT NULL,
  password VARCHAR2(60 CHAR),
  firstName VARCHAR2(255 CHAR),
  lastName VARCHAR2(255 CHAR),
  title VARCHAR2(50 CHAR),
  jobTitle VARCHAR2(255 CHAR),
  managerUserId NUMBER(19, 0),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  iconid NUMBER(19, 0),
  CONSTRAINT UK_User UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_login (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  lastConnection NUMBER(19, 0),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE user_contactinfo (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  email VARCHAR2(255 CHAR),
  phone VARCHAR2(50 CHAR),
  mobile VARCHAR2(50 CHAR),
  fax VARCHAR2(50 CHAR),
  building VARCHAR2(50 CHAR),
  room VARCHAR2(50 CHAR),
  address VARCHAR2(255 CHAR),
  zipCode VARCHAR2(50 CHAR),
  city VARCHAR2(255 CHAR),
  state VARCHAR2(255 CHAR),
  country VARCHAR2(255 CHAR),
  website VARCHAR2(255 CHAR),
  personal NUMBER(1) NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;


CREATE TABLE custom_usr_inf_def (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  CONSTRAINT UK_Custom_Usr_Inf_Def UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE custom_usr_inf_val (
  id NUMBER(19, 0) NOT NULL,
  tenantid NUMBER(19, 0) NOT NULL,
  definitionId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  value VARCHAR2(255 CHAR),
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
  log_timestamp NUMBER(19, 0) NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth SMALLINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear SMALLINT NOT NULL,
  userId VARCHAR2(255 CHAR) NOT NULL,
  threadNumber NUMBER(19, 0) NOT NULL,
  clusterNode VARCHAR2(50 CHAR),
  productVersion VARCHAR2(50 CHAR) NOT NULL,
  severity VARCHAR2(50 CHAR) NOT NULL,
  actionType VARCHAR2(50 CHAR) NOT NULL,
  actionScope VARCHAR2(100 CHAR),
  actionStatus SMALLINT NOT NULL,
  rawMessage VARCHAR2(255 CHAR) NOT NULL,
  callerClassName VARCHAR2(200 CHAR),
  callerMethodName VARCHAR2(80 CHAR),
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
  name VARCHAR2(50 CHAR) NOT NULL,
  stringValue VARCHAR2(255 CHAR),
  blobId NUMBER(19, 0),
  valueType VARCHAR2(30 CHAR),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);

CREATE TABLE page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  contentName VARCHAR2(280 CHAR) NOT NULL,
  content BLOB,
  contentType VARCHAR2(50 CHAR),
  processDefinitionId NUMBER(19, 0) NOT NULL
);

ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);

ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);

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
  version VARCHAR2(50 CHAR) NOT NULL,
  previousVersion VARCHAR2(50 CHAR),
  initialVersion VARCHAR2(50 CHAR) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50 CHAR) NOT NULL,
  information CLOB,
  PRIMARY KEY (id)
);

CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(255 CHAR),
  defaultTenant NUMBER(1) NOT NULL,
  iconname VARCHAR2(50 CHAR),
  iconpath VARCHAR2(255 CHAR),
  name VARCHAR2(50 CHAR) NOT NULL,
  status VARCHAR2(15 CHAR) NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE platformCommand (
  id NUMBER(19, 0) PRIMARY KEY,
  name VARCHAR2(50 CHAR) NOT NULL UNIQUE,
  description VARCHAR2(1024 CHAR),
  IMPLEMENTATION VARCHAR2(100 CHAR) NOT NULL
);
CREATE TABLE profile (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Profile UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR),
  description VARCHAR2(1024 CHAR),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0),
  type VARCHAR2(50 CHAR),
  page VARCHAR2(255 CHAR),
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
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobclassname VARCHAR2(100 CHAR) NOT NULL,
  jobname VARCHAR2(100 CHAR) NOT NULL,
  description VARCHAR2(50 CHAR),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE job_param (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobDescriptorId NUMBER(19, 0) NOT NULL,
  key_ VARCHAR2(50 CHAR) NOT NULL,
  value_ BLOB NOT NULL,
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE job_log (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobDescriptorId NUMBER(19, 0) NOT NULL,
  retryNumber NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  lastMessage CLOB,
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantId, id)
);

ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE TABLE theme (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  content BLOB NOT NULL,
  cssContent BLOB,
  type VARCHAR2(50 CHAR) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE form_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process NUMBER(19, 0) NOT NULL,
  type INT NOT NULL,
  task VARCHAR2(255 CHAR),
  page_mapping_tenant_id NUMBER(19, 0),
  page_mapping_id NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  lastUpdatedBy NUMBER(19, 0),
  target VARCHAR2(16 CHAR) NOT NULL,
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE page_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  key_ VARCHAR2(255 CHAR) NOT NULL,
  pageId NUMBER(19, 0) NULL,
  url VARCHAR2(1024 CHAR) NULL,
  urladapter VARCHAR2(255 CHAR) NULL,
  page_authoriz_rules VARCHAR2(1024 CHAR) NULL,
  lastUpdateDate NUMBER(19, 0) NULL,
  lastUpdatedBy NUMBER(19, 0) NULL,
  CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
  PRIMARY KEY (tenantId, id)
);

ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);

CREATE TABLE proc_parameter (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process_id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  value CLOB NULL,
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE bar_resource (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process_id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  content BLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

CREATE TABLE tenant_resource (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  content BLOB NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name);

CREATE TABLE icon (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  mimetype VARCHAR2(255) NOT NULL,
  content BLOB NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
);