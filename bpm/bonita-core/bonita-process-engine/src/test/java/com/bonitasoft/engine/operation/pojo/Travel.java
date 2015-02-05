/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
