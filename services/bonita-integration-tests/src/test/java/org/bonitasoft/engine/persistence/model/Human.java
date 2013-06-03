package org.bonitasoft.engine.persistence.model;

import org.bonitasoft.engine.persistence.PersistentObject;

public class Human implements PersistentObject {

    private static final long serialVersionUID = 1L;

    private long tenantId;

    private long id;

    private String firstName;

    private String lastName;

    private int age;

    private long carId;

    private boolean deleted;

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
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

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(final long carId) {
        this.carId = carId;
    }

    @Override
    public String getDiscriminator() {
        return null;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + age;
        result = prime * result + (int) (carId ^ (carId >>> 32));
        result = prime * result + (deleted ? 1231 : 1237);
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + (int) (tenantId ^ (tenantId >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Human other = (Human) obj;
        if (age != other.age)
            return false;
        if (carId != other.carId)
            return false;
        if (deleted != other.deleted)
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (id != other.id)
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        if (tenantId != other.tenantId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Human [tenantId=" + tenantId + ", id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", age=" + age + ", carId=" + carId + "]";
    }

}
