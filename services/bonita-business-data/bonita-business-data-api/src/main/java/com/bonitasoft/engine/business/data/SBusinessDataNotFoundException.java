/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class SBusinessDataNotFoundException extends SBonitaException {

    private static final long serialVersionUID = -4470717601583219790L;

    public SBusinessDataNotFoundException(final String message) {
        super(message);
    }

    public SBusinessDataNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
