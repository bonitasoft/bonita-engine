ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_archdocmap_docid;
DROP TABLE arch_document_mapping cascade constraints purge;
