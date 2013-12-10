/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.lang.reflect.Method;

import org.bonitasoft.engine.api.impl.ServerAPIImpl;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.Session;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.TenantModeException;

/**
 * @author Emmanuel Duchastenier
 */
public class ServerAPIExt extends ServerAPIImpl implements ServerAPI {

    private static final long serialVersionUID = 1L;

    private static final String IS_TENANT_IN_MAINTENANCE_METHOD_NAME = "isTenantInMaintenance";

    public ServerAPIExt() {
        super();
    }

    public ServerAPIExt(final boolean cleanSession) {
        super(cleanSession);
    }

    public ServerAPIExt(final boolean cleanSession, final APIAccessResolver accessResolver) {
        super(cleanSession, accessResolver);
    }

    protected void logTenantInMaintenanceMessage(final String apiInterfaceName, final String methodName) {
        logTechnicalErrorMessage("Tenant in Maintenance. Method '" + apiInterfaceName + "." + methodName
                + "' cannot be called until the tenant mode is RUNNING again (TenantAPI.setTenantMode())");
    }

    private boolean isTenantInAValidModeFor(final Method method, final long tenantId, final Session session) {
        return method.isAnnotationPresent(AvailableOnMaintenanceTenant.class) || isTenantAvailable(tenantId, session);
    }

    @Override
    protected void checkMethodAccessibility(final String apiInterfaceName, final Method method, final Session session) {
        super.checkMethodAccessibility(apiInterfaceName, method, session);
        // Tenant-level method call:
        if (session instanceof APISession) {
            long tenantId = ((APISession) session).getTenantId();
            if (!isTenantInAValidModeFor(method, tenantId, session)) {
                logTenantInMaintenanceMessage(apiInterfaceName, method.getName());
                throw new TenantModeException("Tenant with ID " + tenantId + " in maintenance, no API call on this tenant can be made for now.");
            }
        }

    }

    /**
     * @param tenantId
     *            the ID of the tenant to check
     * @param session
     * @return true if the tenant is available, false otherwise (if the tenant mode is in Maintenance)
     */
    protected boolean isTenantAvailable(final long tenantId, final Session session) {
        try {
            final Object apiImpl = accessResolver.getAPIImplementation(TenantManagementAPI.class.getName());
            final Method method = ClassReflector.getMethod(apiImpl.getClass(), IS_TENANT_IN_MAINTENANCE_METHOD_NAME, long.class);
            Boolean inMaintenance = (Boolean) invokeAPIInTransaction(new Object[] { tenantId }, apiImpl, method, session);
            return !inMaintenance;
        } catch (Throwable e) {
            logTechnicalErrorMessage("Cannot determine if the tenant with ID " + tenantId + " is accessible: " + e.getMessage());
            return false;
        }
    }

}
