CREATE TABLE command (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description LONGVARCHAR,
  IMPLEMENTATION VARCHAR(100) NOT NULL,
  system BOOLEAN,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
