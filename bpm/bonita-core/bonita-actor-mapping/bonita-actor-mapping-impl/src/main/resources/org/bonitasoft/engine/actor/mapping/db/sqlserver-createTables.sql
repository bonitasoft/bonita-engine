CREATE TABLE actor (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
