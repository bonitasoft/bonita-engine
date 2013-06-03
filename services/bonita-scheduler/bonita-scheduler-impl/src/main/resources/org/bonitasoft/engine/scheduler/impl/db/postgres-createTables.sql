CREATE TABLE job_desc (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_param (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobDescriptorId INT8 NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id);
