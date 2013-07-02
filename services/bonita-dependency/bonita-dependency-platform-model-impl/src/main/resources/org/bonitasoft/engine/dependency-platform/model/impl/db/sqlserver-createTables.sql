CREATE TABLE pdependency (
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(MAX),
  filename VARCHAR(255) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependency_name ON pdependency (name, id)
GO
CREATE INDEX idx_pdependency_version ON pdependency (version, id)
GO

CREATE TABLE pdependencymapping (
  id NUMERIC(19, 0) NOT NULL,
  artifactid NUMERIC(19, 0) NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid NUMERIC(19, 0) NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid, id)
GO
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE
GO