package com.bonitasoft.engine.operation.pojo;

import com.bonitasoft.engine.bdm.Entity;

public class Travel implements Entity {

    private static final long serialVersionUID = 1L;

    private int nbDays;

    public int getNbDays() {
        return nbDays;
    }

    public void setNbDays(final int nbDays) {
        this.nbDays = nbDays;
    }

    @Override
    public Long getPersistenceId() {
        return 1L;
    }

    @Override
    public Long getPersistenceVersion() {
        return 1L;
    }
}
