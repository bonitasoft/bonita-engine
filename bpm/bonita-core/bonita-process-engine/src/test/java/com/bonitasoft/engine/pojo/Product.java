package com.bonitasoft.engine.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.Entity;

public class Product implements Entity {

    private static final long serialVersionUID = 1897389564266299869L;

    @Id
    @GeneratedValue
    private final Long persistenceId;

    @Version
    private final Long persistenceVersion;

    private final String name;

    public Product(final Long persistenceId, final Long persistenceVersion, final String name) {
        super();
        this.persistenceId = persistenceId;
        this.persistenceVersion = persistenceVersion;
        this.name = name;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public String getName() {
        return name;
    }

}
