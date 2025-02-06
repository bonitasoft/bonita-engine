CREATE TABLE configuration (
  tenant_id BIGINT NOT NULL,
  content_type VARCHAR(50) NOT NULL,
  resource_name VARCHAR(120) NOT NULL,
  resource_content LONGBLOB NOT NULL
);
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name);
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type);

CREATE TABLE contract_data (
  id BIGINT NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  val CLOB,
  CONSTRAINT pk_contract_data PRIMARY KEY (id, scopeId),
  CONSTRAINT uk_contract_data_kind_scopeid_name UNIQUE (kind, scopeId, name)
);

CREATE TABLE arch_contract_data (
  id BIGINT NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  val CLOB,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  CONSTRAINT pk_arch_contract_data PRIMARY KEY (id, scopeId),
  CONSTRAINT uk_arch_contract_data_kind_scopeid_name UNIQUE (kind, scopeId, name)
);

CREATE TABLE actor (
  id BIGINT NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  initiator BOOLEAN,
  CONSTRAINT uk_actor_id_scopeid_name UNIQUE (id, scopeId, name),
  CONSTRAINT pk_actor PRIMARY KEY (id)
);

CREATE TABLE actormember (
  id BIGINT NOT NULL,
  actorId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  CONSTRAINT uk_actormember_actorid_userid_groupid_roleid UNIQUE (actorId, userId, groupId, roleId),
  CONSTRAINT pk_actormember PRIMARY KEY (id)
);

ALTER TABLE actormember ADD CONSTRAINT fk_actormember_actorid FOREIGN KEY (actorId) REFERENCES actor(id);

CREATE TABLE category (
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator BIGINT,
  description LONGVARCHAR,
  creationDate BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  CONSTRAINT pk_category PRIMARY KEY (id),
  CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE processcategorymapping (
  id BIGINT NOT NULL,
  categoryid BIGINT NOT NULL,
  processid BIGINT NOT NULL,
  CONSTRAINT pk_processcategorymapping PRIMARY KEY (id),
  CONSTRAINT uk_processcategorymapping_categoryid_processid UNIQUE (categoryid, processid)
);
ALTER TABLE processcategorymapping ADD CONSTRAINT fk_processcategorymapping_categoryid FOREIGN KEY (categoryid) REFERENCES category(id) ON DELETE CASCADE;

CREATE TABLE arch_process_comment(
  id BIGINT NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(512) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  CONSTRAINT pk_arch_process_comment PRIMARY KEY (id)
);
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate);

CREATE TABLE process_comment (
  id BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(512) NOT NULL,
  CONSTRAINT pk_process_comment PRIMARY KEY (id)
);
CREATE INDEX idx1_process_comment on process_comment (processInstanceId);

CREATE TABLE process_content (
  id BIGINT NOT NULL,
  content MEDIUMTEXT NOT NULL,
  CONSTRAINT pk_process_content PRIMARY KEY (id)
);

CREATE TABLE process_definition (
  id BIGINT NOT NULL,
  processId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate BIGINT NOT NULL,
  deployedBy BIGINT NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate BIGINT,
  categoryId BIGINT,
  iconPath VARCHAR(255),
  content_id BIGINT NOT NULL,
  CONSTRAINT pk_process_definition PRIMARY KEY (id),
  CONSTRAINT uk_process_definition_name_version UNIQUE (name, version)
);
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_content_id FOREIGN KEY (content_id) REFERENCES process_content(id);

CREATE TABLE document (
  id BIGINT NOT NULL,
  author BIGINT,
  creationdate BIGINT NOT NULL,
  hascontent BOOLEAN NOT NULL,
  filename VARCHAR(255),
  mimetype VARCHAR(255),
  url VARCHAR(1024),
  content LONGBLOB NULL,
  CONSTRAINT pk_document PRIMARY KEY (id)
);

CREATE TABLE document_mapping (
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  CONSTRAINT pk_document_mapping PRIMARY KEY (id)
);
ALTER TABLE document_mapping ADD CONSTRAINT fk_document_mapping_documentid FOREIGN KEY (documentid) REFERENCES document(id) ON DELETE CASCADE;

