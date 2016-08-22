-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE actor ADD CONSTRAINT fk_actor_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE actormember ADD CONSTRAINT fk_actormember_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE actormember ADD CONSTRAINT fk_actormember_actorId FOREIGN KEY (tenantid, actorId) REFERENCES actor(tenantid, id);
-- ALTER TABLE queriable_log ADD CONSTRAINT fk_queriable_log_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriablelog_p_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE category ADD CONSTRAINT fk_category_tenantId FOREIGN KEY (tenantid) REFERENCES tenant (id);
ALTER TABLE command ADD CONSTRAINT fk_command_tenantId FOREIGN KEY (tenantid) REFERENCES tenant (id);
ALTER TABLE connector_instance ADD CONSTRAINT fk_connector_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE data_instance ADD CONSTRAINT fk_data_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE dependency ADD CONSTRAINT fk_dependency_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_dependencymapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE document ADD CONSTRAINT fk_document_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE document_mapping ADD CONSTRAINT fk_document_mapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE document_mapping ADD CONSTRAINT fk_docmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;
ALTER TABLE event_trigger_instance ADD CONSTRAINT fk_event_trigger_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE external_identity_mapping ADD CONSTRAINT fk_external_identity_mapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE flownode_instance ADD CONSTRAINT fk_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE group_ ADD CONSTRAINT fk_group__tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE job_desc ADD CONSTRAINT fk_job_desc_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE message_instance ADD CONSTRAINT fk_message_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE custom_usr_inf_def ADD CONSTRAINT fk_custom_usr_inf_def_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_custom_usr_inf_val_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_flownode_instanceId FOREIGN KEY (tenantid, activityId) REFERENCES flownode_instance(tenantid, id);
ALTER TABLE processcategorymapping ADD CONSTRAINT fk_processcategorymapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE process_comment ADD CONSTRAINT fk_process_comment_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_content FOREIGN KEY (content_tenantid, content_id) REFERENCES process_content(tenantid, id);
ALTER TABLE process_instance ADD CONSTRAINT fk_process_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE processsupervisor ADD CONSTRAINT fk_processsupervisor_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE profile ADD CONSTRAINT fk_profile_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE profileentry ADD CONSTRAINT fk_profileentry_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE multi_biz_data ADD CONSTRAINT fk_multi_biz_data_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_inst_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE role ADD CONSTRAINT fk_role_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE theme ADD CONSTRAINT fk_theme_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE user_ ADD CONSTRAINT fk_user__tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE user_membership ADD CONSTRAINT fk_user_membership_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE waiting_event ADD CONSTRAINT fk_waiting_event_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

ALTER TABLE profileentry ADD CONSTRAINT fk_profileentry_profileId FOREIGN KEY (tenantId, profileId) REFERENCES profile(tenantId, id);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_profileId FOREIGN KEY (tenantId, profileId) REFERENCES profile(tenantId, id);
-- ALTER TABLE process_comment	ADD	CONSTRAINT fk_process_comment_process_instanceId FOREIGN KEY (processInstanceId, tenantid) REFERENCES process_instance(id, tenantid);

-- business application
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_profileId FOREIGN KEY (tenantid, profileId) REFERENCES profile (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_layoutId FOREIGN KEY (tenantid, layoutId) REFERENCES page (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_themeId FOREIGN KEY (tenantid, themeId) REFERENCES page (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);

ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

-- cannot have both fk_app_menu_appId and fk_app_menu_pageId because this create to path for deletion of business_app_menu elements:
-- business_app -> business_app_menu
-- business_app -> business_app_page -> business_app_menu
-- this is not allowed in SQL Server
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_appId FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_pageId FOREIGN KEY (tenantid, applicationPageId) REFERENCES business_app_page (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_parentId FOREIGN KEY (tenantid, parentId) REFERENCES business_app_menu (tenantid, id);

--  ------------------------ Foreign Keys to disable if archiving is on another BD ------------------
ALTER TABLE arch_document_mapping ADD CONSTRAINT fk_arch_document_mapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_document_mapping ADD CONSTRAINT fk_archdocmap_docid FOREIGN KEY (tenantid, documentid) REFERENCES document(tenantid, id) ON DELETE CASCADE;
ALTER TABLE arch_flownode_instance ADD CONSTRAINT fk_arch_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_process_comment ADD CONSTRAINT fk_arch_process_comment_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_process_instance ADD CONSTRAINT fk_arch_process_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_data_instance ADD CONSTRAINT fk_arch_data_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
