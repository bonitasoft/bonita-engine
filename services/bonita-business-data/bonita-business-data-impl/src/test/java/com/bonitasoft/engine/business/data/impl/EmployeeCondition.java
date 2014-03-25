package com.bonitasoft.engine.business.data.impl;

import org.assertj.core.api.Condition;

import com.bonitasoft.pojo.Employee;

public class EmployeeCondition extends Condition<Employee> {

    private final String firstName;

    private final String lastName;

    public EmployeeCondition(final String firstName, final String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public boolean matches(final Employee value) {
        return firstName.equals(value.getFirstName()) && lastName.equals(value.getLastName());
    }

}
