CREATE TABLE blob_ (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	blobValue BYTEA,
	PRIMARY KEY (tenantid, id)
);

