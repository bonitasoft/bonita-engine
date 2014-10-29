/*******************************************************************************
 * Copyright (C) 2013, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata;

/**
 * Thrown to indicate that the Business Data Model is invalid. So it cannot be deployed.
 *
 * @author Colin Puy
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class InvalidBusinessDataModelException extends BusinessDataRepositoryException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an InvalidBusinessDataModelException with the specified cause.
     *
     * @param cause the cause
     */
    public InvalidBusinessDataModelException(final Throwable cause) {
        super(cause);
    }

}
