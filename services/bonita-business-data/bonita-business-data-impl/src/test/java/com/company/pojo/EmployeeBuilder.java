package com.company.pojo;

public class EmployeeBuilder {

    private String firstName = "aFirstName";

    private String lastName = "aLastName";

    public static EmployeeBuilder anEmployee() {
        return new EmployeeBuilder();
    }

    public Employee build() {
        return new Employee(firstName, lastName);
    }

    public EmployeeBuilder withFirstName(final String firstName) {
        this.firstName = firstName;
        return this;
    }

    public EmployeeBuilder withLastName(final String lastName) {
        this.lastName = lastName;
        return this;
    }
}
