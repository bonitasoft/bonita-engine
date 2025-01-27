-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE connector_instance ADD CONSTRAINT fk_connector_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_flownode_instanceId FOREIGN KEY (activityId) REFERENCES flownode_instance(id);

--  ------------------------ Foreign Keys to disable if archiving is on another BD ------------------
ALTER TABLE arch_flownode_instance ADD CONSTRAINT fk_arch_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_process_instance ADD CONSTRAINT fk_arch_process_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
