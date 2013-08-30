ALTER TABLE breakpoint ADD CONSTRAINT fk_breakpoint_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE datasourceparameter ADD CONSTRAINT fk_DSParam_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE event_trigger_instance ADD CONSTRAINT fk_EvtTrig_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE external_identity_mapping ADD CONSTRAINT fk_extIdMap_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE processcategorymapping ADD CONSTRAINT fk_procCatMap_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE transition_instance ADD CONSTRAINT fk_transInst_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);

ALTER TABLE arch_document_mapping ADD CONSTRAINT fk_ADocMap_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_flownode_instance ADD CONSTRAINT fk_AFln_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_process_comment ADD CONSTRAINT fk_AProcCom_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_process_instance ADD CONSTRAINT fk_AProc_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE arch_transition_instance ADD CONSTRAINT fk_ATrans_tenId FOREIGN KEY (tenantid) REFERENCES tenant(id);
