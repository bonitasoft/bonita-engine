DELETE FROM processcategorymapping WHERE tenantid = ${tenantid}
GO
DELETE FROM category WHERE tenantid = ${tenantid}
GO
