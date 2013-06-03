CREATE TABLE queriableLog (
  tenantid NUMERIC(19,0) NOT NULL,
  id NUMERIC(19,0) NOT NULL,
  timeStamp NUMERIC(19,0) NOT NULL,
  year SMALLINT NOT NULL,
  month TINYINT NOT NULL,
  day TINYINT NOT NULL,
  hour TINYINT NOT NULL, 
  minute TINYINT NOT NULL,
  second TINYINT NOT NULL,
  millisecond SMALLINT NOT NULL,
  dayOfWeek VARCHAR(50) NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId VARCHAR(50) NOT NULL,
  threadNumber NUMERIC(19,0) NOT NULL,
  clusterNode VARCHAR(50) NULL,
  productVersion VARCHAR(50) NOT NULL,
  severity VARCHAR(50) NOT NULL,
  actionType VARCHAR(50) NOT NULL,
  actionScope VARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage TEXT NOT NULL,
  callerClassName VARCHAR(200) NULL,
  callerMethodName VARCHAR(80) NULL,
  numericIndex1 NUMERIC(19,0) NULL,
  numericIndex2 NUMERIC(19,0) NULL,
  numericIndex3 NUMERIC(19,0) NULL,
  numericIndex4 NUMERIC(19,0) NULL,
  numericIndex5 NUMERIC(19,0) NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE queriableLog_p (
  tenantid NUMERIC(19,0) NOT NULL,
  id NUMERIC(19,0) NOT NULL,
  queriableLogId NUMERIC(19,0) NOT NULL,
  name VARCHAR(50) NOT NULL,
  stringValue TEXT NULL,
  blobValue IMAGE NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE INDEX idx_queriableLog ON queriableLog_p (queriableLogId)
GO

ALTER TABLE queriableLog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriableLog(tenantid, id)
GO
