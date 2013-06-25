DELETE FROM datasourceparameter WHERE tenantid = ${tenantid}
GO
DELETE FROM datasource WHERE tenantid = ${tenantid}
GO
