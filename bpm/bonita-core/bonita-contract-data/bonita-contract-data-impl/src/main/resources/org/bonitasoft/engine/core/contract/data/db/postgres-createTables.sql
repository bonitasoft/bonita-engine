CREATE TABLE contract_data (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  value BYTEA
);

ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (scopeId, name, tenantid);

CREATE INDEX idx_cd_scope_name ON contract_data (scopeId, name, tenantid);
