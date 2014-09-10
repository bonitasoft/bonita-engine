/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SInvalidNameException extends SBonitaException {

    private static final long serialVersionUID = -1258822999709724221L;

    public SInvalidNameException(final String message) {
        super(message);
    }

}
