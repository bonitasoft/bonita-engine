CREATE TABLE document (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  author NUMERIC(19, 0),
  creationdate NUMERIC(19, 0) NOT NULL,
  hascontent BIT NOT NULL,
  filename NVARCHAR(255),
  mimetype NVARCHAR(255),
  url NVARCHAR(1024),
  content VARBINARY(MAX),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0) NOT NULL,
  documentid NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  version NVARCHAR(10) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, ID)
)
GO