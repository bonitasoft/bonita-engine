/*******************************************************************************
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.transaction.expression.bdm;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.lazy.LazyLoaded;

/**
 * @author Romain Bioteau
 * @author Laurent Leseigneur
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
