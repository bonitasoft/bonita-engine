/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.CreationException;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class InvalidNameException extends CreationException {

    private static final long serialVersionUID = 2286268061425067776L;

    public InvalidNameException(final String message) {
        super(message);
    }

}
