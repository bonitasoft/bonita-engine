DELETE FROM arch_contract_data WHERE tenantid = ${tenantid}
GO
DELETE FROM contract_data WHERE tenantid = ${tenantid}
GO
DELETE FROM actormember WHERE tenantid = ${tenantid}
GO
DELETE FROM actor WHERE tenantid = ${tenantid}
GO
DELETE FROM processcategorymapping WHERE tenantid = ${tenantid}
GO
DELETE FROM category WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_process_comment WHERE tenantid = ${tenantid}
GO
DELETE FROM process_comment WHERE tenantid = ${tenantid}
GO
DELETE FROM process_definition WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_document_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM document WHERE tenantid = ${tenantid}
GO
DELETE FROM document_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_flownode_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_process_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_connector_instance  WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_multi_biz_data WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_ref_biz_data_inst WHERE tenantid = ${tenantid}
GO
DELETE FROM multi_biz_data WHERE tenantid = ${tenantid}
GO
DELETE FROM ref_biz_data_inst WHERE tenantid = ${tenantid}
GO
DELETE FROM connector_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM message_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM pending_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM event_trigger_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM waiting_event WHERE tenantid = ${tenantid}
GO
DELETE FROM process_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM flownode_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM report WHERE tenantid = ${tenantid}
GO
DELETE FROM processsupervisor WHERE tenantid = ${tenantid}
GO
DELETE FROM business_app WHERE tenantid = ${tenantid}
GO
DELETE FROM business_app_page WHERE tenantid = ${tenantid}
GO
DELETE FROM business_app_menu WHERE tenantid = ${tenantid}
GO
DELETE FROM command WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_data_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM data_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM dependencymapping WHERE tenantid = ${tenantid}
GO
DELETE FROM dependency WHERE tenantid = ${tenantid}
GO
DELETE FROM external_identity_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM user_membership WHERE tenantid = ${tenantid}
GO
DELETE FROM custom_usr_inf_val WHERE tenantid = ${tenantid}
GO
DELETE FROM custom_usr_inf_def WHERE tenantid = ${tenantid}
GO
DELETE FROM user_login WHERE tenantid = ${tenantid}
GO
DELETE FROM user_ WHERE tenantid = ${tenantid}
GO
DELETE FROM user_contactinfo WHERE tenantid = ${tenantid}
GO
DELETE FROM role WHERE tenantid = ${tenantid}
GO
DELETE FROM group_ WHERE tenantid = ${tenantid}
GO
DELETE FROM queriablelog_p WHERE tenantid = ${tenantid}
GO
DELETE FROM queriable_log WHERE tenantid = ${tenantid}
GO
DELETE FROM page WHERE tenantid = ${tenantid}
GO
DELETE FROM sequence WHERE tenantid = ${tenantid}
GO
DELETE FROM profilemember WHERE tenantid = ${tenantid}
GO
DELETE FROM profileentry WHERE tenantid = ${tenantid}
GO
DELETE FROM profile WHERE tenantid = ${tenantid}
GO
DELETE FROM job_log WHERE tenantid = ${tenantid}
GO
DELETE FROM job_param WHERE tenantid = ${tenantid}
GO
DELETE FROM job_desc WHERE tenantid = ${tenantid}
GO
DELETE FROM theme WHERE tenantid = ${tenantid}
GO
DELETE FROM form_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM page_mapping WHERE tenantid = ${tenantid}
GO
DELETE FROM process_content WHERE tenantid = ${tenantid}
GO
DELETE FROM proc_parameter WHERE tenantid = ${tenantid}
GO
