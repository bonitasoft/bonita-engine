DELETE FROM queriablelog_p WHERE tenantid = ${tenantid}
GO
DELETE FROM queriable_log WHERE tenantid = ${tenantid}
GO
