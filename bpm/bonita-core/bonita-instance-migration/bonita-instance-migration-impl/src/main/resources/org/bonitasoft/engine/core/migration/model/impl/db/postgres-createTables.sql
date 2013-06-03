CREATE TABLE migration_plan (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  description VARCHAR(255) NOT NULL,
  source_name VARCHAR(50) NOT NULL,
  source_version VARCHAR(50) NOT NULL,
  target_name VARCHAR(50) NOT NULL,
  target_version VARCHAR(50) NOT NULL,
  content BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);
