ALTER TABLE processcategorymapping DROP CONSTRAINT fk_catmapping_catid;
DROP TABLE processcategorymapping cascade constraints purge;
DROP TABLE category cascade constraints purge;
