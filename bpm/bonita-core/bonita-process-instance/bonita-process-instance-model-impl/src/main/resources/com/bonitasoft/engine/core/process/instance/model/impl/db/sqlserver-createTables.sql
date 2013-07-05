CREATE TABLE breakpoint (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	state_id INT NOT NULL,
  	int_state_id INT NOT NULL,
  	elem_name NVARCHAR(255) NOT NULL,
  	inst_scope BIT NOT NULL,
  	inst_id NUMERIC(19, 0) NOT NULL,
  	def_id NUMERIC(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
)
GO
