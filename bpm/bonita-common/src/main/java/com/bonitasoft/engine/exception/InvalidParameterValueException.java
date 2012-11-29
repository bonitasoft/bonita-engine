/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class InvalidParameterValueException extends BonitaException {

    private static final long serialVersionUID = 7463213076180306458L;

    public InvalidParameterValueException(final String message) {
        super(message);
    }

    public InvalidParameterValueException(final Throwable e) {
        super(e);
    }

}
