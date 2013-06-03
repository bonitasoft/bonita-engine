DELETE FROM arch_flownode_instance WHERE tenantid = ${tenantid};
DELETE FROM arch_process_instance WHERE tenantid = ${tenantid};
DELETE FROM arch_transition_instance  WHERE tenantid = ${tenantid};
DELETE FROM arch_connector_instance  WHERE tenantid = ${tenantid};
