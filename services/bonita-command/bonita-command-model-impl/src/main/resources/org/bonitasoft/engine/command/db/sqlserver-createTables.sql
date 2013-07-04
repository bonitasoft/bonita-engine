CREATE TABLE command (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  IMPLEMENTATION NVARCHAR(100) NOT NULL,
  system BIT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO
