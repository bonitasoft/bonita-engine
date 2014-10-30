CREATE TABLE queriable_log (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  timeStamp BIGINT NOT NULL,
  year SMALLINT NOT NULL,
  month TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId VARCHAR(255) NOT NULL,
  threadNumber BIGINT NOT NULL,
  clusterNode VARCHAR(50),
  productVersion VARCHAR(50) NOT NULL,
  severity VARCHAR(50) NOT NULL,
  actionType VARCHAR(50) NOT NULL,
  actionScope VARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage VARCHAR(255) NOT NULL,
  callerClassName VARCHAR(200),
  callerMethodName VARCHAR(80),
  numericIndex1 BIGINT,
  numericIndex2 BIGINT,
  numericIndex3 BIGINT,
  numericIndex4 BIGINT,
  numericIndex5 BIGINT,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE queriablelog_p (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  queriableLogId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  stringValue VARCHAR(255),
  blobId BIGINT,
  valueType VARCHAR(30),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);