CREATE TABLE arch_document_mapping (
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  archiveDate BIGINT NOT NULL,
  CONSTRAINT pk_arch_document_mapping PRIMARY KEY (id)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid);
ALTER TABLE arch_document_mapping ADD CONSTRAINT fk_arch_document_mapping_documentid FOREIGN KEY (documentid) REFERENCES document(id) ON DELETE CASCADE;

CREATE TABLE arch_process_instance (
  id BIGINT NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  description VARCHAR(255),
  startDate BIGINT NOT NULL,
  startedBy BIGINT NOT NULL,
  startedBySubstitute BIGINT NOT NULL,
  endDate BIGINT NOT NULL,
  archiveDate BIGINT NOT NULL,
  stateId INT NOT NULL,
  lastUpdate BIGINT NOT NULL,
  rootProcessInstanceId BIGINT,
  callerId BIGINT,
  sourceObjectId BIGINT NOT NULL,
  stringIndex1 VARCHAR(255),
  stringIndex2 VARCHAR(255),
  stringIndex3 VARCHAR(255),
  stringIndex4 VARCHAR(255),
  stringIndex5 VARCHAR(255),
  CONSTRAINT pk_arch_process_instance PRIMARY KEY (id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  id BIGINT NOT NULL,
  flownodeDefinitionId BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId BIGINT NULL,
  assigneeId BIGINT DEFAULT 0 NOT NULL,
  reachedStateDate BIGINT,
  lastUpdateDate BIGINT,
  expectedEndDate BIGINT,
  claimedDate BIGINT,
  priority TINYINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  description VARCHAR(255),
  sequential BOOLEAN,
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy BIGINT,
  executedBySubstitute BIGINT,
  activityInstanceId BIGINT,
  aborting BOOLEAN NOT NULL,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  CONSTRAINT pk_arch_flownode_instance PRIMARY KEY (id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(kind, logicalGroup3);
CREATE INDEX idx_afi_lg4 ON arch_flownode_instance(logicalGroup4);
CREATE INDEX idx_afi_sourceid_kind ON arch_flownode_instance (sourceObjectId, kind);
CREATE INDEX idx1_afi_root_parent ON arch_flownode_instance (rootContainerId, parentContainerId);
CREATE INDEX idx_lg4_lg2 on arch_flownode_instance(logicalGroup4, logicalGroup2);

CREATE TABLE arch_connector_instance (
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  CONSTRAINT pk_arch_connector_instance PRIMARY KEY (id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (containerId, containerType);
CREATE TABLE process_instance (
  id BIGINT NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  description VARCHAR(255),
  startDate BIGINT NOT NULL,
  startedBy BIGINT NOT NULL,
  startedBySubstitute BIGINT NOT NULL,
  endDate BIGINT NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(50) NOT NULL,
  lastUpdate BIGINT NOT NULL,
  containerId BIGINT,
  rootProcessInstanceId BIGINT,
  callerId BIGINT,
  callerType VARCHAR(50),
  interruptingEventId BIGINT,
  stringIndex1 VARCHAR(255),
  stringIndex2 VARCHAR(255),
  stringIndex3 VARCHAR(255),
  stringIndex4 VARCHAR(255),
  stringIndex5 VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (processdefinitionid, stateid);

CREATE TABLE flownode_instance (
  id BIGINT NOT NULL,
  flownodeDefinitionId BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId BIGINT NULL,
  assigneeId BIGINT DEFAULT 0 NOT NULL,
  reachedStateDate BIGINT,
  lastUpdateDate BIGINT,
  expectedEndDate BIGINT,
  claimedDate BIGINT,
  priority TINYINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
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
  executedBy BIGINT,
  executedBySubstitute BIGINT,
  activityInstanceId BIGINT,
  state_executing BOOLEAN DEFAULT FALSE,
  abortedByBoundary BIGINT,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  tokenCount INT NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal);
CREATE INDEX idx_fn_lg2_state ON flownode_instance (logicalGroup2, stateName);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind);

CREATE TABLE connector_instance (
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  executionOrder INT,
  exceptionMessage VARCHAR(255),
  stackTrace CLOB,
  CONSTRAINT pk_connector_instance PRIMARY KEY (id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
  	id BIGINT NOT NULL,
  	eventInstanceId BIGINT NOT NULL,
  	eventInstanceName VARCHAR(50),
  	executionDate BIGINT,
  	jobTriggerName VARCHAR(255),
    CONSTRAINT pk_event_trigger_instance PRIMARY KEY (id)
);

CREATE TABLE waiting_event (
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventType VARCHAR(50),
  	messageName VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	processName VARCHAR(150),
  	flowNodeName VARCHAR(50),
  	flowNodeDefinitionId BIGINT,
  	subProcessId BIGINT,
  	processDefinitionId BIGINT,
  	rootProcessInstanceId BIGINT,
  	parentProcessInstanceId BIGINT,
  	flowNodeInstanceId BIGINT,
  	relatedActivityInstanceId BIGINT,
  	locked BOOLEAN,
  	active BOOLEAN,
  	progress TINYINT,
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
    CONSTRAINT pk_waiting_event PRIMARY KEY (id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, kind, locked, active);
CREATE INDEX idx_waiting_event_correl ON waiting_event (correlation1, correlation2, correlation3, correlation4, correlation5);

CREATE TABLE message_instance (
  	id BIGINT NOT NULL,
  	messageName VARCHAR(255) NOT NULL,
  	targetProcess VARCHAR(255) NOT NULL,
  	targetFlowNode VARCHAR(255) NULL,
  	locked BOOLEAN NOT NULL,
  	handled BOOLEAN NOT NULL,
  	processDefinitionId BIGINT NOT NULL,
  	flowNodeName VARCHAR(255),
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	creationDate BIGINT NOT NULL,
    CONSTRAINT pk_message_instance PRIMARY KEY (id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);
CREATE INDEX idx_message_instance_correl ON message_instance (correlation1, correlation2, correlation3, correlation4, correlation5);

CREATE TABLE pending_mapping (
  	id BIGINT NOT NULL,
  	activityId BIGINT NOT NULL,
  	actorId BIGINT,
  	userId BIGINT,
    CONSTRAINT pk_pending_mapping PRIMARY KEY (id),
    CONSTRAINT uk_pending_mapping_activityid_userid_actorid UNIQUE (activityId, userId, actorId)
);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_activityid FOREIGN KEY (activityId) REFERENCES flownode_instance(id);

CREATE TABLE ref_biz_data_inst (
  id BIGINT NOT NULL,
  kind VARCHAR(15) NOT NULL,
  name VARCHAR(255) NOT NULL,
  proc_inst_id BIGINT,
  fn_inst_id BIGINT,
  data_id BIGINT,
  data_classname VARCHAR(255) NOT NULL,
  CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (id)
);
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_inst_proc_inst_id FOREIGN KEY (proc_inst_id) REFERENCES process_instance(id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_inst_fn_inst_id FOREIGN KEY (fn_inst_id) REFERENCES flownode_instance(id) ON DELETE CASCADE;

CREATE TABLE multi_biz_data (
  id BIGINT NOT NULL,
  idx BIGINT NOT NULL,
  data_id BIGINT NOT NULL,
  CONSTRAINT pk_multi_biz_data PRIMARY KEY (id, data_id)
);
ALTER TABLE multi_biz_data ADD CONSTRAINT fk_multi_biz_data_id FOREIGN KEY (id) REFERENCES ref_biz_data_inst(id) ON DELETE CASCADE;

CREATE TABLE arch_ref_biz_data_inst (
  id BIGINT NOT NULL,
  kind VARCHAR(15) NOT NULL,
  name VARCHAR(255) NOT NULL,
  orig_proc_inst_id BIGINT,
  orig_fn_inst_id BIGINT,
  data_id BIGINT,
  data_classname VARCHAR(255) NOT NULL,
  CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (id)
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (orig_fn_inst_id);

CREATE TABLE arch_multi_biz_data (
  id BIGINT NOT NULL,
  idx BIGINT NOT NULL,
  data_id BIGINT NOT NULL,
  CONSTRAINT pk_arch_multi_biz_data PRIMARY KEY (id, data_id)
);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_multi_biz_data_id FOREIGN KEY (id) REFERENCES arch_ref_biz_data_inst(id) ON DELETE CASCADE;

CREATE TABLE processsupervisor (
  id BIGINT NOT NULL,
  processDefId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  CONSTRAINT pk_processsupervisor PRIMARY KEY (id),
  CONSTRAINT uk_processsupervisor_processdefid_userid_groupid_roleid UNIQUE (processDefId, userId, groupId, roleId)
);

CREATE TABLE page (
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description LONGVARCHAR,
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided BOOLEAN,
  editable BOOLEAN,
  removable BOOLEAN,
  lastModificationDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  contentName VARCHAR(280) NOT NULL,
  content LONGBLOB,
  contentType VARCHAR(50) NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  pageHash VARCHAR(32),
  CONSTRAINT pk_page PRIMARY KEY (id),
  CONSTRAINT uk_page_name_processdefinitionid UNIQUE (name, processDefinitionId)
);

CREATE TABLE profile (
  id BIGINT NOT NULL,
  isDefault BOOLEAN NOT NULL,
  name VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  CONSTRAINT uk_profile_name UNIQUE (name),
  CONSTRAINT pk_profile PRIMARY KEY (id)
);

CREATE TABLE profilemember (
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  CONSTRAINT pk_profilemember PRIMARY KEY (id),
  CONSTRAINT uk_profilemember_profileid_userid_groupid_roleid UNIQUE (profileId, userId, groupId, roleId)
);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_profileid FOREIGN KEY (profileId) REFERENCES profile(id);

CREATE TABLE business_app (
  id BIGINT NOT NULL,
  token VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  iconPath VARCHAR(255),
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  updatedBy BIGINT NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId BIGINT,
  profileId BIGINT,
  layoutId BIGINT,
  themeId BIGINT,
  iconMimeType VARCHAR(255),
  iconContent LONGBLOB,
  displayName VARCHAR(255) NOT NULL,
  editable BOOLEAN,
  internalProfile VARCHAR(255),
  isLink BOOLEAN DEFAULT FALSE,
  CONSTRAINT pk_business_app PRIMARY KEY (id),
  CONSTRAINT uk_business_app_token_version UNIQUE (token, version)
);
ALTER TABLE business_app ADD CONSTRAINT fk_business_app_profileid FOREIGN KEY (profileId) REFERENCES profile (id);
ALTER TABLE business_app ADD CONSTRAINT fk_business_app_layoutid FOREIGN KEY (layoutId) REFERENCES page (id);
ALTER TABLE business_app ADD CONSTRAINT fk_business_app_themeid FOREIGN KEY (themeId) REFERENCES page (id);

CREATE INDEX idx_app_token ON business_app (token);
CREATE INDEX idx_app_profile ON business_app (profileId);
CREATE INDEX idx_app_homepage ON business_app (homePageId);

CREATE TABLE business_app_page (
  id BIGINT NOT NULL,
  applicationId BIGINT NOT NULL,
  pageId BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  CONSTRAINT pk_business_app_page PRIMARY KEY (id),
  CONSTRAINT uk_business_app_page_applicationid_token UNIQUE (applicationId, token)
);
ALTER TABLE business_app_page ADD CONSTRAINT fk_business_app_page_applicationid FOREIGN KEY (applicationId) REFERENCES business_app (id) ON DELETE CASCADE;
ALTER TABLE business_app_page ADD CONSTRAINT fk_business_app_page_pageid FOREIGN KEY (pageId) REFERENCES page (id);

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId);

CREATE TABLE business_app_menu (
  id BIGINT NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  applicationId BIGINT NOT NULL,
  applicationPageId BIGINT,
  parentId BIGINT,
  index_ BIGINT,
  CONSTRAINT pk_business_app_menu PRIMARY KEY (id)
);
-- cannot have both fk_business_app_menu_applicationid and fk_business_app_menu_applicationpageid because this create to path for deletion of business_app_menu elements:
-- business_app -> business_app_menu
-- business_app -> business_app_page -> business_app_menu
-- this is not allowed in SQL Server
ALTER TABLE business_app_menu ADD CONSTRAINT fk_business_app_menu_applicationid FOREIGN KEY (applicationId) REFERENCES business_app (id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_business_app_menu_applicationpageid FOREIGN KEY (applicationPageId) REFERENCES business_app_page (id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_business_app_menu_parentid FOREIGN KEY (parentId) REFERENCES business_app_menu (id);

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId);

CREATE TABLE command (
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  IMPLEMENTATION VARCHAR(100) NOT NULL,
  isSystem BOOLEAN,
  CONSTRAINT pk_command PRIMARY KEY (id),
  CONSTRAINT uk_command_name UNIQUE (name)
);

CREATE TABLE arch_data_instance (
  id BIGINT NOT NULL,
  name VARCHAR(50),
  description VARCHAR(50),
  transientData BOOLEAN,
  className VARCHAR(100),
  containerId BIGINT,
  containerType VARCHAR(60),
  namespace VARCHAR(100),
  element VARCHAR(60),
  intValue INT,
  longValue BIGINT,
  shortTextValue VARCHAR(255),
  booleanValue BOOLEAN,
  doubleValue NUMERIC(19,5),
  floatValue REAL,
  blobValue MEDIUMBLOB,
  clobValue CLOB,
  discriminant VARCHAR(50) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  CONSTRAINT pk_arch_data_instance PRIMARY KEY (id)
);
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id);

CREATE TABLE data_instance (
  id BIGINT NOT NULL,
  name VARCHAR(50),
  description VARCHAR(50),
  transientData BOOLEAN,
  className VARCHAR(100),
  containerId BIGINT,
  containerType VARCHAR(60),
  namespace VARCHAR(100),
  element VARCHAR(60),
  intValue INT,
  longValue BIGINT,
  shortTextValue VARCHAR(255),
  booleanValue BOOLEAN,
  doubleValue NUMERIC(19,5),
  floatValue REAL,
  blobValue MEDIUMBLOB,
  clobValue CLOB,
  discriminant VARCHAR(50) NOT NULL,
  CONSTRAINT pk_data_instance PRIMARY KEY (id)
);
CREATE INDEX idx_datai_container ON data_instance (containerId, containerType, name);

CREATE TABLE dependency (
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description LONGVARCHAR,
  filename VARCHAR(255) NOT NULL,
  value_ LONGVARBINARY NOT NULL,
  CONSTRAINT pk_dependency PRIMARY KEY (id),
  CONSTRAINT uk_dependency_name UNIQUE (name)
);

CREATE TABLE dependencymapping (
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  CONSTRAINT pk_dependencymapping PRIMARY KEY (id),
  CONSTRAINT uk_dependencymapping_dependencyid_artifactid_artifacttype UNIQUE (dependencyid, artifactid, artifacttype)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_dependencymapping_dependencyid FOREIGN KEY (dependencyid) REFERENCES dependency(id) ON DELETE CASCADE;

CREATE TABLE pdependency (
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  filename VARCHAR(255) NOT NULL,
  value_ LONGVARBINARY NOT NULL,
  CONSTRAINT pk_pdependency PRIMARY KEY (id),
  CONSTRAINT uk_pdependency_name UNIQUE (name)
);

CREATE TABLE pdependencymapping (
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  CONSTRAINT pk_pdependencymapping PRIMARY KEY (id),
  CONSTRAINT uk_pdependencymapping_dependencyid_artifactid_artifacttype UNIQUE (dependencyid, artifactid, artifacttype)
);
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdependencymapping_dependencyid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;


CREATE TABLE group_ (
  id BIGINT NOT NULL,
  name VARCHAR(125) NOT NULL,
  parentPath VARCHAR(255),
  displayName VARCHAR(255),
  description LONGVARCHAR,
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  iconid BIGINT,
  CONSTRAINT pk_group PRIMARY KEY (id)
);
CREATE INDEX idx_group_name ON group_ (parentPath, name);

CREATE TABLE role (
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  description LONGVARCHAR,
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  iconid BIGINT,
  CONSTRAINT pk_role PRIMARY KEY (id),
  CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE user_ (
  id BIGINT NOT NULL,
  enabled BOOLEAN NOT NULL,
  userName VARCHAR(255) NOT NULL,
  password VARCHAR(60),
  firstName VARCHAR(255),
  lastName VARCHAR(255),
  title VARCHAR(50),
  jobTitle VARCHAR(255),
  managerUserId BIGINT,
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  iconid BIGINT,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT uk_user_username UNIQUE (userName)
);

CREATE TABLE user_login (
  id BIGINT NOT NULL,
  lastConnection BIGINT,
  CONSTRAINT pk_user_login PRIMARY KEY (id)
);

CREATE TABLE user_contactinfo (
  id BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(50),
  mobile VARCHAR(50),
  fax VARCHAR(50),
  building VARCHAR(50),
  room VARCHAR(50),
  address VARCHAR(255),
  zipCode VARCHAR(50),
  city VARCHAR(255),
  state VARCHAR(255),
  country VARCHAR(255),
  website VARCHAR(255),
  personal BOOLEAN NOT NULL,
  CONSTRAINT pk_user_contactinfo PRIMARY KEY (id),
  CONSTRAINT uk_user_contactinfo_userid_personal UNIQUE (userId, personal)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_user_contactinfo_userid FOREIGN KEY (userId) REFERENCES user_ (id) ON DELETE CASCADE;

CREATE TABLE custom_usr_inf_def (
  id BIGINT NOT NULL,
  name VARCHAR(75) NOT NULL,
  description LONGVARCHAR,
  CONSTRAINT pk_custom_usr_inf_def PRIMARY KEY (id),
  CONSTRAINT uk_custom_usr_inf_def_name UNIQUE (name)
);

CREATE TABLE custom_usr_inf_val (
  id BIGINT NOT NULL,
  definitionId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  value VARCHAR(255),
  CONSTRAINT pk_custom_usr_inf_val PRIMARY KEY (id),
  CONSTRAINT uk_custom_usr_inf_val_definitionid_userid UNIQUE (definitionId, userId)
);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_custom_usr_inf_val_userid FOREIGN KEY (userId) REFERENCES user_ (id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_custom_usr_inf_val_definitionid FOREIGN KEY (definitionId) REFERENCES custom_usr_inf_def (id) ON DELETE CASCADE;

CREATE TABLE user_membership (
  id BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  assignedBy BIGINT,
  assignedDate BIGINT,
  CONSTRAINT pk_user_membership PRIMARY KEY (id),
  CONSTRAINT uk_user_membership_userid_roleid_groupid UNIQUE (userId, roleId, groupId)
);

CREATE TABLE icon (
  id BIGINT NOT NULL,
  mimetype VARCHAR(255) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (id)
);

CREATE TABLE queriable_log (
  id BIGINT NOT NULL,
  log_timestamp BIGINT NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId VARCHAR(255) NOT NULL,
  threadNumber BIGINT NOT NULL,
  clusterNode VARCHAR(50),
  productVersion VARCHAR(50) NOT NULL,
  severity VARCHAR(50) NOT NULL,
  actionType VARCHAR(50) NOT NULL,
  actionScope VARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage VARCHAR(255) NOT NULL,
  callerClassName VARCHAR(200),
  callerMethodName VARCHAR(80),
  numericIndex1 BIGINT,
  numericIndex2 BIGINT,
  numericIndex3 BIGINT,
  numericIndex4 BIGINT,
  numericIndex5 BIGINT,
  CONSTRAINT pk_queriable_log PRIMARY KEY (id)
);

CREATE TABLE sequence (
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  CONSTRAINT pk_sequence PRIMARY KEY (id)
);

CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  initial_bonita_version VARCHAR(50) NOT NULL,
  application_version VARCHAR(50) NOT NULL,
  maintenance_message LONGVARCHAR,
  maintenance_message_active BOOLEAN NOT NULL,
  created BIGINT NOT NULL,
  created_by VARCHAR(50) NOT NULL,
  information CLOB,
  status VARCHAR(15) NOT NULL,
  CONSTRAINT pk_platform PRIMARY KEY (id)
);

CREATE TABLE tenant (
  id BIGINT NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  defaultTenant BOOLEAN NOT NULL,
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE platformCommand (
  id BIGINT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description LONGVARCHAR,
  IMPLEMENTATION VARCHAR(100) NOT NULL
);

CREATE TABLE job_desc (
  id BIGINT NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  CONSTRAINT pk_job_desc PRIMARY KEY (id)
);

CREATE TABLE job_param (
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  CONSTRAINT pk_job_param PRIMARY KEY (id)
);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobdescriptorid FOREIGN KEY (jobDescriptorId) REFERENCES job_desc(id) ON DELETE CASCADE;
CREATE INDEX idx_job_param_jobid ON job_param(jobDescriptorId);

CREATE TABLE job_log (
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  retryNumber BIGINT,
  lastUpdateDate BIGINT,
  lastMessage LONGVARCHAR,
  CONSTRAINT pk_job_log PRIMARY KEY (id),
  CONSTRAINT uk_job_log_jobdescriptorid UNIQUE (jobDescriptorId)
);
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobdescriptorid FOREIGN KEY (jobDescriptorId) REFERENCES job_desc(id) ON DELETE CASCADE;

CREATE TABLE page_mapping (
  id BIGINT NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  pageId BIGINT NULL,
  url VARCHAR(1024) NULL,
  urladapter VARCHAR(255) NULL,
  page_authoriz_rules TEXT NULL,
  lastUpdateDate BIGINT NULL,
  lastUpdatedBy BIGINT NULL,
  CONSTRAINT pk_page_mapping PRIMARY KEY (id),
  CONSTRAINT uk_page_mapping_key UNIQUE (key_)
);

CREATE TABLE form_mapping (
  id BIGINT NOT NULL,
  process BIGINT NOT NULL,
  type INT NOT NULL,
  task VARCHAR(255),
  page_mapping_id BIGINT,
  lastUpdateDate BIGINT,
  lastUpdatedBy BIGINT,
  target VARCHAR(16) NOT NULL,
  CONSTRAINT pk_form_mapping PRIMARY KEY (id)
);

ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_id) REFERENCES page_mapping(id);

CREATE TABLE proc_parameter (
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  value CLOB NULL,
  CONSTRAINT pk_proc_parameter PRIMARY KEY (id)
);

CREATE TABLE bar_resource (
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT pk_bar_resource PRIMARY KEY (id),
  CONSTRAINT uk_bar_resource_processid_name_type UNIQUE (process_id, name, type)
);

CREATE TABLE temporary_content (
  id BIGINT NOT NULL,
  creationDate BIGINT NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  fileName VARCHAR(255) NOT NULL,
  mimeType VARCHAR(255) NOT NULL,
  content LONGBLOB NOT NULL,
  UNIQUE (key_),
  PRIMARY KEY (id)
);
CREATE INDEX idx_temporary_content ON temporary_content (key_);

CREATE TABLE tenant_resource (
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  lastUpdateDate BIGINT,
  state VARCHAR(50) NOT NULL,
  CONSTRAINT pk_tenant_resource PRIMARY KEY (id),
  CONSTRAINT uk_tenant_resource_name_type UNIQUE (name, type)
);

CREATE TABLE bpm_failure (
  id BIGINT NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  processInstanceId BIGINT NOT NULL,
  rootProcessInstanceId BIGINT,
  flowNodeInstanceId BIGINT,
  scope VARCHAR(255),
  context VARCHAR(1024),
  errorMessage VARCHAR(1024),
  stackTrace TEXT,
  failureDate BIGINT NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_flownode_instance_id ON bpm_failure (flowNodeInstanceId);
CREATE INDEX idx_process_instance_id ON bpm_failure (processInstanceId);
CREATE INDEX idx_root_process_instance_id ON bpm_failure (rootProcessInstanceId);
CREATE INDEX idx_process_definition_id ON bpm_failure (processDefinitionId);

CREATE TABLE arch_bpm_failure (
  id BIGINT NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  processInstanceId BIGINT NOT NULL,
  rootProcessInstanceId BIGINT,
  flowNodeInstanceId BIGINT,
  scope VARCHAR(255),
  context VARCHAR(1024),
  errorMessage VARCHAR(1024),
  stackTrace TEXT,
  failureDate BIGINT NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_arch_flownode_instance_id ON arch_bpm_failure (flowNodeInstanceId);
CREATE INDEX idx_arch_process_instance_id ON arch_bpm_failure (processInstanceId);
CREATE INDEX idx_arch_root_process_instance_id ON arch_bpm_failure (rootProcessInstanceId);
CREATE INDEX idx_arch_process_definition_id ON arch_bpm_failure (processDefinitionId);