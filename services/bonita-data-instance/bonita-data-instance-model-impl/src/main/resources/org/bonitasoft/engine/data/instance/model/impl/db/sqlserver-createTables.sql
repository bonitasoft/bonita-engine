CREATE TABLE data_instance (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BIT,
	className VARCHAR(100),
	containerId NUMERIC(19, 0),
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue NUMERIC(19, 0),
	shortTextValue VARCHAR(255),
	booleanValue BIT,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue VARBINARY(MAX),
	clobValue VARCHAR(MAX),
	discriminant VARCHAR(50) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_datai_container ON data_instance (containerId, containerType, tenantId)
GO

CREATE TABLE data_mapping (
    tenantid NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	containerId NUMERIC(19, 0),
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId NUMERIC(19, 0) NOT NULL,
	UNIQUE (containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_datamapp_container ON data_mapping (containerId, containerType, tenantId)
GO
