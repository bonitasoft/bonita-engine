package com.bonitasoft.engine.operation.pojo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.bonitasoft.engine.bdm.Entity;

public class Employee implements Entity {

    private static final long serialVersionUID = 4877386043381866907L;

    private Long persistenceId;

    private Long persistenceVersion;

    private String firstName;

    private String lastName;

    public Employee() {
        super();
    }

    public Employee(final Long id, final Long persistenceVersion, final String firstName, final String lastName) {
        super();
        persistenceId = id;
        this.persistenceVersion = persistenceVersion;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(final Long id) {
        persistenceId = id;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(persistenceId).append(persistenceVersion).append(firstName).append(lastName).build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Employee other = (Employee) obj;
        return new EqualsBuilder().append(persistenceId, other.persistenceId).append(persistenceVersion, other.persistenceVersion)
                .append(firstName, other.firstName).append(lastName, other.lastName).build();
    }

    @Override
    public String toString() {
        return "Employee [id=" + persistenceId + ", version=" + persistenceVersion + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }

}
