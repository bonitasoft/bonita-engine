/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.parameter;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class SParameterNameNotFoundException extends SBonitaException {

    private static final long serialVersionUID = 6019783138024113896L;

    public SParameterNameNotFoundException(final Throwable cause) {
        super(cause);
    }

    public SParameterNameNotFoundException(final String message) {
        super(message);
    }

}
