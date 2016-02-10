/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import org.bonitasoft.engine.exception.CreationException;

/**
 * Thrown to indicate that the DAO was not created.
 *
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.bdm.BusinessObjectDaoCreationException} instead.
 */
@Deprecated
public class BusinessObjectDaoCreationException extends CreationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a BusinessObjectDaoCreationException with the specified cause.
     *
     * @param cause the cause
     */
    public BusinessObjectDaoCreationException(final Throwable cause) {
        super(cause);
    }

}
