package com.bonitasoft.engine.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.Entity;

public class Address implements Entity {

    private static final long serialVersionUID = -2128636663862820028L;

    @Id
    @GeneratedValue
    private Long persistenceId;

    @Version
    private Long persistenceVersion;

    private String street;

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public void setPersistenceId(final Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    public void setPersistenceVersion(final Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

}
