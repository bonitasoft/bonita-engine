CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
