package org.bonitasoft.engine.data;

import java.util.List;

import org.bonitasoft.engine.data.model.Address;
import org.bonitasoft.engine.data.model.Employee;
import org.bonitasoft.engine.data.model.LightEmployee;

public interface EmployeeDataSource extends DataSourceImplementation {

    void insertEmployee(Employee employee);

    void deleteEmployee(long employeeId);

    Employee getEmployee(long employeeId);

    LightEmployee getLightEmployee(long employeeId);

    List<Address> getEmployeeAdresses(long employeeId);

    List<Employee> getEmployees();

}
