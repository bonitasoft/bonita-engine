ALTER TABLE datasourceparameter DROP CONSTRAINT fk_dsparam_dsid;
DROP TABLE datasourceparameter cascade constraints purge;
DROP TABLE datasource cascade constraints purge;