CREATE TABLE process_comment (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content TEXT NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

