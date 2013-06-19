CREATE TABLE command (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(MAX),
  IMPLEMENTATION VARCHAR(100) NOT NULL,
  system BIT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
GO
