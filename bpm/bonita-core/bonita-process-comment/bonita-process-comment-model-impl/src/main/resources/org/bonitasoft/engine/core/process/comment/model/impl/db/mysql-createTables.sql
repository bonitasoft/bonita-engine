CREATE TABLE process_comment (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(255) NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
