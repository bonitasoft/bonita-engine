/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.business.application;

/**
 * Contains available policies during {@link Application}s import
 * @author Elias Ricken de Medeiros
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationImportPolicy} instead.
 * @see com.bonitasoft.engine.business.application.Application
 * @see org.bonitasoft.engine.business.application.ApplicationImportPolicy
 */
@Deprecated
public enum ApplicationImportPolicy {

    /**
     * Import will fail on import an existent {@link com.bonitasoft.engine.business.application.Application} or {@link com.bonitasoft.engine.business.application.ApplicationPage}
     */
    FAIL_ON_DUPLICATES

}
