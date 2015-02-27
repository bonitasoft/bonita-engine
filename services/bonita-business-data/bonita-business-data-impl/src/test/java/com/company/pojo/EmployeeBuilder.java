/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
