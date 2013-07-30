CREATE TABLE blob_ (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	blobValue MEDIUMBLOB,
	PRIMARY KEY (tenantid, id)
);

