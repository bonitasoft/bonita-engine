CREATE TABLE pdependency (
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;
CREATE INDEX idx_pdependency_name ON pdependency (name);

CREATE TABLE pdependencymapping (
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
) ENGINE = INNODB;
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;