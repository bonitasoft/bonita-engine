CREATE TABLE contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  value VARBINARY(MAX)
)
GO

ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id)
GO
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (scopeId, name, tenantid)
GO

CREATE INDEX idx_cd_scope_name ON contract_data (scopeId, name, tenantid)
GO
