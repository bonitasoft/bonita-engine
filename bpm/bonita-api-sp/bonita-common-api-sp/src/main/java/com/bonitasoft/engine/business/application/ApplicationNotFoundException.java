/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

/**
 * @author Elias Ricken de Medeiros
 *
 * @see org.bonitasoft.engine.business.application.ApplicationNotFoundException
 *
 * @deprecated from version 7.0 on, use {@link org.bonitasoft.engine.business.application.ApplicationNotFoundException} instead.
 */
@Deprecated
public class ApplicationNotFoundException extends org.bonitasoft.engine.business.application.ApplicationNotFoundException {

    private static final long serialVersionUID = -4073233652785845623L;

    public ApplicationNotFoundException(final long applicationId) {
        super(applicationId);
    }

}
