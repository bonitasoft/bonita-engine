CREATE TABLE migration_plan (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  description VARCHAR(255) NOT NULL,
  source_name VARCHAR(50) NOT NULL,
  source_version VARCHAR(50) NOT NULL,
  target_name VARCHAR(50) NOT NULL,
  target_version VARCHAR(50) NOT NULL,
  content BLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);
