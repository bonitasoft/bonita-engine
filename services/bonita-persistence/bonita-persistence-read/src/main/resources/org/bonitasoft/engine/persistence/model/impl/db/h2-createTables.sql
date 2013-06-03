CREATE TABLE blob_ (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	blobValue BLOB,
	PRIMARY KEY (tenantid, id)
);

