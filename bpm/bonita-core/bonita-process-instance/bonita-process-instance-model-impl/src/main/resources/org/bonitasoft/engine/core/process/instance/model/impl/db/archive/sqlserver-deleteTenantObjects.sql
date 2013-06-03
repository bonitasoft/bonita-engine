DELETE FROM arch_activity_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_process_instance WHERE tenantid = ${tenantid}
GO
DELETE FROM arch_event_trigger_instance  WHERE tenantid = ${tenantid}
GO