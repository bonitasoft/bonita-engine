-- ------------------------------------------------ Foreign Keys -----------------------------------------------
ALTER TABLE breakpoint DROP FOREIGN KEY fk_breakpoint_tenantId;

-- ------------------------------------------------ Index -----------------------------------------------
DROP INDEX fk_breakpoint_tenantId_idx on breakpoint;
DROP INDEX fk_breakpoint_process_definitionId_idx on breakpoint;
DROP INDEX fk_breakpoint_process_instanceId_idx on breakpoint;