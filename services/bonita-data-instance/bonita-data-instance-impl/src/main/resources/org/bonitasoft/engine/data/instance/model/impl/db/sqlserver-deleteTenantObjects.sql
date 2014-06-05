DELETE FROM data_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM data_instance WHERE tenantid = ${tenantid}
GO