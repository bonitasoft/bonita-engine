/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.exception;

import java.text.MessageFormat;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Baptiste Mesta
 */
public class TenantNotActivatedException extends BonitaException {

    private static final long serialVersionUID = -8827675679190216044L;

    public TenantNotActivatedException(final String tenantName) {
        super(MessageFormat.format("the tenant with name ''{0}'' is not activated", tenantName));
    }

    public TenantNotActivatedException(final long tenantId) {
        super(MessageFormat.format("the tenant with id ''{0}'' is not activated", tenantId));
    }
}
