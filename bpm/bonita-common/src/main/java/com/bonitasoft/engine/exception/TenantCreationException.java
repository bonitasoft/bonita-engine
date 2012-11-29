/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 */
public class TenantCreationException extends BonitaException {

    private static final long serialVersionUID = 3451245519674055831L;

    public TenantCreationException(final String message) {
        super(message);
    }

    public TenantCreationException(final Throwable cause) {
        super(cause);
    }

    public TenantCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
