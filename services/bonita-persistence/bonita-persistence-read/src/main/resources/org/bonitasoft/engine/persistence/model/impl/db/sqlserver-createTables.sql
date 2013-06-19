CREATE TABLE blob_ (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	blobValue VARBINARY(MAX),
	PRIMARY KEY (tenantid, id)
)
GO

