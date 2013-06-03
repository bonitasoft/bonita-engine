CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
