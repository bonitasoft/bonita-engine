/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.exception.profile;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Celine Souchet
 */
public class ProfileUpdateException extends BonitaException {

    private static final long serialVersionUID = 6783985465730300732L;

    public ProfileUpdateException(final Throwable cause) {
        super(cause);
    }

    public ProfileUpdateException(final String message) {
        super(message);
    }

}
