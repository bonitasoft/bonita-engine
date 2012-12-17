/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.parameter;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class SOutOfBoundException extends SBonitaException {

    private static final long serialVersionUID = -2729330464039642649L;

    public SOutOfBoundException(final String message) {
        super(message);
    }

}
