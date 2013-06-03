CREATE TABLE document_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processdefinitionid NUMERIC(19, 0) NOT NULL,
  processinstanceid NUMERIC(19, 0),
  activityid NUMERIC(19, 0),
  documentid VARCHAR(50) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
