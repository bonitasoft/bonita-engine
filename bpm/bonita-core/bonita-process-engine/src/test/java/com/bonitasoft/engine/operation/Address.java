package com.bonitasoft.engine.operation;

import com.bonitasoft.engine.bdm.Entity;

public class Address implements Entity {

    private static final long serialVersionUID = -7765232426654390190L;

    private Long persistenceId;

    public Address(final Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    @Override
    public Long getPersistenceVersion() {
        return 4687634L;
    }

    @Override
    public String toString() {
        return "Address{" +
                "persistenceId=" + persistenceId +
                '}';
    }
}
