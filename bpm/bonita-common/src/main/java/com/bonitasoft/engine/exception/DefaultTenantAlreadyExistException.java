/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Bole Zhang
 */
public class DefaultTenantAlreadyExistException extends BonitaException {

    private static final long serialVersionUID = -8081816540437747847L;

    public DefaultTenantAlreadyExistException() {
        super("A default tenant already esists");
    }

    public DefaultTenantAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
