DELETE FROM dependencymapping WHERE tenantid = ${tenantid}
GO
DELETE FROM dependency WHERE tenantid = ${tenantid}
GO
