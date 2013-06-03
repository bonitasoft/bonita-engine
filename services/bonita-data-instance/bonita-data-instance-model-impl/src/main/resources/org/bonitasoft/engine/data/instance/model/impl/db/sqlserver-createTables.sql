CREATE TABLE process_instance (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    name VARCHAR(75) NOT NULL,
	description VARCHAR(255),
	transientData varchar(1),
	className varchar(100),
	containerId NUMERIC(19, 0),
	containerType varchar(60),
	namespace varchar(100),
	element varchar(60),
	intValue NUMERIC(19, 0),
	longValue NUMERIC(19, 0),
	shortTextValue VARCHAR(50),
	booleanValue varchar(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	dateValue NUMERIC(19,0),
	blobValue IMAGE,
	clobValue TEXT,
	discriminant VARCHAR(10) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
