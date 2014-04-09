package com.bonitasoft.pojo;

public class EmployeeBuilder {

    private Long persistenceId = 1L;

    private String firstName = "aFirstName";

    private String lastName = "aLastName";

    public static EmployeeBuilder anEmployee() {
        return new EmployeeBuilder();
    }

    public Employee build() {
        return new Employee(persistenceId, firstName, lastName);
    }

    public EmployeeBuilder withId(final long id) {
        persistenceId = id;
        return this;
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
