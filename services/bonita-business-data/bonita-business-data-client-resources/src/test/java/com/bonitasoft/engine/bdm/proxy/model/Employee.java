package com.bonitasoft.engine.bdm.proxy.model;

import java.util.ArrayList;
import java.util.List;


public class Employee
{


    private List<Employee> employees = new ArrayList<Employee>(10);

    private Employee manager;

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



}