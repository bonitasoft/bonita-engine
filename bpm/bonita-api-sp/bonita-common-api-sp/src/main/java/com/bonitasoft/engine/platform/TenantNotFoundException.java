/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown when an exception happens during the retrieving of a tenant
 *
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @see com.bonitasoft.engine.api.PlatformAPI#getTenantByName(String)
 * @see com.bonitasoft.engine.api.PlatformAPI#getTenantById(long)
 * @since 6.0.0
 */
public class TenantNotFoundException extends BonitaException {

    private static final long serialVersionUID = 5645446219034832398L;

    public TenantNotFoundException(final long tenantId) {
        super("Unable to find the tenant with id: " + tenantId);
    }

    public TenantNotFoundException(final String message) {
        super(message);
    }

}
