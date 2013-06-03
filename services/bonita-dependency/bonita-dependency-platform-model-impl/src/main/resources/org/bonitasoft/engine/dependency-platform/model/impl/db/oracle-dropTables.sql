ALTER TABLE pdependencymapping DROP CONSTRAINT fk_pdepmapping_depid;
DROP TABLE pdependencymapping cascade constraints purge;
DROP TABLE pdependency cascade constraints purge;
