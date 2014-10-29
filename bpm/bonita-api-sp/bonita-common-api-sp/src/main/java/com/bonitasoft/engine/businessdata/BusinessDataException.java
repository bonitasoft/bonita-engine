/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown when a BusinessData problem occurs, for instance, passed object parameter is not a Business Data
 *
 * @author Emmanuel Duchastenier
 * @deprecated unused
 */
@Deprecated
public class BusinessDataException extends BonitaException {

    private static final long serialVersionUID = -7068505030248451684L;

    /**
     * Constructs a BusinessDataException with the specified cause.
     *
     * @param cause the cause
     */
    public BusinessDataException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BusinessDataException with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessDataException(final String message) {
        super(message);
    }

    /**
     * Constructs a BusinessDataException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public BusinessDataException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
