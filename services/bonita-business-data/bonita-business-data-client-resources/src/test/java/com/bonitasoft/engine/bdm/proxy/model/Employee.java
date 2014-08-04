package com.bonitasoft.engine.bdm.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {

    private List<Employee> employees = new ArrayList<Employee>(10);
    private List<Address> addresses = new ArrayList<Address>(10);

    private Employee manager;
    private Address address;

    public Employee() {
    }

    public void setEmployees(final List<Employee> employees) {
        this.employees = employees;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addToAddresses(final Employee addTo) {
        final List employees = getEmployees();
        employees.add(addTo);
    }

    @SuppressWarnings("rawtypes")
    public void removeFromAddresses(final Employee removeFrom) {
        final List employees = getEmployees();
        employees.remove(removeFrom);
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}