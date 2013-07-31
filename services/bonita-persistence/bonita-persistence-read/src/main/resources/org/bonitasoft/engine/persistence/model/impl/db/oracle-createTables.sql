CREATE TABLE blob_ (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	blobValue BLOB,
	PRIMARY KEY (tenantid, id)
);

