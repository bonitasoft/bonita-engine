/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Bole Zhang
 */
public class TenantAlreadyExistException extends BonitaException {

    private static final long serialVersionUID = 9066161421854604750L;

    public TenantAlreadyExistException(final String message) {
        super(message);
    }

    public TenantAlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
