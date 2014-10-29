/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown when an exception happens during the deactivation of a tenant
 *
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @see com.bonitasoft.engine.api.PlatformAPI#deactiveTenant(long)
 * @since 6.0.0
 */
public class TenantDeactivationException extends BonitaException {

    private static final long serialVersionUID = -4828922043790622901L;

    public TenantDeactivationException(final String message) {
        super(message);
    }

    public TenantDeactivationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
