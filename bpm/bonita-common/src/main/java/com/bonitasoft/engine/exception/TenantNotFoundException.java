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
public class TenantNotFoundException extends BonitaException {

    private static final long serialVersionUID = 5645446219034832398L;

    public TenantNotFoundException(final long tenantId) {
        super("Unable to find the tenant with id" + tenantId);
    }

    public TenantNotFoundException(final String message) {
        super(message);
    }

}
