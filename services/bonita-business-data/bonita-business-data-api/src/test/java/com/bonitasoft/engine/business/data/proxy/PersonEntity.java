/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/
package com.bonitasoft.engine.business.data.proxy;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

/**
 * @author Romain Bioteau
 */
public class PersonEntity implements Entity {

    public PersonEntity() {

    }

    @Override
    public Long getPersistenceId() {
        return 1L;
    }

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
