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

    @SuppressWarnings("unused")
    @Override
    public void setParameters(final Map<String, String> dataSourceParameters) {
    }

    @Override
    public void deleteEmployee(final long employeeId) {
        this.employees.remove(employeeId);
        if (this.employeeAddressJoinMap.containsKey(employeeId)) {
            final List<Long> employeeAddressIds = this.employeeAddressJoinMap.get(employeeId);
            for (final Long employeeAddressId : employeeAddressIds) {
                this.addresses.remove(employeeAddressId);
            }
            this.employeeAddressJoinMap.remove(employeeId);
        }
    }

    @Override
    public Employee getEmployee(final long employeeId) {
        final LightEmployee lightEmployee = this.employees.get(employeeId);
        if (lightEmployee == null) {
            return null;
        }
        final Employee employee = new Employee(lightEmployee.getId(), lightEmployee.getFirstName(), lightEmployee.getLastName(), lightEmployee.getAge());
        if (this.employeeAddressJoinMap.containsKey(employeeId)) {
            final List<Long> employeeAddressIds = this.employeeAddressJoinMap.get(employeeId);
            for (final Long employeeAddressId : employeeAddressIds) {
                employee.addAddress(this.addresses.get(employeeAddressId));
            }
        }
        return employee;
    }

    @Override
    public LightEmployee getLightEmployee(final long employeeId) {
        return this.employees.get(employeeId);
    }

    @Override
    public List<Employee> getEmployees() {
        final List<Employee> employees = new ArrayList<Employee>();
        for (final Long employeeId : this.employees.keySet()) {
            employees.add(this.getEmployee(employeeId));
        }
        return employees;
    }

    @Override
    public List<Address> getEmployeeAdresses(final long employeeId) {
        final List<Address> addresses = new ArrayList<Address>();
        if (this.employeeAddressJoinMap.containsKey(employeeId)) {
            final List<Long> employeeAddressIds = this.employeeAddressJoinMap.get(employeeId);
            for (final Long employeeAddressId : employeeAddressIds) {
                addresses.add(this.addresses.get(employeeAddressId));
            }
        }
        return addresses;
    }

    @Override
    public void insertEmployee(final Employee employee) {
        final long id = employee.getId();
        this.employees.put(id, new LightEmployee(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getAge()));
        final List<Long> addressIds = new ArrayList<Long>();
        for (final Address address : employee.getAddresses()) {
            this.addresses.put(address.getId(), address);
            addressIds.add(address.getId());
        }
        this.employeeAddressJoinMap.put(id, addressIds);
    }

    @SuppressWarnings("unused")
    @Override
    public boolean configurationMatches(final DataSourceConfiguration datasourceConfiguration) {
        // TODO Auto-generated method stub
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public void configure(final DataSourceConfiguration dataSourceConfiguration) {
        // TODO Auto-generated method stub
    }

}
