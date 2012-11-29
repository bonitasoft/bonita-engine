/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Bole Zhang
 */
public class LogNotFoundException extends BonitaException {

    private static final long serialVersionUID = 1901535152006080386L;

    public LogNotFoundException(final String message) {
        super(message);
    }

    public LogNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
