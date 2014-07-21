-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE actor DROP CONSTRAINT fk_actor_tenantId
GO
ALTER TABLE actormember DROP CONSTRAINT fk_actormember_tenantId
GO
ALTER TABLE breakpoint DROP CONSTRAINT fk_breakpoint_tenantId
GO
-- ALTER TABLE queriable_log DROP CONSTRAINT fk_queriable_log_tenantId
-- GO
ALTER TABLE queriablelog_p DROP CONSTRAINT fk_queriablelog_p_tenantId
GO
ALTER TABLE category DROP CONSTRAINT fk_category_tenantId
GO
ALTER TABLE command DROP CONSTRAINT fk_command_tenantId
GO
ALTER TABLE connector_instance DROP CONSTRAINT fk_connector_instance_tenantId
GO
ALTER TABLE data_instance DROP CONSTRAINT fk_data_instance_tenantId
GO
ALTER TABLE data_mapping DROP CONSTRAINT fk_data_mapping_tenantId
GO
ALTER TABLE dependency DROP CONSTRAINT fk_dependency_tenantId
GO
ALTER TABLE dependencymapping DROP CONSTRAINT fk_dependencymapping_tenantId
GO
ALTER TABLE document_content DROP CONSTRAINT fk_document_content_tenantId
GO
ALTER TABLE document_mapping DROP CONSTRAINT fk_document_mapping_tenantId
GO
ALTER TABLE event_trigger_instance DROP CONSTRAINT fk_event_trigger_instance_tenantId
GO
ALTER TABLE external_identity_mapping DROP CONSTRAINT fk_external_identity_mapping_tenantId
GO
ALTER TABLE flownode_instance DROP CONSTRAINT fk_flownode_instance_tenantId
GO
ALTER TABLE group_ DROP CONSTRAINT fk_group__tenantId
GO
ALTER TABLE hidden_activity DROP CONSTRAINT fk_hidden_activity_tenantId
GO
ALTER TABLE job_desc DROP CONSTRAINT fk_job_desc_tenantId
GO
ALTER TABLE job_param DROP CONSTRAINT fk_job_param_tenantId
GO
ALTER TABLE message_instance DROP CONSTRAINT fk_message_instance_tenantId
GO
-- ALTER TABLE migration_plan DROP CONSTRAINT fk_migration_plan_tenantId
GO
ALTER TABLE custom_usr_inf_def DROP CONSTRAINT fk_custom_usr_inf_def_tenantId
GO
ALTER TABLE custom_usr_inf_val DROP CONSTRAINT fk_custom_usr_inf_val_tenantId
GO
ALTER TABLE pending_mapping DROP CONSTRAINT fk_pending_mapping_tenantId
GO
ALTER TABLE processcategorymapping DROP CONSTRAINT fk_processcategorymapping_tenantId
GO
ALTER TABLE process_comment DROP CONSTRAINT fk_process_comment_tenantId
GO
ALTER TABLE process_definition DROP CONSTRAINT fk_process_definition_tenantId
GO
ALTER TABLE process_instance DROP CONSTRAINT fk_process_instance_tenantId
GO
ALTER TABLE processsupervisor DROP CONSTRAINT fk_processsupervisor_tenantId
GO
ALTER TABLE profile DROP CONSTRAINT fk_profile_tenantId
GO
ALTER TABLE profileentry DROP CONSTRAINT fk_profileentry_tenantId
GO
ALTER TABLE profilemember DROP CONSTRAINT fk_profilemember_tenantId
GO
ALTER TABLE ref_biz_data_inst DROP CONSTRAINT fk_ref_biz_data_inst_tenantId
GO
ALTER TABLE role DROP CONSTRAINT fk_role_tenantId
GO
ALTER TABLE theme DROP CONSTRAINT fk_theme_tenantId
GO
ALTER TABLE user_ DROP CONSTRAINT fk_user__tenantId
GO
ALTER TABLE user_membership DROP CONSTRAINT fk_user_membership_tenantId
GO
ALTER TABLE waiting_event DROP CONSTRAINT fk_waiting_event_tenantId
GO

ALTER TABLE profilemember DROP CONSTRAINT fk_profilemember_profileId
GO
ALTER TABLE profileentry DROP CONSTRAINT fk_profileentry_profileId
GO

-- business application
ALTER TABLE business_app DROP CONSTRAINT fk_app_tenantId
GO
ALTER TABLE business_app_page DROP CONSTRAINT fk_app_page_tenantId
GO
ALTER TABLE business_app_page DROP CONSTRAINT fk_bus_app_id
GO
ALTER TABLE business_app_page DROP CONSTRAINT fk_page_id
GO


--  ------------------------ Foreign Keys to disable if archiving is on another BD ------------------
ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_arch_document_mapping_tenantId
GO
ALTER TABLE arch_flownode_instance DROP CONSTRAINT fk_arch_flownode_instance_tenantId
GO
ALTER TABLE arch_process_comment DROP CONSTRAINT fk_arch_process_comment_tenantId
GO
ALTER TABLE arch_process_instance DROP CONSTRAINT fk_arch_process_instance_tenantId
GO
ALTER TABLE arch_transition_instance DROP CONSTRAINT fk_arch_transition_instance_tenantId
GO
ALTER TABLE arch_data_instance DROP CONSTRAINT fk_arch_data_instance_tenantId
GO
ALTER TABLE arch_data_mapping DROP CONSTRAINT fk_arch_data_mapping_tenantId
GO
