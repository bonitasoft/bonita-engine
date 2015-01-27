CREATE TABLE contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  value BLOB
);

ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (scopeId, name, tenantid);

CREATE INDEX idx_cd_scope_name ON contract_data (scopeId, name, tenantid);
