DELETE FROM document WHERE tenantid = ${tenantid}
GO
DELETE FROM document_mapping WHERE tenantid = ${tenantid}
GO
