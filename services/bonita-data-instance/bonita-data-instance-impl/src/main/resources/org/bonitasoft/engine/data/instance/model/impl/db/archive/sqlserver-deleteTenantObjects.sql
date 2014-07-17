DELETE FROM arch_data_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_data_instance WHERE tenantid = ${tenantid}
GO