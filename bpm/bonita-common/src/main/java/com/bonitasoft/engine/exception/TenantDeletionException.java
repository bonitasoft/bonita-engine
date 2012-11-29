/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Lu Kai
 */
public class TenantDeletionException extends BonitaException {

    private static final long serialVersionUID = 4494635860478136174L;

    public TenantDeletionException(final String message) {
        super(message);
    }

    public TenantDeletionException(final long tenantId) {
        super("Unable to delete the tenant with id " + tenantId);
    }

}
