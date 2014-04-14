package org.bonitasoft.engine.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.data.model.Address;
import org.bonitasoft.engine.data.model.Employee;
import org.bonitasoft.engine.data.model.LightEmployee;

public class EmployeeDataSourceImpl implements EmployeeDataSource {

    // key = employeeId
    private final Map<Long, LightEmployee> employees = new HashMap<Long, LightEmployee>();

    // key = addressId
    private final Map<Long, Address> addresses = new HashMap<Long, Address>();

    // key = employeeId, value = list of AddressId
    private final Map<Long, List<Long>> employeeAddressJoinMap = new HashMap<Long, List<Long>>();

    @Override
    public void setParameters(final Map<String, String> dataSourceParameters) {
    }

    @Override
    public void deleteEmployee(final long employeeId) {
        employees.remove(employeeId);
        if (employeeAddressJoinMap.containsKey(employeeId)) {
            final List<Long> employeeAddressIds = employeeAddressJoinMap.get(employeeId);
            for (final Long employeeAddressId : employeeAddressIds) {
                addresses.remove(employeeAddressId);
            }
            employeeAddressJoinMap.remove(employeeId);
        }
    }

    @Override
    public Employee getEmployee(final long employeeId) {
        final LightEmployee lightEmployee = employees.get(employeeId);
        if (lightEmployee == null) {
            return null;
        }
        final Employee employee = new Employee(lightEmployee.getId(), lightEmployee.getFirstName(), lightEmployee.getLastName(), lightEmployee.getAge());
        if (employeeAddressJoinMap.containsKey(employeeId)) {
            final List<Long> employeeAddressIds = employeeAddressJoinMap.get(employeeId);
            for (final Long employeeAddressId : employeeAddressIds) {
                employee.addAddress(addresses.get(employeeAddressId));
            }
        }
        return employee;
    }

    @Override
    public LightEmployee getLightEmployee(final long employeeId) {
        return employees.get(employeeId);
    }

    @Override
    public List<Employee> getEmployees() {
        final List<Employee> employees = new ArrayList<Employee>();
        for (final Long employeeId : this.employees.keySet()) {
            employees.add(getEmployee(employeeId));
        }
        return employees;
    }

    @Override
    public List<Address> getEmployeeAdresses(final long employeeId) {
        final List<Address> addresses = new ArrayList<Address>();
        if (employeeAddressJoinMap.containsKey(employeeId)) {
            final List<Long> employeeAddressIds = employeeAddressJoinMap.get(employeeId);
            for (final Long employeeAddressId : employeeAddressIds) {
                addresses.add(this.addresses.get(employeeAddressId));
            }
        }
        return addresses;
    }

    @Override
    public void insertEmployee(final Employee employee) {
        final long id = employee.getId();
        employees.put(id, new LightEmployee(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getAge()));
        final List<Long> addressIds = new ArrayList<Long>();
        for (final Address address : employee.getAddresses()) {
            addresses.put(address.getId(), address);
            addressIds.add(address.getId());
        }
        employeeAddressJoinMap.put(id, addressIds);
    }

    @Override
    public boolean configurationMatches(final DataSourceConfiguration datasourceConfiguration) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void configure(final DataSourceConfiguration dataSourceConfiguration) {
        // TODO Auto-generated method stub
    }

}
