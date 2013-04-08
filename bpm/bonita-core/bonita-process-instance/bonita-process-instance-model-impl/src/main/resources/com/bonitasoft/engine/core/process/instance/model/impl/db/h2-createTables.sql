CREATE TABLE breakpoint (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR(255) NOT NULL,
  	inst_scope BOOLEAN NOT NULL,
  	inst_id BIGINT NOT NULL,
  	def_id BIGINT NOT NULL,
  	PRIMARY KEY (tenantid, id)
);
