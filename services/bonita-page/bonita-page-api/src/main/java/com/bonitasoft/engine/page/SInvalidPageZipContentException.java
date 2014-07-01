/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import java.io.IOException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * 
 * content of the page is not valid
 * 
 * @author Baptiste Mesta
 * 
 */
public class SInvalidPageZipContentException extends SBonitaException {

    private static final long serialVersionUID = -7263291210428082852L;

    public SInvalidPageZipContentException(final String message) {
        super(message);
    }

    public SInvalidPageZipContentException(final String string, final IOException e) {
        super(string, e);
    }

}
