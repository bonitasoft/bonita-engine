/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.parameter;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class SParameterProcessNotFoundException extends SBonitaException {

    private static final long serialVersionUID = 946351177446570851L;

    public SParameterProcessNotFoundException(final String message) {
        super(message);
    }

    public SParameterProcessNotFoundException(final Throwable cause) {
        super(cause);
    }

}
