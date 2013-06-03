ALTER TABLE queriablelog_p DROP CONSTRAINT fk_queriableLogId;
DROP TABLE queriablelog_p cascade constraints purge;
DROP TABLE queriable_log cascade constraints purge;
