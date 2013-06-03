DELETE FROM transition_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM connector_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM flownode_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM process_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM event_trigger_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM hidden_activity WHERE tenantid = ${tenantid};
GO
