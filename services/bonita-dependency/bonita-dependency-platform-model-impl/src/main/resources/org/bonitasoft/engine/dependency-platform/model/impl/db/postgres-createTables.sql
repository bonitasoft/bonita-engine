CREATE TABLE pdependency (
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ BYTEA NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX idx_pdependency_name ON pdependency (name);

CREATE TABLE pdependencymapping (
  id INT8 NOT NULL,
  artifactid INT8 NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid INT8 NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
);
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;