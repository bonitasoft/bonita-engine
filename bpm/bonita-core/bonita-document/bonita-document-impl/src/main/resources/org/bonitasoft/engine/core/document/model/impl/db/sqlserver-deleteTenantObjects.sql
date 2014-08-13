DELETE FROM document_content WHERE tenantid = ${tenantid}
GO
DELETE FROM document_mapping WHERE tenantid = ${tenantid}
GO
