CREATE TABLE job_desc (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE job_param (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobDescriptorId NUMERIC(19, 0) NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id)
GO
