CREATE TABLE dependency (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(100 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  filename VARCHAR2(255 CHAR) NOT NULL,
  value_ BLOB NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50 CHAR) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;
