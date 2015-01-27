CREATE TABLE contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  value LONGBLOB
);

ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (scopeId, name, tenantid);

CREATE INDEX idx_cd_scope_name ON contract_data (scopeId, name, tenantid);
