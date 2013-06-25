DELETE FROM profilemember WHERE tenantid = ${tenantid}
GO
DELETE FROM profileentry WHERE tenantid = ${tenantid}
GO
DELETE FROM profile WHERE tenantid = ${tenantid}
GO
