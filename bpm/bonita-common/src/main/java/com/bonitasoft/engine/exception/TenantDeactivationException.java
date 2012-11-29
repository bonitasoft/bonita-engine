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
public class TenantDeactivationException extends BonitaException {

    private static final long serialVersionUID = -4828922043790622901L;

    public TenantDeactivationException(final String message) {
        super(message);
    }

    public TenantDeactivationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TenantDeactivationException(final Throwable cause) {
        super(cause);
    }

}
