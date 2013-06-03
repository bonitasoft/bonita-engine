DELETE FROM user_membership WHERE tenantid = ${tenantid};
DELETE FROM p_metadata_val WHERE tenantid = ${tenantid};
DELETE FROM p_metadata_def WHERE tenantid = ${tenantid};
DELETE FROM user_ WHERE tenantid = ${tenantid};
DELETE FROM role WHERE tenantid = ${tenantid};
DELETE FROM group_ WHERE tenantid = ${tenantid};
