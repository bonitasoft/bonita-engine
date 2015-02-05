/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

/**
 * @author Matthieu Chaffotte
 */
public class SBusinessDataRepositoryDeploymentException extends SBusinessDataRepositoryException {

    private static final long serialVersionUID = 3729004984895038663L;

    public SBusinessDataRepositoryDeploymentException(final String message) {
        super(message);
    }

    public SBusinessDataRepositoryDeploymentException(final Throwable cause) {
        super(cause);
    }

    public SBusinessDataRepositoryDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
