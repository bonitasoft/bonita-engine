CREATE TABLE pdependency (
  id NUMERIC(19, 0) NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  version VARCHAR(50) NOT NULL,
  description TEXT NULL,
  filename VARCHAR(255) NOT NULL,
  value_ IMAGE NOT NULL,
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependency_name ON pdependency (name)
GO
CREATE INDEX idx_pdependency_version ON pdependency (version)
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
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid)
GO
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY ( dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE
GO