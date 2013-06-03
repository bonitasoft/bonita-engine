CREATE TABLE process_comment (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId INT8,
  processInstanceId INT8 NOT NULL,
  postDate INT8 NOT NULL,
  content VARCHAR(255) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
