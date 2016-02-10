/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.theme.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown when setting a new custom theme cannot be fulfilled.
 * 
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public class SetThemeException extends BonitaException {

    private static final long serialVersionUID = 3465858452506324242L;

    public SetThemeException(final String message) {
        super(message);
    }

    public SetThemeException(final Throwable cause) {
        super(cause);
    }

    public SetThemeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
