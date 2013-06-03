CREATE TABLE platform (
  id NUMERIC(19, 0) NOT NULL,
  version VARCHAR(50) NOT NULL,
  previousVersion VARCHAR(50) NOT NULL,
  initialVersion VARCHAR(50) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
)
GO

CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255) NULL,
  defaultTenant CHAR NOT NULL,  
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
)
GO
