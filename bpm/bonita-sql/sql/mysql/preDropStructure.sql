-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE actor DROP FOREIGN KEY fkactor_tenantId;
ALTER TABLE actormember DROP FOREIGN KEY fkactormember_tenantId;
ALTER TABLE actormember DROP FOREIGN KEY fkactormember_actorId;
ALTER TABLE breakpoint DROP FOREIGN KEY fkbreakpoint_tenantId;
-- ALTER TABLE queriable_log DROP FOREIGN KEY fkqueriable_log_tenantId;
ALTER TABLE queriablelog_p DROP FOREIGN KEY fkqueriablelog_p_tenantId;
ALTER TABLE category DROP FOREIGN KEY fkcategory_tenantId;
ALTER TABLE command DROP FOREIGN KEY fkcommand_tenantId;
ALTER TABLE connector_instance DROP FOREIGN KEY fkconnector_instance_tenantId;
ALTER TABLE data_instance DROP FOREIGN KEY fkdata_instance_tenantId;
ALTER TABLE data_mapping DROP FOREIGN KEY fkdata_mapping_tenantId;
ALTER TABLE dependency DROP FOREIGN KEY fkdependency_tenantId;
ALTER TABLE dependencymapping DROP FOREIGN KEY fkdependencymapping_tenantId;
ALTER TABLE document DROP FOREIGN KEY fkdocument_tenantId;
ALTER TABLE document_mapping DROP FOREIGN KEY fkdocument_mapping_tenantId;
ALTER TABLE document_mapping DROP FOREIGN KEY fkdocmap_docid;
ALTER TABLE event_trigger_instance DROP FOREIGN KEY fkevent_trigger_instance_tenantId;
ALTER TABLE external_identity_mapping DROP FOREIGN KEY fkexternal_identity_mapping_tenantId;
ALTER TABLE flownode_instance DROP FOREIGN KEY fkflownode_instance_tenantId;
ALTER TABLE group_ DROP FOREIGN KEY fkgroup__tenantId;
ALTER TABLE hidden_activity DROP FOREIGN KEY fkhidden_activity_tenantId;
ALTER TABLE job_desc DROP FOREIGN KEY fkjob_desc_tenantId;
ALTER TABLE job_param DROP FOREIGN KEY fkjob_param_tenantId;
ALTER TABLE message_instance DROP FOREIGN KEY fkmessage_instance_tenantId;
-- ALTER TABLE migration_plan DROP FOREIGN KEY fkmigration_plan_tenantId;
ALTER TABLE custom_usr_inf_def DROP FOREIGN KEY fkcustom_usr_inf_def_tenantId;
ALTER TABLE custom_usr_inf_val DROP FOREIGN KEY fkcustom_usr_inf_val_tenantId;
ALTER TABLE pending_mapping DROP FOREIGN KEY fkpending_mapping_tenantId;
ALTER TABLE pending_mapping DROP FOREIGN KEY fkpending_mapping_flownode_instanceId;
ALTER TABLE processcategorymapping DROP FOREIGN KEY fkprocesscategorymapping_tenantId;
ALTER TABLE process_comment DROP FOREIGN KEY fkprocess_comment_tenantId;
ALTER TABLE process_definition DROP FOREIGN KEY fkprocess_definition_tenantId;
ALTER TABLE process_instance DROP FOREIGN KEY fkprocess_instance_tenantId;
ALTER TABLE processsupervisor DROP FOREIGN KEY fkprocesssupervisor_tenantId;
ALTER TABLE profile DROP FOREIGN KEY fkprofile_tenantId;
ALTER TABLE profileentry DROP FOREIGN KEY fkprofileentry_tenantId;
ALTER TABLE profilemember DROP FOREIGN KEY fkprofilemember_tenantId;
ALTER TABLE multi_biz_data DROP FOREIGN KEY fkmulti_biz_data_tenantId;
ALTER TABLE ref_biz_data_inst DROP FOREIGN KEY fkref_biz_data_inst_tenantId;
ALTER TABLE role DROP FOREIGN KEY fkrole_tenantId;
ALTER TABLE theme DROP FOREIGN KEY fktheme_tenantId;
ALTER TABLE user_ DROP FOREIGN KEY fkuser__tenantId;
ALTER TABLE user_membership DROP FOREIGN KEY fkuser_membership_tenantId;
ALTER TABLE waiting_event DROP FOREIGN KEY fkwaiting_event_tenantId;

ALTER TABLE profilemember DROP FOREIGN KEY fkprofilemember_profileId;
ALTER TABLE profileentry DROP FOREIGN KEY fkprofileentry_profileId;
-- ALTER TABLE process_comment DROP FOREIGN KEY fkprocess_comment_process_instanceId;

-- business application
ALTER TABLE business_app_menu DROP FOREIGN KEY fkapp_menu_tenantId;
ALTER TABLE business_app_menu DROP FOREIGN KEY fkapp_menu_appId;
ALTER TABLE business_app_menu DROP FOREIGN KEY fkapp_menu_pageId;
ALTER TABLE business_app_menu DROP FOREIGN KEY fkapp_menu_parentId;
ALTER TABLE business_app_page DROP FOREIGN KEY fkapp_page_tenantId;
ALTER TABLE business_app_page DROP FOREIGN KEY fkbus_app_id;
ALTER TABLE business_app_page DROP FOREIGN KEY fkpage_id;
ALTER TABLE business_app DROP FOREIGN KEY fkapp_profileId;
ALTER TABLE business_app DROP FOREIGN KEY fkapp_tenantId;



--  ------------------------ Foreign Keys to disable if archiving is on another BD ------------------
ALTER TABLE arch_document_mapping DROP FOREIGN KEY fkarch_document_mapping_tenantId;
ALTER TABLE arch_document_mapping DROP FOREIGN KEY fkarchdocmap_docid;
ALTER TABLE arch_flownode_instance DROP FOREIGN KEY fkarch_flownode_instance_tenantId;
ALTER TABLE arch_process_comment DROP FOREIGN KEY fkarch_process_comment_tenantId;
ALTER TABLE arch_process_instance DROP FOREIGN KEY fkarch_process_instance_tenantId;
ALTER TABLE arch_transition_instance DROP FOREIGN KEY fkarch_transition_instance_tenantId;
ALTER TABLE arch_data_instance DROP FOREIGN KEY fkarch_data_instance_tenantId;
ALTER TABLE arch_data_mapping DROP FOREIGN KEY fkarch_data_mapping_tenantId;
