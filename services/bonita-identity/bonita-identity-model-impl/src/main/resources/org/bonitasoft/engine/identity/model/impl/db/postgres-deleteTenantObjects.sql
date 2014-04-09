DELETE FROM user_membership WHERE tenantid = ${tenantid};
DELETE FROM custom_usr_inf_val WHERE tenantid = ${tenantid};
DELETE FROM custom_usr_inf_def WHERE tenantid = ${tenantid};
DELETE FROM user_ WHERE tenantid = ${tenantid};
DELETE FROM user_contactinfo WHERE tenantid = ${tenantid};
DELETE FROM role WHERE tenantid = ${tenantid};
DELETE FROM group_ WHERE tenantid = ${tenantid};
