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
