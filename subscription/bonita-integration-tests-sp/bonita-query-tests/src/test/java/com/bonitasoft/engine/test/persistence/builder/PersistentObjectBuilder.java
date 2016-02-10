/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.test.persistence.builder;

import java.util.Random;

import org.bonitasoft.engine.persistence.PersistentObject;


public abstract class PersistentObjectBuilder<T extends PersistentObject> {

    public static final long DEFAULT_TENANT_ID = 1L;

    protected long id = new Random().nextLong();

    public T build() {
        final T persistentObject = _build();
        persistentObject.setId(id);
        persistentObject.setTenantId(DEFAULT_TENANT_ID);
        return persistentObject;
    }

    abstract T _build();

}
