DELETE FROM p_employee WHERE tenantid = ${tenantid};

DELETE FROM laptop WHERE tenantid = ${tenantid};

DELETE FROM p_address WHERE tenantid = ${tenantid};

DELETE FROM project WHERE tenantid = ${tenantid};

DELETE FROM employeeprojectmapping WHERE tenantid = ${tenantid};

DELETE FROM saemployee WHERE tenantid = ${tenantid};
DELETE FROM car WHERE tenantid = ${tenantid};
DELETE FROM human WHERE tenantid = ${tenantid};
