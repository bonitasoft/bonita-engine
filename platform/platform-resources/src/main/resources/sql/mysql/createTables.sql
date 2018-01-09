CREATE TABLE configuration (
  tenant_id BIGINT NOT NULL,
  content_type VARCHAR(50) NOT NULL,
  resource_name VARCHAR(120) NOT NULL,
  resource_content BLOB
) ENGINE = INNODB;
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name);
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type);

CREATE TABLE contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  val LONGBLOB
) ENGINE = INNODB;
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_cd_scope_name ON contract_data (kind, scopeId, name, tenantid);

CREATE TABLE arch_contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  val LONGBLOB,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL
) ENGINE = INNODB;
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);

CREATE TABLE actor (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(75),
  description TEXT,
  initiator BOOLEAN,
  UNIQUE (tenantid, id, scopeId, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE actormember (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  actorId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, actorid, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE category (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  creator BIGINT,
  description TEXT,
  creationDate BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE processcategorymapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  categoryid BIGINT NOT NULL,
  processid BIGINT NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;

CREATE TABLE arch_process_comment(
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(512) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);

CREATE TABLE process_comment (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(512) NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE process_definition (
  tenantid BIGINT NOT NULL,
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
  content_tenantid BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
) ENGINE = INNODB;
CREATE TABLE process_content (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  content MEDIUMTEXT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);

CREATE TABLE document (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  author BIGINT,
  creationdate BIGINT NOT NULL,
  hascontent BOOLEAN NOT NULL,
  filename VARCHAR(255),
  mimetype VARCHAR(255),
  url VARCHAR(1024),
  content LONGBLOB,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;

CREATE TABLE arch_process_instance (
  tenantid BIGINT NOT NULL,
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
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid BIGINT NOT NULL,
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
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);

CREATE TABLE arch_connector_instance (
  tenantid BIGINT NOT NULL,
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
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);
CREATE TABLE process_instance (
  tenantid BIGINT NOT NULL,
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
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (tenantid, processdefinitionid, stateid);

CREATE TABLE flownode_instance (
  tenantid BIGINT NOT NULL,
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
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid);

CREATE TABLE connector_instance (
  tenantid BIGINT NOT NULL,
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
  stackTrace TEXT,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE event_trigger_instance (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventInstanceId BIGINT NOT NULL,
  	eventInstanceName VARCHAR(50),
  	messageName VARCHAR(255),
  	targetProcess VARCHAR(255),
  	targetFlowNode VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	executionDate BIGINT, 
  	jobTriggerName VARCHAR(255),
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE waiting_event (
	tenantid BIGINT NOT NULL,
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
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);

CREATE TABLE message_instance (
	tenantid BIGINT NOT NULL,
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
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);

CREATE TABLE pending_mapping (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	activityId BIGINT NOT NULL,
  	actorId BIGINT,
  	userId BIGINT,
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id BIGINT,
  	fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
) ENGINE = INNODB;

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE multi_biz_data (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	idx BIGINT NOT NULL,
  	data_id BIGINT NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
) ENGINE = INNODB;

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;

CREATE TABLE arch_ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	orig_proc_inst_id BIGINT,
  	orig_fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);

CREATE TABLE arch_multi_biz_data (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	idx BIGINT NOT NULL,
  	data_id BIGINT NOT NULL
);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id);
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;

CREATE TABLE report (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided BOOLEAN,
  lastModificationDate BIGINT NOT NULL,
  screenshot MEDIUMBLOB,
  content LONGBLOB,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
CREATE TABLE processsupervisor (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processDefId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE business_app (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  token VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description TEXT,
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
  displayName VARCHAR(255) NOT NULL
) ENGINE = INNODB;

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
  token VARCHAR(255) NOT NULL
) ENGINE = INNODB;

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  applicationId BIGINT NOT NULL,
  applicationPageId BIGINT,
  parentId BIGINT,
  index_ BIGINT
) ENGINE = INNODB;

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

CREATE TABLE command (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  IMPLEMENTATION VARCHAR(100) NOT NULL,
  system BOOLEAN,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE arch_data_instance (
    tenantId BIGINT NOT NULL,
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
	floatValue FLOAT,
	blobValue MEDIUMBLOB,
	clobValue MEDIUMTEXT,
	discriminant VARCHAR(50) NOT NULL,
	archiveDate BIGINT NOT NULL,
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);

CREATE TABLE data_instance (
    tenantId BIGINT NOT NULL,
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
	floatValue FLOAT,
	blobValue MEDIUMBLOB,
	clobValue MEDIUMTEXT,
	discriminant VARCHAR(50) NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name);

CREATE TABLE dependency (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
CREATE TABLE pdependency (
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;
CREATE INDEX idx_pdependency_name ON pdependency (name);

CREATE TABLE pdependencymapping (
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
) ENGINE = INNODB;
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;
CREATE TABLE external_identity_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  externalId VARCHAR(50) NOT NULL,
  userId BIGINT NOT NULL,
  groupId BIGINT NOT NULL,
  roleId BIGINT NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE group_ (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(125) NOT NULL,
  parentPath VARCHAR(255),
  displayName VARCHAR(255),
  description TEXT,
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  iconid BIGINT,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_group_name ON group_ (tenantid, parentPath, name);

CREATE TABLE role (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  description TEXT,
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  iconid BIGINT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx_role_name ON role (tenantid, name);

CREATE TABLE user_ (
  tenantid BIGINT NOT NULL,
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
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx_user_name ON user_ (tenantid, userName);

CREATE TABLE user_login (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  lastConnection BIGINT,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE user_contactinfo (
  tenantid BIGINT NOT NULL,
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
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal);


CREATE TABLE custom_usr_inf_def (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(75) NOT NULL,
  description TEXT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);

CREATE TABLE custom_usr_inf_val (
  id BIGINT NOT NULL,
  tenantid BIGINT NOT NULL,
  definitionId BIGINT NOT NULL,
  userId BIGINT NOT NULL,
  value VARCHAR(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
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
) ENGINE = INNODB;
CREATE TABLE queriable_log (
  tenantid BIGINT NOT NULL,
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
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE queriablelog_p (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  queriableLogId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  stringValue VARCHAR(255),
  blobId BIGINT,
  valueType VARCHAR(30),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);
CREATE TABLE page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided BOOLEAN,
  lastModificationDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  contentName VARCHAR(280) NOT NULL,
  content LONGBLOB,
  contentType VARCHAR(50) NOT NULL,
  processDefinitionId BIGINT NOT NULL
) ENGINE = INNODB;

ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantid, name, processDefinitionId);


CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE blob_ (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	blobValue MEDIUMBLOB,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  previousVersion VARCHAR(50) NOT NULL,
  initialVersion VARCHAR(50) NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  information TEXT,
  PRIMARY KEY (id)
) ENGINE = INNODB;

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
) ENGINE = INNODB;
CREATE TABLE platformCommand (
  id BIGINT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  IMPLEMENTATION VARCHAR(100) NOT NULL
) ENGINE = INNODB;
CREATE TABLE profile (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  isDefault BOOLEAN NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;

CREATE TABLE profileentry (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  name VARCHAR(50),
  description TEXT,
  parentId BIGINT,
  index_ BIGINT,
  type VARCHAR(50),
  page VARCHAR(255),
  custom BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;

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
) ENGINE = INNODB;

CREATE TABLE job_desc (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX fk_job_desc_Id_idx ON job_desc(id ASC, tenantid ASC);

CREATE TABLE job_param (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX fk_job_param_jobId_idx ON job_param(jobDescriptorId ASC, tenantid ASC);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;

CREATE TABLE job_log (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  retryNumber BIGINT,
  lastUpdateDate BIGINT,
  lastMessage TEXT,
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX fk_job_log_jobId_idx ON job_log(jobDescriptorId ASC, tenantid ASC);

ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE TABLE theme (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  isDefault BOOLEAN NOT NULL,
  content LONGBLOB NOT NULL,
  cssContent LONGBLOB,
  type VARCHAR(50) NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;

CREATE TABLE form_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process BIGINT NOT NULL,
  type INT NOT NULL,
  task VARCHAR(255),
  page_mapping_tenant_id BIGINT,
  page_mapping_id BIGINT,
  lastUpdateDate BIGINT,
  lastUpdatedBy BIGINT,
  target VARCHAR(16) NOT NULL,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;

CREATE TABLE page_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  pageId BIGINT NULL,
  url VARCHAR(1024) NULL,
  urladapter VARCHAR(255) NULL,
  page_authoriz_rules TEXT NULL,
  lastUpdateDate BIGINT NULL,
  lastUpdatedBy BIGINT NULL,
  PRIMARY KEY (tenantId, id),
  UNIQUE (tenantId, key_)
) ENGINE = INNODB;

ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);

CREATE TABLE proc_parameter (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  value MEDIUMTEXT NULL,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;

CREATE TABLE bar_resource (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

CREATE TABLE tenant_resource (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name);

CREATE TABLE icon (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  mimetype VARCHAR(255) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
