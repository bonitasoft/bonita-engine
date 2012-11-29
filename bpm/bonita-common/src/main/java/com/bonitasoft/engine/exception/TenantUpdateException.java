/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Yanyan Liu
 */
public class TenantUpdateException extends BonitaException {

    private static final long serialVersionUID = 4935191223304265973L;

    public TenantUpdateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TenantUpdateException(final String message) {
        super(message);
    }

    public TenantUpdateException(final Throwable cause) {
        super(cause);
    }

}
