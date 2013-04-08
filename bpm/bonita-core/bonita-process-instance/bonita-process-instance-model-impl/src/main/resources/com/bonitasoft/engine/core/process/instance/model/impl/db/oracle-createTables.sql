CREATE TABLE breakpoint (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name VARCHAR2(255) NOT NULL,
  	inst_scope NUMBER(1)  NOT NULL,
  	inst_id NUMBER(19, 0) NOT NULL,
  	def_id NUMBER(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
);
