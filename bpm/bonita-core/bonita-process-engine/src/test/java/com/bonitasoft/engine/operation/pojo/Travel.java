package com.bonitasoft.engine.operation.pojo;

import com.bonitasoft.engine.bdm.Entity;

public class Travel implements Entity {

    private static final long serialVersionUID = 1L;

    private int nbDays;

    private Long persistenceId;

    private Long persistenceVersion;

    public int getNbDays() {
        return nbDays;
    }

    public void setNbDays(final int nbDays) {
        this.nbDays = nbDays;
    }

    @Override
    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    @Override
    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(final Long id) {
        persistenceId = id;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

}
