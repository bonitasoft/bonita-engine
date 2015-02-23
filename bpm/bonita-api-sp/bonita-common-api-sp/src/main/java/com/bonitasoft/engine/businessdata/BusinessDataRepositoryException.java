/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown if an exception occurs dealing with the Business Data Repository.
 *
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.business.data.BusinessDataRepositoryException} instead.
 */
@Deprecated
public class BusinessDataRepositoryException extends BonitaException {

    private static final long serialVersionUID = -1056166500737611443L;

    /**
     * Constructs a BusinessDataRepositoryException with the specified cause.
     *
     * @param cause the cause
     */
    public BusinessDataRepositoryException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BusinessDataRepositoryException with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessDataRepositoryException(final String message) {
        super(message);
    }

    /**
     * Constructs a BusinessDataRepositoryException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public BusinessDataRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
