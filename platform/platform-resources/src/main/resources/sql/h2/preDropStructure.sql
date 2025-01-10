-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE actormember DROP CONSTRAINT fk_actormember_actorid;
ALTER TABLE category DROP CONSTRAINT fk_category_tenantId;
ALTER TABLE command DROP CONSTRAINT fk_command_tenantId;
ALTER TABLE connector_instance DROP CONSTRAINT fk_connector_instance_tenantId;
ALTER TABLE data_instance DROP CONSTRAINT fk_data_instance_tenantId;
ALTER TABLE dependency DROP CONSTRAINT fk_dependency_tenantId;
ALTER TABLE dependencymapping DROP CONSTRAINT fk_dependencymapping_tenantId;
ALTER TABLE document DROP CONSTRAINT fk_document_tenantId;
ALTER TABLE document_mapping DROP CONSTRAINT fk_document_mapping_tenantId;
ALTER TABLE document_mapping DROP CONSTRAINT fk_docmap_docid;
ALTER TABLE event_trigger_instance DROP CONSTRAINT fk_event_trigger_instance_tenantId;
ALTER TABLE message_instance DROP CONSTRAINT fk_message_instance_tenantId;
ALTER TABLE pending_mapping DROP CONSTRAINT fk_pending_mapping_tenantId;
ALTER TABLE pending_mapping DROP CONSTRAINT fk_pending_mapping_flownode_instanceId;
ALTER TABLE processcategorymapping DROP CONSTRAINT fk_processcategorymapping_tenantId;
ALTER TABLE process_comment DROP CONSTRAINT fk_process_comment_tenantId;
ALTER TABLE process_definition DROP CONSTRAINT fk_process_definition_tenantId;
ALTER TABLE process_definition DROP CONSTRAINT fk_process_definition_content;
ALTER TABLE processsupervisor DROP CONSTRAINT fk_processsupervisor_tenantId;
ALTER TABLE profile DROP CONSTRAINT fk_profile_tenantId;
ALTER TABLE profilemember DROP CONSTRAINT fk_profilemember_tenantId;
ALTER TABLE multi_biz_data DROP CONSTRAINT fk_multi_biz_data_tenantId;
ALTER TABLE ref_biz_data_inst DROP CONSTRAINT fk_ref_biz_data_inst_tenantId;
ALTER TABLE waiting_event DROP CONSTRAINT fk_waiting_event_tenantId;

ALTER TABLE profilemember DROP CONSTRAINT fk_profilemember_profileid;
-- ALTER TABLE process_comment DROP CONSTRAINT fk_process_comment_process_instanceId;

-- business application
ALTER TABLE business_app_menu DROP CONSTRAINT fk_app_menu_tenantId;
ALTER TABLE business_app_menu DROP CONSTRAINT fk_business_app_menu_applicationid;
ALTER TABLE business_app_menu DROP CONSTRAINT fk_business_app_menu_applicationpageid;
ALTER TABLE business_app_menu DROP CONSTRAINT fk_business_app_menu_parentid;
ALTER TABLE business_app_page DROP CONSTRAINT fk_app_page_tenantId;
ALTER TABLE business_app_page DROP CONSTRAINT fk_business_app_page_applicationid;
ALTER TABLE business_app_page DROP CONSTRAINT fk_business_app_page_pageid;
ALTER TABLE business_app DROP CONSTRAINT fk_business_app_profileid;
ALTER TABLE business_app DROP CONSTRAINT fk_app_tenantId;
ALTER TABLE business_app DROP CONSTRAINT fk_business_app_layoutid;
ALTER TABLE business_app DROP CONSTRAINT fk_business_app_themeid;



--  ------------------------ Foreign Keys to disable if archiving is on another BD ------------------
ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_arch_document_mapping_tenantId;
ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_archdocmap_docid;
ALTER TABLE arch_flownode_instance DROP CONSTRAINT fk_arch_flownode_instance_tenantId;
ALTER TABLE arch_process_comment DROP CONSTRAINT fk_arch_process_comment_tenantId;
ALTER TABLE arch_process_instance DROP CONSTRAINT fk_arch_process_instance_tenantId;
ALTER TABLE arch_data_instance DROP CONSTRAINT fk_arch_data_instance_tenantId;
