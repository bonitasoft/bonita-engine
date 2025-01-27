-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE actormember DROP CONSTRAINT fk_actormember_actorid;
ALTER TABLE profilemember DROP CONSTRAINT fk_profilemember_profileid;

ALTER TABLE connector_instance DROP CONSTRAINT fk_connector_instance_tenantId;
ALTER TABLE document_mapping DROP CONSTRAINT fk_document_mapping_documentid;
ALTER TABLE pending_mapping DROP CONSTRAINT fk_pending_mapping_tenantId;
ALTER TABLE pending_mapping DROP CONSTRAINT fk_pending_mapping_flownode_instanceId;
ALTER TABLE process_definition DROP CONSTRAINT fk_process_definition_content_id;

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
ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_arch_document_mapping_documentid;
ALTER TABLE arch_flownode_instance DROP CONSTRAINT fk_arch_flownode_instance_tenantId;
ALTER TABLE arch_process_instance DROP CONSTRAINT fk_arch_process_instance_tenantId;
