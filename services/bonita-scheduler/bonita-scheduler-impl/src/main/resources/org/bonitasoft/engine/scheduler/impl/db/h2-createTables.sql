CREATE TABLE job_desc (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_param (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_log (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  jobDescriptorId BIGINT NOT NULL,
  retryNumber BIGINT,
  lastUpdateDate BIGINT,
  lastMessage LONGVARCHAR,
  PRIMARY KEY (tenantid, id, jobDescriptorId)
);

ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id);
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id);