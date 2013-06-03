ALTER TABLE transition_instance DROP CONSTRAINT fk_transInst_tenId;
ALTER TABLE processcategorymapping DROP CONSTRAINT fk_procCatMap_tenId;
ALTER TABLE external_identity_mapping DROP CONSTRAINT fk_extIdMap_tenId;
ALTER TABLE event_trigger_instance DROP CONSTRAINT fk_EvtTrig_tenId;
ALTER TABLE datasourceparameter DROP CONSTRAINT fk_DSParam_tenId;

ALTER TABLE arch_document_mapping DROP CONSTRAINT fk_ADocMap_tenId;
ALTER TABLE arch_flownode_instance DROP CONSTRAINT fk_AFln_tenId;
ALTER TABLE arch_process_comment DROP CONSTRAINT fk_AProcCom_tenId;
ALTER TABLE arch_process_instance DROP CONSTRAINT fk_AProc_tenId;
ALTER TABLE arch_transition_instance DROP CONSTRAINT fk_ATrans_tenId;