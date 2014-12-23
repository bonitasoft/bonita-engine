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

CREATE INDEX idx_datai_container ON data_instance (containerId, containerType, tenantId);
