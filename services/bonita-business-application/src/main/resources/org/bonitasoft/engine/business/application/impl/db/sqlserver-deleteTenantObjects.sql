DELETE FROM business_app WHERE tenantid = ${tenantid}
GO
DELETE FROM business_app_page WHERE tenantid = ${tenantid}
GO
DELETE FROM business_app_menu WHERE tenantid = ${tenantid}
GO
