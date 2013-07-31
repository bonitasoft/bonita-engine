CREATE TABLE queriable_log (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  timeStamp NUMERIC(19, 0) NOT NULL,
  year SMALLINT NOT NULL,
  month TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId NVARCHAR(50) NOT NULL,
  threadNumber NUMERIC(19, 0) NOT NULL,
  clusterNode NVARCHAR(50),
  productVersion NVARCHAR(50) NOT NULL,
  severity NVARCHAR(50) NOT NULL,
  actionType NVARCHAR(50) NOT NULL,
  actionScope NVARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage NVARCHAR(255) NOT NULL,
  callerClassName NVARCHAR(200),
  callerMethodName NVARCHAR(80),
  numericIndex1 NUMERIC(19, 0),
  numericIndex2 NUMERIC(19, 0),
  numericIndex3 NUMERIC(19, 0),
  numericIndex4 NUMERIC(19, 0),
  numericIndex5 NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE queriablelog_p (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  queriableLogId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  stringValue NVARCHAR(255),
  blobId NUMERIC(19, 0),
  valueType NVARCHAR(30),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId, id)
GO
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id)
GO
