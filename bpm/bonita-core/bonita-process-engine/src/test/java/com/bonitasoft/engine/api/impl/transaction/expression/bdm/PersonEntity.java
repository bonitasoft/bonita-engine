/*******************************************************************************
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.expression.bdm;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

/**
 * @author Romain Bioteau
 */
public class PersonEntity implements Entity {

    public PersonEntity() {

    }

    /*
     * (non-Javadoc)
     * @see com.bonitasoft.engine.bdm.Entity#getPersistenceId()
     */
    @Override
    public Long getPersistenceId() {
        return 1L;
    }

    /*
     * (non-Javadoc)
     * @see com.bonitasoft.engine.bdm.Entity#getPersistenceVersion()
     */
    @Override
    public Long getPersistenceVersion() {
        return null;
    }

    @LazyLoaded
    public String getWithLazyLoadedAnnotation() {
        return "getWithLazyLoadedAnnotation";
    }

    public String getWithoutLazyLoadedAnnotation() {
        return "getWithoutLazyLoadedAnnotation";
    }
}
