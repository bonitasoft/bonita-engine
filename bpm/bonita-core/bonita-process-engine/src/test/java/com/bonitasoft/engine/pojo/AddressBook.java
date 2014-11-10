package com.bonitasoft.engine.pojo;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaRuntimeException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AddressBook implements Entity {

    private static final long serialVersionUID = -4669250943602795814L;

    @JsonIgnore
    private Address address;

    @JsonIgnore
    private List<Address> addresses;

    @Override
    public Long getPersistenceId() {
        return 1983L;
    }

    @Override
    public Long getPersistenceVersion() {
        return 46587646L;
    }

    @LazyLoaded
    public Address getAddress() {
        throw new BonitaRuntimeException("Proxy");
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    @LazyLoaded
    public List<Address> getAddresses() {
        throw new BonitaRuntimeException("Proxy");
    }

    public void setAddresses(final List<Address> addresses) {
        this.addresses = addresses;
    }

}
