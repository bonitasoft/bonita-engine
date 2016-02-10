ALTER TABLE human ADD CONSTRAINT fk_car FOREIGN KEY (tenantid, car_id) REFERENCES car(tenantid, id) ON DELETE CASCADE
GO