/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import com.bonitasoft.engine.bdm.Entity;

/**
 * Simple Business Data test class
 */
class SimpleBizData implements Entity {

    private static final long serialVersionUID = 1L;

    private final Long id;

    public SimpleBizData() {
        id = null;
    }

    public SimpleBizData(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Long getPersistenceId() {
        return null;
    }

    @Override
    public Long getPersistenceVersion() {
        return null;
    }
}
