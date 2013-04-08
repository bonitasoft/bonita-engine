CREATE TABLE breakpoint (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR(255) NOT NULL,
  	inst_scope BOOLEAN NOT NULL,
  	inst_id INT8 NOT NULL,
  	def_id INT8 NOT NULL,
  	PRIMARY KEY (tenantid, id)
);
