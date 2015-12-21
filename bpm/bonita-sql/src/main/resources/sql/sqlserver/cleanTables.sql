DELETE FROM arch_contract_data
GO
DELETE FROM contract_data
GO
DELETE FROM actormember
GO
DELETE FROM actor
GO
DELETE FROM processcategorymapping
GO
DELETE FROM category
GO
DELETE FROM arch_process_comment
GO
DELETE FROM process_comment
GO
DELETE FROM process_definition
GO
DELETE FROM arch_document_mapping
GO
DELETE FROM document
GO
DELETE FROM document_mapping
GO
DELETE FROM arch_flownode_instance
GO
DELETE FROM arch_process_instance
GO
DELETE FROM arch_connector_instance
GO
DELETE FROM arch_multi_biz_data
GO
DELETE FROM arch_ref_biz_data_inst
GO
DELETE FROM multi_biz_data
GO
DELETE FROM ref_biz_data_inst
GO
DELETE FROM pending_mapping
GO
DELETE FROM message_instance
GO
DELETE FROM waiting_event
GO
DELETE FROM event_trigger_instance
GO
DELETE FROM connector_instance
GO
DELETE FROM flownode_instance
GO
DELETE FROM process_instance
GO
DELETE FROM report
GO
DELETE FROM processsupervisor
GO
DELETE FROM business_app_menu
GO
DELETE FROM business_app_page
GO
DELETE FROM business_app
GO
DELETE FROM command
GO
DELETE FROM arch_data_instance
GO
DELETE FROM data_instance
GO
DELETE FROM dependencymapping
GO
DELETE FROM dependency
GO
DELETE FROM external_identity_mapping
GO
DELETE FROM user_membership
GO
DELETE FROM custom_usr_inf_val
GO
DELETE FROM custom_usr_inf_def
GO
DELETE FROM user_contactinfo
GO
DELETE FROM user_login
GO
DELETE FROM user_
GO
DELETE FROM role
GO
DELETE FROM group_
GO
DELETE FROM queriablelog_p
GO
DELETE FROM queriable_log
GO
DELETE FROM page
GO
DELETE FROM sequence WHERE tenantId <> -1
GO
DELETE FROM profilemember
GO
DELETE FROM profileentry
GO
DELETE FROM profile
GO
DELETE FROM job_log
GO
DELETE FROM job_param
GO
DELETE FROM job_desc
GO
DELETE FROM theme
GO
DELETE FROM tenant
GO
DELETE FROM platformCommand
GO
DELETE FROM form_mapping
GO
DELETE FROM page_mapping
GO
DELETE FROM proc_parameter
GO

-- do NOT clear directly PLATFORM table, Hibernate needs to update its cache to know the platform has been deleted
 