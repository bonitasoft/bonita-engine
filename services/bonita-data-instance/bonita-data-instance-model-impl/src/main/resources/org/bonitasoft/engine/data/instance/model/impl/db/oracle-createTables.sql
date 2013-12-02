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
