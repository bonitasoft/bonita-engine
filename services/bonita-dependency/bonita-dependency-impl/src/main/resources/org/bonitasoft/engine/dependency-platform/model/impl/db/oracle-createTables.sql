CREATE TABLE pdependency (
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL UNIQUE,
  description VARCHAR2(1024),
  filename VARCHAR2(255) NOT NULL,
  value_ BLOB NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE pdependencymapping (
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  UNIQUE (dependencyid, artifactid, artifacttype),
  PRIMARY KEY (id)
);
ALTER TABLE pdependencymapping ADD CONSTRAINT fk_pdepmapping_depid FOREIGN KEY (dependencyid) REFERENCES pdependency(id) ON DELETE CASCADE;