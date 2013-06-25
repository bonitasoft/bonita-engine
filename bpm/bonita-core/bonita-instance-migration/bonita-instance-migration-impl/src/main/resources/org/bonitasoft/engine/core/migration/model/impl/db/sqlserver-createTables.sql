CREATE TABLE migration_plan (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  description VARCHAR(255) NOT NULL,
  source_name VARCHAR(50) NOT NULL,
  source_version VARCHAR(50) NOT NULL,
  target_name VARCHAR(50) NOT NULL,
  target_version VARCHAR(50) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
