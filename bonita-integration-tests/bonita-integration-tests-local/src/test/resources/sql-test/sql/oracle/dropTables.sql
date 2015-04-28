DROP TABLE p_employee cascade constraints purge;

DROP TABLE laptop cascade constraints purge;

DROP TABLE p_address cascade constraints purge;

DROP TABLE project cascade constraints purge;

DROP TABLE employeeprojectmapping cascade constraints purge;

DROP TABLE saemployee cascade constraints purge;


ALTER TABLE human DROP CONSTRAINT fk_car;
DROP TABLE car cascade constraints purge;
DROP TABLE human cascade constraints purge;
