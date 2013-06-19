DELETE FROM actormember WHERE tenantid = ${tenantid}
GO
DELETE FROM actor WHERE tenantid = ${tenantid}
GO
