CREATE TABLE arch_data_instance (
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
	archiveDate NUMERIC(19, 0) NOT NULL,
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE arch_data_mapping (
    tenantid NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	containerId NUMERIC(19, 0),
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId NUMERIC(19, 0) NOT NULL,
	archiveDate NUMERIC(19, 0) NOT NULL,
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
GO