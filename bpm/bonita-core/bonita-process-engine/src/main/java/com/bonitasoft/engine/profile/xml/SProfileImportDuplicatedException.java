/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile.xml;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Zhao Na
 */
public class SProfileImportDuplicatedException extends SBonitaException {

    private static final long serialVersionUID = -6338979347415161630L;

    public SProfileImportDuplicatedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SProfileImportDuplicatedException(final String message) {
        super(message);
    }

    public SProfileImportDuplicatedException(final Throwable cause) {
        super(cause);
    }

}
