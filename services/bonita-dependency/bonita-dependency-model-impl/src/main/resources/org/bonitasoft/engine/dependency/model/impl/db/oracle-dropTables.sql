ALTER TABLE dependencymapping DROP CONSTRAINT fk_depmapping_depid;
DROP TABLE dependencymapping cascade constraints purge;
DROP TABLE dependency cascade constraints purge;
