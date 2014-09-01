ALTER TABLE document_mapping DROP CONSTRAINT fk_docmap_docid;
DROP TABLE document cascade constraints purge;
DROP TABLE document_mapping cascade constraints purge;
