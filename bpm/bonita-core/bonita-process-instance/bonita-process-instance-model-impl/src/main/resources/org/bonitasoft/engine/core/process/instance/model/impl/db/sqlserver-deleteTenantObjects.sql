DELETE FROM multi_biz_data WHERE tenantid = ${tenantid}
GO
DELETE FROM ref_biz_data_inst WHERE tenantid = ${tenantid}
GO
DELETE FROM connector_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM hidden_activity WHERE tenantid = ${tenantid}
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
DELETE FROM token WHERE tenantid = ${tenantid}
GO
DELETE FROM breakpoint WHERE tenantid = ${tenantid}
GO
