package com.bonitasoft.engine.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.Entity;

public class Country implements Entity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private final Long persistenceId;

    @Version
    private final Long persistenceVersion;

    private final String name;

    public Country(final Long persistenceId, final Long persistenceVersion, final String name) {
        this.persistenceId = persistenceId;
        this.persistenceVersion = persistenceVersion;
        this.name = name;

    }

    public String getName() {
        return name;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

}
