package com.bonitasoft.engine.operation;

public class Employee {

    private Long persistenceId;

    private String firstName;

    private String lastName;

    protected Employee() {
        super();
    }

    public Employee(final Long id, final String firstName, final String lastName) {
        super();
        this.persistenceId = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getPersistenceId() {
        return persistenceId;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + (firstName == null ? 0 : firstName.hashCode());
        result = prime * result + (persistenceId == null ? 0 : persistenceId.hashCode());
        result = prime * result + (lastName == null ? 0 : lastName.hashCode());
        return result;
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
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        } else if (!firstName.equals(other.firstName)) {
            return false;
        }
        if (persistenceId == null) {
            if (other.persistenceId != null) {
                return false;
            }
        } else if (!persistenceId.equals(other.persistenceId)) {
            return false;
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else if (!lastName.equals(other.lastName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Employee [id=" + persistenceId + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }

}
