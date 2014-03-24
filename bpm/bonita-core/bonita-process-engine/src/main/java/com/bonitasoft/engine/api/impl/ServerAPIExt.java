/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.lang.reflect.Method;

import org.bonitasoft.engine.api.impl.ServerAPIImpl;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.Session;

import com.bonitasoft.engine.api.TenantIsPausedException;
import com.bonitasoft.engine.api.TenantManagementAPI;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ServerAPIExt extends ServerAPIImpl {

    private static final long serialVersionUID = -7305837284567265464L;

    private static final String IS_PAUSED = "isPaused";

    public ServerAPIExt() {
        super();
    }

    public ServerAPIExt(final boolean cleanSession) {
        super(cleanSession);
    }

    public ServerAPIExt(final boolean cleanSession, final APIAccessResolver accessResolver) {
        super(cleanSession, accessResolver);
    }

    @Override
    protected void checkMethodAccessibility(final Object apiImpl, final String apiInterfaceName, final Method method, final Session session) {
        super.checkMethodAccessibility(apiImpl, apiInterfaceName, method, session);
        // we don't check if tenant is in maintenance at platform level and when there is no session
        // when there is no session means that we are trying to log in, in this case it is the LoginApiExt that check if the user is the technical user
        // For tenant level method call:
        if (session instanceof APISession) {
            final long tenantId = ((APISession) session).getTenantId();
            if (!isTenantInAValidModeFor(apiImpl, method, tenantId, session)) {
                logTenantInMaintenanceMessage(apiImpl, apiInterfaceName, method.getName());
                throw new TenantIsPausedException("Tenant with ID " + tenantId + " is in maintenance, no API call on this tenant can be made for now.");
            }
        }
    }

    protected void logTenantInMaintenanceMessage(final Object apiImpl, final String apiInterfaceName, final String methodName) {
        logTechnicalErrorMessage("Tenant in Maintenance. Method '" + apiInterfaceName + "." + methodName + "' of '" + apiImpl
                + "' cannot be called until the tenant mode is RUNNING again (TenantManagementAPI.resume())");
    }

    private boolean isTenantInAValidModeFor(final Object apiImpl, final Method method, final long tenantId, final Session session) {
        final boolean tenantAvailable = isTenantAvailable(tenantId, session);
        final AvailableWhenTenantIsPaused methodAnnotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);
        if (methodAnnotation != null) {
            return isInAValidModeFor(tenantAvailable, methodAnnotation);
        } else {
            final Class<?> apiClass = apiImpl.getClass();
            final AvailableWhenTenantIsPaused classAnnotation = apiClass.getAnnotation(AvailableWhenTenantIsPaused.class);
            return isInAValidModeFor(tenantAvailable, classAnnotation);
        }
    }

    protected boolean isInAValidModeFor(final boolean available, final AvailableWhenTenantIsPaused annotation) {
        return !(annotation == null && !available || available && annotation != null && annotation.only());
    }

    /**
     * @param tenantId
     *            the ID of the tenant to check
     * @param session
     * @return true if the tenant is available, false otherwise (if the tenant is paused)
     */
    protected boolean isTenantAvailable(final long tenantId, final Session session) {
        Object apiImpl;
        try {
            apiImpl = accessResolver.getAPIImplementation(TenantManagementAPI.class.getName());
            final Method method = ClassReflector.getMethod(apiImpl.getClass(), IS_PAUSED);
            final Boolean paused = (Boolean) invokeAPIInTransaction(new Object[0], apiImpl, method, session);
            return !paused;
        } catch (final Throwable e) {
            throw new BonitaRuntimeException("Cannot determine if the tenant with ID " + tenantId + " is accessible", e);
        }
    }

}
