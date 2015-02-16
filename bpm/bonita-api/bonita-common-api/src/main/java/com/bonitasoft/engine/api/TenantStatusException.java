/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.BonitaRuntimeException;

/**
 * Thrown when we try to access on a paused tenant an API method that cannot be called on a paused tenant, or when we try to access on a running tenant an API
 * method that cannot be called on a running tenant.
 * 
 * @author Emmanuel Duchastenier
 * @deprecated since version 7.0.  This class was only introduced to avoid an API break of Bonita Engine Subscription version. Use {@link org.bonitasoft.engine.exception.TenantStatusException} instead.
 */
@Deprecated
public class TenantStatusException extends BonitaRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *        the exception message
     */
    public TenantStatusException(final String message) {
        super(message);
    }

}
