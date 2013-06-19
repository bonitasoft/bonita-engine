CREATE TABLE queriable_log (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  timeStamp NUMERIC(19, 0) NOT NULL,
  year SMALLINT NOT NULL,
  month TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId VARCHAR(50) NOT NULL,
  threadNumber NUMERIC(19, 0) NOT NULL,
  clusterNode VARCHAR(50),
  productVersion VARCHAR(50) NOT NULL,
  severity VARCHAR(50) NOT NULL,
  actionType VARCHAR(50) NOT NULL,
  actionScope VARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage VARCHAR(255) NOT NULL,
  callerClassName VARCHAR(200),
  callerMethodName VARCHAR(80),
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
  name VARCHAR(50) NOT NULL,
  stringValue VARCHAR(255),
  blobId NUMERIC(19, 0),
  valueType VARCHAR(30),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId)
GO
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id)
GO
