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
public class SBusinessDataRepositoryException extends SBonitaException {

    private static final long serialVersionUID = -2517115818095061435L;

    public SBusinessDataRepositoryException(final String message) {
        super(message);
    }

    public SBusinessDataRepositoryException(final Throwable cause) {
        super(cause);
    }

    public SBusinessDataRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
