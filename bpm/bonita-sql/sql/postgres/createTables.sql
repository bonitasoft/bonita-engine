CREATE TABLE actor (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  initiator BOOLEAN,
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE actormember (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  actorId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE category (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator INT8,
  description TEXT,
  creationDate INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE processcategorymapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  categoryid INT8 NOT NULL,
  processid INT8 NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;
CREATE TABLE migration_plan (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  description VARCHAR(255) NOT NULL,
  source_name VARCHAR(50) NOT NULL,
  source_version VARCHAR(50) NOT NULL,
  target_name VARCHAR(50) NOT NULL,
  target_version VARCHAR(50) NOT NULL,
  content BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_process_comment(
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8,
  processInstanceId INT8 NOT NULL,
  postDate INT8 NOT NULL,
  content VARCHAR(255) NOT NULL,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_process_comment on arch_process_comment (tenantid, sourceobjectid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (tenantid, processinstanceid, archivedate);
CREATE TABLE process_comment (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId INT8,
  processInstanceId INT8 NOT NULL,
  postDate INT8 NOT NULL,
  content VARCHAR(255) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE process_definition (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processId INT8 NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate INT8 NOT NULL,
  deployedBy INT8 NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  migrationDate INT8,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate INT8,
  categoryId INT8,
  iconPath VARCHAR(255),
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
);
CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processinstanceid INT8,
  sourceObjectId INT8,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor INT8,
  documentCreationDate INT8 NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
CREATE TABLE document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processinstanceid INT8,
  documentName VARCHAR(50) NOT NULL,
  documentAuthor INT8,
  documentCreationDate INT8 NOT NULL,
  documentHasContent BOOLEAN NOT NULL,
  documentContentFileName VARCHAR(255),
  documentContentMimeType VARCHAR(255),
  contentStorageId VARCHAR(50),
  documentURL VARCHAR(255),
  PRIMARY KEY (tenantid, ID)
);
CREATE TABLE arch_process_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedBySubstitute INT8 NOT NULL,
  endDate INT8 NOT NULL,
  archiveDate INT8 NOT NULL,
  stateId INT NOT NULL,
  lastUpdate INT8 NOT NULL,
  rootProcessInstanceId INT8,
  callerId INT8,
  migration_plan INT8,
  sourceObjectId INT8 NOT NULL,
  stringIndex1 VARCHAR(50),
  stringIndex2 VARCHAR(50),
  stringIndex3 VARCHAR(50),
  stringIndex4 VARCHAR(50),
  stringIndex5 VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId INT8 NULL,
  assigneeId INT8 DEFAULT 0 NOT NULL,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
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
  executedBy INT8,
  executedBySubstitute INT8,
  activityInstanceId INT8,
  aborting BOOLEAN NOT NULL,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_transition_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  source INT8,
  target INT8,
  state VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
  description VARCHAR(255),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid);

CREATE TABLE arch_connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);
CREATE TABLE process_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedBySubstitute INT8 NOT NULL,
  endDate INT8 NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(50) NOT NULL,
  lastUpdate INT8 NOT NULL,
  containerId INT8,
  rootProcessInstanceId INT8,
  callerId INT8,
  callerType VARCHAR(50),
  interruptingEventId INT8,
  migration_plan INT8,
  stringIndex1 VARCHAR(50),
  stringIndex2 VARCHAR(50),
  stringIndex3 VARCHAR(50),
  stringIndex4 VARCHAR(50),
  stringIndex5 VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE token (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processInstanceId INT8 NOT NULL,
  ref_id INT8 NOT NULL,
  parent_ref_id INT8 NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_token ON token(tenantid,processInstanceId);

CREATE TABLE flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId INT8 NULL,
  assigneeId INT8 DEFAULT 0 NOT NULL,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
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
  executedBy INT8,
  executedBySubstitute INT8,
  activityInstanceId INT8,
  state_executing BOOLEAN DEFAULT FALSE,
  abortedByBoundary INT8,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  deleted BOOLEAN DEFAULT FALSE,
  tokenCount INT NOT NULL,
  token_ref_id INT8 NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);

CREATE TABLE connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  executionOrder INT,
  exceptionMessage VARCHAR(255),
  stackTrace TEXT,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	eventInstanceId INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	timerType VARCHAR(10),
  	timerValue INT8,
  	messageName VARCHAR(255),
  	targetProcess VARCHAR(255),
  	targetFlowNode VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE waiting_event (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventType VARCHAR(50),
  	messageName VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	processName VARCHAR(150),
  	flowNodeName VARCHAR(50),
  	flowNodeDefinitionId INT8,
  	subProcessId INT8,
  	processDefinitionId INT8,
  	rootProcessInstanceId INT8,
  	parentProcessInstanceId INT8,
  	flowNodeInstanceId INT8,
  	relatedActivityInstanceId INT8,
  	locked BOOLEAN,
  	active BOOLEAN,
  	progress SMALLINT,
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);

CREATE TABLE message_instance (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	messageName VARCHAR(255) NOT NULL,
  	targetProcess VARCHAR(255) NOT NULL,
  	targetFlowNode VARCHAR(255) NULL,
  	locked BOOLEAN NOT NULL,
  	handled BOOLEAN NOT NULL,
  	processDefinitionId INT8 NOT NULL,
  	flowNodeName VARCHAR(255),
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);

CREATE TABLE pending_mapping (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	activityId INT8 NOT NULL,
  	actorId INT8,
  	userId INT8,
  	PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE hidden_activity (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	activityId INT8 NOT NULL,
  	userId INT8 NOT NULL,
  	UNIQUE (tenantid, activityId, userId),
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE breakpoint (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR(255) NOT NULL,
  	inst_scope BOOLEAN NOT NULL,
  	inst_id INT8 NOT NULL,
  	def_id INT8 NOT NULL,
  	PRIMARY KEY (tenantid, id)
);

CREATE TABLE ref_biz_data_inst (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id INT8,
  	fn_inst_id INT8,
  	data_id INT8,
  	data_classname VARCHAR(255) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);


ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data_inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantid);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE multi_biz_data (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	idx INT8 NOT NULL,
  	data_id INT8 NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
);

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;
CREATE TABLE report (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  installationDate INT8 NOT NULL,
  installedBy INT8 NOT NULL,
  provided BOOLEAN,
  lastModificationDate INT8 NOT NULL,
  screenshot BYTEA,
  content BYTEA,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE processsupervisor (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processDefId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE business_app (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  path VARCHAR(255) NOT NULL,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  updatedBy INT8 NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId INT8,
  displayName VARCHAR(255) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version);

CREATE INDEX idx_app_name ON business_app (name, tenantid);

CREATE TABLE business_app_page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  name VARCHAR(255) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_name UNIQUE (tenantId, applicationId, name);

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

-- forein keys are create in bonita-persistence-db/postCreateStructure.sql
CREATE TABLE command (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  IMPLEMENTATION VARCHAR(100) NOT NULL,
  system BOOLEAN,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE arch_data_instance (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BOOLEAN,
	className VARCHAR(100),
	containerId INT8,
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue INT8,
	shortTextValue VARCHAR(255),
	booleanValue BOOLEAN,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BYTEA,
	clobValue TEXT,
	discriminant VARCHAR(50) NOT NULL,
	archiveDate INT8 NOT NULL,
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId,containerId, sourceObjectId);

CREATE TABLE arch_data_mapping (
    tenantid INT8 NOT NULL,
	id INT8 NOT NULL,
	containerId INT8,
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId INT8 NOT NULL,
	archiveDate INT8 NOT NULL,
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx1_arch_data_mapping ON arch_data_mapping (tenantId,containerId, dataInstanceId, sourceObjectId);
CREATE INDEX idx2_arch_data_mapping ON arch_data_mapping (tenantid, containerId, containerType);
CREATE TABLE data_instance (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BOOLEAN,
	className VARCHAR(100),
	containerId INT8,
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue INT8,
	shortTextValue VARCHAR(255),
	booleanValue BOOLEAN,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BYTEA,
	clobValue TEXT,
	discriminant VARCHAR(50) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datai_container ON data_instance (containerId, containerType, tenantId);

CREATE TABLE data_mapping (
    tenantid INT8 NOT NULL,
	id INT8 NOT NULL,
	containerId INT8,
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId INT8 NOT NULL,
	UNIQUE (tenantId, containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datamapp_container ON data_mapping (containerId, containerType, tenantId);
CREATE TABLE dependency (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ BYTEA NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  artifactid INT8 NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid INT8 NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
CREATE TABLE pdependency (
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ BYTEA NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_pdependency_name ON pdependency (name);

CREATE TABLE pdependencymapping (
  id INT8 NOT NULL,
  artifactid INT8 NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid INT8 NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
);
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;
CREATE TABLE document_content (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  documentId VARCHAR(50) NOT NULL,
  content BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE external_identity_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  externalId VARCHAR(50) NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE group_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  parentPath VARCHAR(255),
  displayName VARCHAR(75),
  description TEXT,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  UNIQUE (tenantid, parentPath, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE role (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_role_name ON role (tenantid, name);

CREATE TABLE user_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  enabled BOOLEAN NOT NULL,
  userName VARCHAR(50) NOT NULL,
  password VARCHAR(60),
  firstName VARCHAR(50),
  lastName VARCHAR(50),
  title VARCHAR(50),
  jobTitle VARCHAR(50),
  managerUserId INT8,
  delegeeUserName VARCHAR(50),
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  lastConnection INT8,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_user_name ON user_ (tenantid, userName);

CREATE TABLE user_contactinfo (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8 NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(50),
  mobile VARCHAR(50),
  fax VARCHAR(50),
  building VARCHAR(50),
  room VARCHAR(50),
  address VARCHAR(50),
  zipCode VARCHAR(50),
  city VARCHAR(50),
  state VARCHAR(50),
  country VARCHAR(50),
  website VARCHAR(50),
  personal BOOLEAN NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal);


CREATE TABLE custom_usr_inf_def (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  description TEXT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);

CREATE TABLE custom_usr_inf_val (
  id INT8 NOT NULL,
  tenantid INT8 NOT NULL,
  definitionId INT8 NOT NULL,
  userId INT8 NOT NULL,
  value VARCHAR(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE;

CREATE TABLE user_membership (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  assignedBy INT8,
  assignedDate INT8,
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE queriable_log (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  timeStamp INT8 NOT NULL,
  year SMALLINT NOT NULL,
  month SMALLINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear SMALLINT NOT NULL,
  userId VARCHAR(50) NOT NULL,
  threadNumber INT8 NOT NULL,
  clusterNode VARCHAR(50),
  productVersion VARCHAR(50) NOT NULL,
  severity VARCHAR(50) NOT NULL,
  actionType VARCHAR(50) NOT NULL,
  actionScope VARCHAR(100),
  actionStatus SMALLINT NOT NULL,
  rawMessage VARCHAR(255) NOT NULL,
  callerClassName VARCHAR(200),
  callerMethodName VARCHAR(80),
  numericIndex1 INT8,
  numericIndex2 INT8,
  numericIndex3 INT8,
  numericIndex4 INT8,
  numericIndex5 INT8,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE queriablelog_p (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  queriableLogId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  stringValue VARCHAR(255),
  blobId INT8,
  valueType VARCHAR(30),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);
CREATE TABLE page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate INT8 NOT NULL,
  installedBy INT8 NOT NULL,
  provided BOOLEAN,
  lastModificationDate INT8 NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  contentName VARCHAR(50) NOT NULL,
  content BYTEA,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE blob_ (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	blobValue BYTEA,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE platform (
  id INT8 NOT NULL,
  version VARCHAR(50) NOT NULL,
  previousVersion VARCHAR(50) NOT NULL,
  initialVersion VARCHAR(50) NOT NULL,
  created INT8 NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tenant (
  id INT8 NOT NULL,
  created INT8 NOT NULL,
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
  id INT8 PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  IMPLEMENTATION VARCHAR(100) NOT NULL
);
CREATE TABLE profile (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  isDefault BOOLEAN NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profileentry (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  profileId INT8 NOT NULL,
  name VARCHAR(50),
  description TEXT,
  parentId INT8,
  index_ INT8,
  type VARCHAR(50),
  page VARCHAR(50),
  custom BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (tenantId, id)
);

CREATE INDEX indexProfileEntry ON profileentry(tenantId, parentId, profileId);

CREATE TABLE profilemember (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  profileId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);
CREATE TABLE job_desc (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_param (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobDescriptorId INT8 NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_log (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobDescriptorId INT8 NOT NULL,
  retryNumber INT8,
  lastUpdateDate INT8,
  lastMessage TEXT,
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantid, id)
);

ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE TABLE theme (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  isDefault BOOLEAN NOT NULL,
  content BYTEA NOT NULL,
  cssContent BYTEA,
  type VARCHAR(50) NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);
