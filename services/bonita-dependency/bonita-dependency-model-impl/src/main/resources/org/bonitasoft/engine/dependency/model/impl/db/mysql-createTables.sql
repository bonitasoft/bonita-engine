CREATE TABLE dependency (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
