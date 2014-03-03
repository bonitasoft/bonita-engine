CREATE TABLE platform (
  id NUMBER(19, 0) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  previousVersion VARCHAR2(50),
  initialVersion VARCHAR2(50) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50) NOT NULL,
  description VARCHAR2(255),
  defaultTenant NUMBER(1) NOT NULL,
  iconname VARCHAR2(50),
  iconpath VARCHAR2(255),
  name VARCHAR2(50) NOT NULL,
  status VARCHAR2(15) NOT NULL,
  PRIMARY KEY (id)
);
