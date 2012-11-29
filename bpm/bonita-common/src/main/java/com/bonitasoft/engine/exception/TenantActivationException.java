/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 */
public class TenantActivationException extends BonitaException {

    private static final long serialVersionUID = -4739521170756848192L;

    public TenantActivationException(final String message) {
        super(message);
    }

    public TenantActivationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TenantActivationException(final Throwable cause) {
        super(cause);
    }

}
