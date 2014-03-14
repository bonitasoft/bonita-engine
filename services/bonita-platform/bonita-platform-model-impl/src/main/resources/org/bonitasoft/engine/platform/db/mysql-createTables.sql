CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  previousVersion VARCHAR(50) NOT NULL,
  initialVersion VARCHAR(50) NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE tenant (
  id BIGINT NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  defaultTenant BOOLEAN NOT NULL,
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;
