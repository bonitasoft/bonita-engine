package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.persistence.PersistentObject;

public class Address extends SPersistentObjectImpl {

    private static final long serialVersionUID = 1L;

    private String address;

    private long employeeId; // The relationship between Employee and Address is
                             // one-to-many.

    public Address(final String address) {
        this.address = address;
    }

    public Address() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final long employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String getDiscriminator() {
        return Address.class.getName();
    }

    @Override
    public long getSourceObjectId() {
        return 0;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return Address.class;
    }

}
