package org.bonitasoft.engine.api.impl.transaction.expression.bdm;

import java.util.List;

import org.bonitasoft.engine.bdm.Entity;

public class Employee implements Entity {

    private static final long serialVersionUID = 4877386043381866907L;

    private Long persistenceId;

    private Long persistenceVersion;

    private String firstName;

    private String lastName;

    private Address address;

    private List<Address> addresses;

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<Address> addresses) {
        this.addresses = addresses;
    }

}
