/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import com.bonitasoft.engine.bdm.Entity;

public class Address implements Entity {

    private static final long serialVersionUID = -7765232426654390190L;

    @Override
    public Long getPersistenceId() {
        return 45L;
    }

    @Override
    public Long getPersistenceVersion() {
        return 4687634L;
    }

}
