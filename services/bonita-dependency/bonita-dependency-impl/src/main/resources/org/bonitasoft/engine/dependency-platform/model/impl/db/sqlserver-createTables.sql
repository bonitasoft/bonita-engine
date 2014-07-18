CREATE TABLE pdependency (
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL UNIQUE,
  description NVARCHAR(MAX),
  filename NVARCHAR(255) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependency_name ON pdependency (name, id)
GO

CREATE TABLE pdependencymapping (
  id NUMERIC(19, 0) NOT NULL,
  artifactid NUMERIC(19, 0) NOT NULL,
  artifacttype NVARCHAR(50) NOT NULL,
  dependencyid NUMERIC(19, 0) NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
)
GO
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid, id)
GO
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE
GO
