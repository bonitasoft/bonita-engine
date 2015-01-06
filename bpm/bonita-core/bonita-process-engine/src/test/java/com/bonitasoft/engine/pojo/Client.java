package com.bonitasoft.engine.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

public class Client implements Entity {

    private static final long serialVersionUID = -4669250943602795814L;

    @Id
    @GeneratedValue
    private final Long persistenceId;

    @Version
    private final Long persistenceVersion;

    private Address address;

    private List<Address> addresses;

    public Client() {
        addresses = new ArrayList<Address>();
        persistenceId=753L;
        persistenceVersion = 148L;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    @LazyLoaded
    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    @LazyLoaded
    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<Address> addresses) {
        this.addresses = addresses;
    }

}
