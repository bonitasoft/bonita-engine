CREATE TABLE platform (
  id NUMERIC(19, 0) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  previousVersion NVARCHAR(50) NOT NULL,
  initialVersion NVARCHAR(50) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
)
GO

CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  defaultTenant BIT NOT NULL,
  iconname NVARCHAR(50),
  iconpath NVARCHAR(255),
  name NVARCHAR(50) NOT NULL,
  status NVARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
)
GO
