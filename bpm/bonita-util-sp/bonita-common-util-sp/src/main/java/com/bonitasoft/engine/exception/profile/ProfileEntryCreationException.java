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
public class ProfileEntryCreationException extends BonitaException {

    private static final long serialVersionUID = -5779388521864179290L;

    public ProfileEntryCreationException(final String message) {
        super(message);
    }

    public ProfileEntryCreationException(final Throwable t) {
        super(t);
    }

}
