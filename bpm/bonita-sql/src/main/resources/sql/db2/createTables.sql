CREATE TABLE contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(60) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  val BLOB(2G)
);
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_cd_scope_name ON contract_data (kind, scopeId, name, tenantid);

CREATE TABLE arch_contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(60) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  val BLOB(2G),
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);

CREATE TABLE actor (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  displayName VARCHAR(225),
  description VARCHAR(3072),
  initiator SMALLINT,
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE actormember (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  actorId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE category (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator BIGINT,
  description VARCHAR(3072),
  creationDate BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE processcategorymapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  categoryid BIGINT NOT NULL,
  processid BIGINT NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;
CREATE TABLE arch_process_comment(
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(1536) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_process_comment on arch_process_comment (tenantid, sourceobjectid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);
CREATE TABLE process_comment (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(75) NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(1536) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE process_definition (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processId BIGINT NOT NULL,
  name VARCHAR(450) NOT NULL,
  version VARCHAR(450) NOT NULL,
  description VARCHAR(765),
  deploymentDate BIGINT NOT NULL,
  deployedBy BIGINT NOT NULL,
  activationState VARCHAR(90) NOT NULL,
  configurationState VARCHAR(90) NOT NULL,
  migrationDate BIGINT,
  displayName VARCHAR(225),
  displayDescription VARCHAR(765),
  lastUpdateDate BIGINT,
  categoryId BIGINT,
  iconPath VARCHAR(765),
  content_tenantid BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
);
CREATE TABLE process_content (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  content CLOB(16M) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(3072),
  version VARCHAR(30) NOT NULL,
  index_ INT NOT NULL,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);
CREATE TABLE document (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  author BIGINT,
  creationdate BIGINT NOT NULL,
  hascontent SMALLINT NOT NULL,
  filename VARCHAR(765),
  mimetype VARCHAR(765),
  url VARCHAR(3072),
  content BLOB(2G),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(3072),
  version VARCHAR(30) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_process_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(225) NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  description VARCHAR(765),
  startDate BIGINT NOT NULL,
  startedBy BIGINT NOT NULL,
  startedBySubstitute BIGINT NOT NULL,
  endDate BIGINT NOT NULL,
  archiveDate BIGINT NOT NULL,
  stateId INT NOT NULL,
  lastUpdate BIGINT NOT NULL,
  rootProcessInstanceId BIGINT,
  callerId BIGINT,
  migration_plan BIGINT,
  sourceObjectId BIGINT NOT NULL,
  stringIndex1 VARCHAR(765),
  stringIndex2 VARCHAR(765),
  stringIndex3 VARCHAR(765),
  stringIndex4 VARCHAR(765),
  stringIndex5 VARCHAR(765),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  flownodeDefinitionId BIGINT NOT NULL,
  kind VARCHAR(75) NOT NULL,
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  name VARCHAR(765) NOT NULL,
  displayName VARCHAR(765) ,
  displayDescription VARCHAR(765),
  stateId INT NOT NULL,
  stateName VARCHAR(150),
  terminal SMALLINT NOT NULL,
  stable SMALLINT ,
  actorId BIGINT ,
  assigneeId BIGINT DEFAULT 0 NOT NULL,
  reachedStateDate BIGINT,
  lastUpdateDate BIGINT,
  expectedEndDate BIGINT,
  claimedDate BIGINT,
  priority SMALLINT,
  gatewayType VARCHAR(150),
  hitBys VARCHAR(765),
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR(765),
  loopDataOutputRef VARCHAR(765),
  description VARCHAR(765),
  sequential SMALLINT,
  dataInputItemRef VARCHAR(765),
  dataOutputItemRef VARCHAR(765),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy BIGINT,
  executedBySubstitute BIGINT,
  activityInstanceId BIGINT,
  aborting SMALLINT NOT NULL,
  triggeredByEvent SMALLINT,
  interrupting SMALLINT,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_transition_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  source BIGINT,
  target BIGINT,
  state VARCHAR(150),
  terminal SMALLINT NOT NULL,
  stable SMALLINT ,
  stateCategory VARCHAR(150) NOT NULL,
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  description VARCHAR(765),
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid);

CREATE TABLE arch_connector_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(30) NOT NULL,
  connectorId VARCHAR(765) NOT NULL,
  version VARCHAR(30) NOT NULL,
  name VARCHAR(765) NOT NULL,
  activationEvent VARCHAR(90),
  state VARCHAR(150),
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);
CREATE TABLE process_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(225) NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  description VARCHAR(765),
  startDate BIGINT NOT NULL,
  startedBy BIGINT NOT NULL,
  startedBySubstitute BIGINT NOT NULL,
  endDate BIGINT NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(150) NOT NULL,
  lastUpdate BIGINT NOT NULL,
  containerId BIGINT,
  rootProcessInstanceId BIGINT,
  callerId BIGINT,
  callerType VARCHAR(150),
  interruptingEventId BIGINT,
  migration_plan BIGINT,
  stringIndex1 VARCHAR(765),
  stringIndex2 VARCHAR(765),
  stringIndex3 VARCHAR(765),
  stringIndex4 VARCHAR(765),
  stringIndex5 VARCHAR(765),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (tenantid, processdefinitionid, stateid);

CREATE TABLE flownode_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  flownodeDefinitionId BIGINT NOT NULL,
  kind VARCHAR(75) NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  name VARCHAR(765) NOT NULL,
  displayName VARCHAR(765),
  displayDescription VARCHAR(765),
  stateId INT NOT NULL,
  stateName VARCHAR(150),
  prev_state_id INT NOT NULL,
  terminal SMALLINT NOT NULL,
  stable SMALLINT ,
  actorId BIGINT ,
  assigneeId BIGINT DEFAULT 0 NOT NULL,
  reachedStateDate BIGINT,
  lastUpdateDate BIGINT,
  expectedEndDate BIGINT,
  claimedDate BIGINT,
  priority SMALLINT,
  gatewayType VARCHAR(150),
  hitBys VARCHAR(765),
  stateCategory VARCHAR(150) NOT NULL,
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR(765),
  sequential SMALLINT,
  loopDataInputRef VARCHAR(765),
  loopDataOutputRef VARCHAR(765),
  dataInputItemRef VARCHAR(765),
  dataOutputItemRef VARCHAR(765),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy BIGINT,
  executedBySubstitute BIGINT,
  activityInstanceId BIGINT,
  state_executing SMALLINT DEFAULT 0,
  abortedByBoundary BIGINT,
  triggeredByEvent SMALLINT,
  interrupting SMALLINT,
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (tenantid, logicalGroup4);
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid);

CREATE TABLE connector_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(30) NOT NULL,
  connectorId VARCHAR(765) NOT NULL,
  version VARCHAR(30) NOT NULL,
  name VARCHAR(765) NOT NULL,
  activationEvent VARCHAR(90),
  state VARCHAR(150),
  executionOrder INT,
  exceptionMessage VARCHAR(765),
  stackTrace CLOB(2G),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(45) NOT NULL,
  	eventInstanceId BIGINT NOT NULL,
  	eventInstanceName VARCHAR(150),
  	messageName VARCHAR(765),
  	targetProcess VARCHAR(765),
  	targetFlowNode VARCHAR(765),
  	signalName VARCHAR(765),
  	errorCode VARCHAR(765),
  	executionDate BIGINT, 
  	jobTriggerName VARCHAR(765),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_event_trigger_instance ON event_trigger_instance (tenantid, eventInstanceId);

CREATE TABLE waiting_event (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(45) NOT NULL,
  	eventType VARCHAR(150),
  	messageName VARCHAR(765),
  	signalName VARCHAR(765),
  	errorCode VARCHAR(765),
  	processName VARCHAR(450),
  	flowNodeName VARCHAR(150),
  	flowNodeDefinitionId BIGINT,
  	subProcessId BIGINT,
  	processDefinitionId BIGINT,
  	rootProcessInstanceId BIGINT,
  	parentProcessInstanceId BIGINT,
  	flowNodeInstanceId BIGINT,
  	relatedActivityInstanceId BIGINT,
  	locked SMALLINT,
  	active SMALLINT,
  	progress SMALLINT,
  	correlation1 VARCHAR(384),
  	correlation2 VARCHAR(384),
  	correlation3 VARCHAR(384),
  	correlation4 VARCHAR(384),
  	correlation5 VARCHAR(384),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);
CREATE INDEX idx_waiting_event2 ON waiting_event (tenantid, parentProcessInstanceId, subProcessId, active);

CREATE TABLE message_instance (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	messageName VARCHAR(765) NOT NULL,
  	targetProcess VARCHAR(765) NOT NULL,
  	targetFlowNode VARCHAR(765),
  	locked SMALLINT NOT NULL,
  	handled SMALLINT NOT NULL,
  	processDefinitionId BIGINT NOT NULL,
  	flowNodeName VARCHAR(765),
  	correlation1 VARCHAR(384),
  	correlation2 VARCHAR(384),
  	correlation3 VARCHAR(384),
  	correlation4 VARCHAR(384),
  	correlation5 VARCHAR(384),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);

CREATE TABLE pending_mapping (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	activityId BIGINT NOT NULL,
  	actorId BIGINT,
  	userId BIGINT,
  	PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE breakpoint (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR(765) NOT NULL,
  	inst_scope SMALLINT NOT NULL,
  	inst_id BIGINT NOT NULL,
  	def_id BIGINT NOT NULL,
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(45) NOT NULL,
  	name VARCHAR(765) NOT NULL,
  	proc_inst_id BIGINT,
  	fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(765) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);


ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
CREATE INDEX uk_ref_biz_data_inst ON ref_biz_data_inst (name, proc_inst_id, fn_inst_id, tenantid);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE multi_biz_data (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	idx BIGINT NOT NULL,
  	data_id BIGINT NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
);

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;
CREATE TABLE report (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(3072),
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided SMALLINT,
  lastModificationDate BIGINT NOT NULL,
  screenshot BLOB(2G),
  content BLOB(2G),
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE processsupervisor (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processDefId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE business_app (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  token VARCHAR(150) NOT NULL,
  version VARCHAR(150) NOT NULL,
  description VARCHAR(3072),
  iconPath VARCHAR(765),
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  updatedBy BIGINT NOT NULL,
  state VARCHAR(90) NOT NULL,
  homePageId BIGINT,
  profileId BIGINT,
  layoutId BIGINT,
  themeId BIGINT,
  displayName VARCHAR(765) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version);

CREATE INDEX idx_app_token ON business_app (token, tenantid);
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid);
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid);

CREATE TABLE business_app_page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  applicationId BIGINT NOT NULL,
  pageId BIGINT NOT NULL,
  token VARCHAR(765) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  displayName VARCHAR(765) NOT NULL,
  applicationId BIGINT NOT NULL,
  applicationPageId BIGINT,
  parentId BIGINT,
  index_ BIGINT
);

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

CREATE TABLE command (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(3072),
  IMPLEMENTATION VARCHAR(300) NOT NULL,
  system SMALLINT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_data_instance (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	name VARCHAR(150),
	description VARCHAR(150),
	transientData SMALLINT,
	className VARCHAR(300),
	containerId BIGINT,
	containerType VARCHAR(180),
	namespace VARCHAR(300),
	element VARCHAR(180),
	intValue INT,
	longValue BIGINT,
	shortTextValue VARCHAR(765),
	booleanValue SMALLINT,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB(2G),
	clobValue CLOB(2G),
	discriminant VARCHAR(150) NOT NULL,
	archiveDate BIGINT NOT NULL,
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);

CREATE TABLE data_instance (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	name VARCHAR(150),
	description VARCHAR(150),
	transientData SMALLINT,
	className VARCHAR(300),
	containerId BIGINT,
	containerType VARCHAR(180),
	namespace VARCHAR(300),
	element VARCHAR(180),
	intValue INT,
	longValue BIGINT,
	shortTextValue VARCHAR(765),
	booleanValue SMALLINT,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB(2G),
	clobValue CLOB(2G),
	discriminant VARCHAR(150) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name);

CREATE TABLE dependency (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(450) NOT NULL,
  description VARCHAR(3072),
  filename VARCHAR(765) NOT NULL,
  value_ BLOB(2G) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(150) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
CREATE TABLE pdependency (
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL UNIQUE,
  description VARCHAR(3072),
  filename VARCHAR(765) NOT NULL,
  value_ BLOB(2G) NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_pdependency_name ON pdependency (name);

CREATE TABLE pdependencymapping (
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(150) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
);
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;
CREATE TABLE external_identity_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(75) NOT NULL,
  externalId VARCHAR(150) NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE group_ (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(375) NOT NULL,
  parentPath VARCHAR(765),
  displayName VARCHAR(765),
  description VARCHAR(3072),
  iconName VARCHAR(150),
  iconPath VARCHAR(150),
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_g ON group_ (tenantid, parentPath, name);

CREATE TABLE role (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(765) NOT NULL,
  displayName VARCHAR(765),
  description VARCHAR(3072),
  iconName VARCHAR(150),
  iconPath VARCHAR(150),
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_role_name ON role (tenantid, name);

CREATE TABLE user_ (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  enabled SMALLINT NOT NULL,
  userName VARCHAR(765) NOT NULL,
  password VARCHAR(180),
  firstName VARCHAR(765),
  lastName VARCHAR(765),
  title VARCHAR(150),
  jobTitle VARCHAR(765),
  managerUserId BIGINT,
  iconName VARCHAR(150),
  iconPath VARCHAR(150),
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_user_name ON user_ (tenantid, userName);

CREATE TABLE user_login (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  lastConnection BIGINT,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE user_contactinfo (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  email VARCHAR(765),
  phone VARCHAR(150),
  mobile VARCHAR(150),
  fax VARCHAR(150),
  building VARCHAR(150),
  room VARCHAR(150),
  address VARCHAR(765),
  zipCode VARCHAR(150),
  city VARCHAR(765),
  state VARCHAR(765),
  country VARCHAR(765),
  website VARCHAR(765),
  personal SMALLINT NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal);


CREATE TABLE custom_usr_inf_def (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(225) NOT NULL,
  description VARCHAR(3072),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);

CREATE TABLE custom_usr_inf_val (
  id BIGINT NOT NULL,
  tenantid BIGINT NOT NULL,
  definitionId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  value VARCHAR(765),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE;

CREATE TABLE user_membership (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  assignedBy BIGINT,
  assignedDate BIGINT,
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE queriable_log (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  timeStamp BIGINT NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth SMALLINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear SMALLINT NOT NULL,
  userId VARCHAR(765) NOT NULL,
  threadNumber BIGINT NOT NULL,
  clusterNode VARCHAR(150),
  productVersion VARCHAR(150) NOT NULL,
  severity VARCHAR(150) NOT NULL,
  actionType VARCHAR(150) NOT NULL,
  actionScope VARCHAR(300),
  actionStatus SMALLINT NOT NULL,
  rawMessage VARCHAR(765) NOT NULL,
  callerClassName VARCHAR(600),
  callerMethodName VARCHAR(240),
  numericIndex1 BIGINT,
  numericIndex2 BIGINT,
  numericIndex3 BIGINT,
  numericIndex4 BIGINT,
  numericIndex5 BIGINT,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE queriablelog_p (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  queriableLogId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  stringValue VARCHAR(765),
  blobId BIGINT,
  valueType VARCHAR(90),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);
CREATE TABLE page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  displayName VARCHAR(765) NOT NULL,
  description VARCHAR(3072),
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided SMALLINT,
  lastModificationDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  contentName VARCHAR(150) NOT NULL,
  content BLOB(2G),
  contentType VARCHAR(150) NOT NULL,
  processDefinitionId BIGINT
);
ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
CREATE INDEX uk_page ON page (tenantId, name, processDefinitionId);

CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE blob_ (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	blobValue BLOB(2G),
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(150) NOT NULL,
  previousVersion VARCHAR(150) NOT NULL,
  initialVersion VARCHAR(150) NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(150) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tenant (
  id BIGINT NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(150) NOT NULL,
  description VARCHAR(765),
  defaultTenant SMALLINT NOT NULL,
  iconname VARCHAR(150),
  iconpath VARCHAR(765),
  name VARCHAR(150) NOT NULL,
  status VARCHAR(45) NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE platformCommand (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(150) NOT NULL UNIQUE,
  description VARCHAR(3072),
  IMPLEMENTATION VARCHAR(300) NOT NULL
);
CREATE TABLE profile (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  isDefault SMALLINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(3072),
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  name VARCHAR(150),
  description VARCHAR(3072),
  parentId BIGINT,
  index_ BIGINT,
  type VARCHAR(150),
  page VARCHAR(150),
  custom SMALLINT DEFAULT 0,
  PRIMARY KEY (tenantId, id)
);

CREATE INDEX indexProfileEntry ON profileentry(tenantId, parentId, profileId);

CREATE TABLE profilemember (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE job_desc (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobclassname VARCHAR(300) NOT NULL,
  jobname VARCHAR(300) NOT NULL,
  description VARCHAR(150),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_param (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  key_ VARCHAR(150) NOT NULL,
  value_ BLOB(2G) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_log (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  retryNumber BIGINT,
  lastUpdateDate BIGINT,
  lastMessage CLOB(2G),
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE TABLE theme (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  isDefault SMALLINT NOT NULL,
  content BLOB(2G) NOT NULL,
  cssContent BLOB(2G),
  type VARCHAR(150) NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE form_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process BIGINT NOT NULL,
  type INT NOT NULL,
  task VARCHAR(765),
  page_mapping_tenant_id BIGINT,
  page_mapping_id BIGINT,
  lastUpdateDate BIGINT,
  lastUpdatedBy BIGINT,
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE page_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  key_ VARCHAR(765) NOT NULL,
  pageId BIGINT,
  url VARCHAR(3072),
  urladapter VARCHAR(765),
  page_authoriz_rules VARCHAR(3072),
  lastUpdateDate BIGINT,
  lastUpdatedBy BIGINT,
  PRIMARY KEY (tenantId, id),
  UNIQUE (tenantId, key_)
);

ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);
