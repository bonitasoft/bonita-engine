DELETE FROM employee WHERE tenantid = ${tenantid}
GO

DELETE FROM laptop WHERE tenantid = ${tenantid}
GO

DELETE FROM address WHERE tenantid = ${tenantid}
GO

DELETE FROM project WHERE tenantid = ${tenantid}
GO

DELETE FROM employeeprojectmapping WHERE tenantid = ${tenantid}
GO

DELETE FROM saemployee WHERE tenantid = ${tenantid}
GO
DELETE FROM car WHERE tenantid = ${tenantid}
GO
DELETE FROM human WHERE tenantid = ${tenantid}
GO
