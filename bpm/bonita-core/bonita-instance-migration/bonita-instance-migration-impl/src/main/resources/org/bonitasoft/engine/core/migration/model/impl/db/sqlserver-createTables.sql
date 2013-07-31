CREATE TABLE migration_plan (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255) NOT NULL,
  source_name NVARCHAR(50) NOT NULL,
  source_version NVARCHAR(50) NOT NULL,
  target_name NVARCHAR(50) NOT NULL,
  target_version NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
