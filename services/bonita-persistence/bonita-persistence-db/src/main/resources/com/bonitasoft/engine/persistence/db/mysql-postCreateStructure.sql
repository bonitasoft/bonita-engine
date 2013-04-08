-- ------------------------------------------------ Index -----------------------------------------------
CREATE INDEX fk_breakpoint_tenantId_idx ON breakpoint(tenantid ASC);
CREATE INDEX fk_breakpoint_process_definitionId_idx ON breakpoint(def_id ASC, tenantid ASC);
CREATE INDEX fk_breakpoint_process_instanceId_idx ON breakpoint(inst_id ASC, tenantid ASC);

-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE breakpoint ADD CONSTRAINT fk_breakpoint_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);