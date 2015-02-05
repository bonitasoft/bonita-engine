/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Zhao Na
 */
public class ProfileImportException extends BonitaException {

    private static final long serialVersionUID = 1093089087299418396L;

    public ProfileImportException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ProfileImportException(final String message) {
        super(message);
    }

    public ProfileImportException(final Throwable cause) {
        super(cause);
    }
}
