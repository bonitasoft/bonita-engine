CREATE TABLE sequence (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    nextid NUMERIC(19, 0) NOT NULL,
    PRIMARY KEY (tenantid, id)
)
GO
