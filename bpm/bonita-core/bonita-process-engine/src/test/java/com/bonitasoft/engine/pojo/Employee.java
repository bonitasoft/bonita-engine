/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.pojo;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaRuntimeException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

public class Employee implements Entity {

    private static final long serialVersionUID = -4669250943602795814L;


    private Address address;


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
