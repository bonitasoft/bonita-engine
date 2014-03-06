CREATE TABLE dependency (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150) NOT NULL,
  description VARCHAR2(1024),
  filename VARCHAR2(255) NOT NULL,
  value_ BLOB NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
