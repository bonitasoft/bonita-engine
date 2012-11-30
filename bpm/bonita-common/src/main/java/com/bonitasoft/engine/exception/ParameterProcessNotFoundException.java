/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.ObjectNotFoundException;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterProcessNotFoundException extends ObjectNotFoundException {

    private static final long serialVersionUID = -6743209754015668676L;

    public ParameterProcessNotFoundException(final String message) {
        super(message);
    }

    public ParameterProcessNotFoundException(final Throwable cause) {
        super(cause);
    }

}
