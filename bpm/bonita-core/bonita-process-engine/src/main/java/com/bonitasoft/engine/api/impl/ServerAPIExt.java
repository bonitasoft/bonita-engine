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
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.ServerAPIImpl;
import org.bonitasoft.engine.api.impl.ServerAPIRuntimeException;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.transaction.UserTransactionService;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.TenantStatusException;

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
    protected void checkMethodAccessibility(final Object apiImpl, final String apiInterfaceName, final Method method, final Session session, boolean isInTransaction) {
        super.checkMethodAccessibility(apiImpl, apiInterfaceName, method, session, isInTransaction);
        // we don't check if tenant is in pause mode at platform level and when there is no session
        // when there is no session means that we are trying to log in, in this case it is the LoginApiExt that check if the user is the technical user
        // For tenant level method call:
        if (session instanceof APISession) {
            final long tenantId = ((APISession) session).getTenantId();
            checkTenantIsInAValidModeFor(apiImpl, method, apiInterfaceName, tenantId, session, isInTransaction);
        }
    }


    private void checkTenantIsInAValidModeFor(final Object apiImpl, final Method method, final String apiInterfaceName, final long tenantId,
                                              final Session session,
                                              boolean isInTransaction) {
        final boolean tenantRunning = isTenantAvailable(tenantId, session, isInTransaction);
        final AvailableWhenTenantIsPaused methodAnnotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);
        AvailableWhenTenantIsPaused annotation = null;
        if (methodAnnotation != null) {
            annotation = methodAnnotation;
        } else {
            final Class<?> apiClass = apiImpl.getClass();
            annotation = apiClass.getAnnotation(AvailableWhenTenantIsPaused.class);
        }
        checkIsValidModeFor(tenantRunning, annotation, tenantId, apiImpl, method, apiInterfaceName);
    }

    protected void checkIsValidModeFor(final boolean tenantRunning, final AvailableWhenTenantIsPaused annotation, final long tenantId, final Object apiImpl,
            final Method method, final String apiInterfaceName) {
        // On running tenant, annotation must not be present, or present without ONLY flag:
        boolean okOnRunningTenant = isMethodAvailableOnRunningTenant(tenantRunning, annotation);
        // On paused tenant, annotation must be present (without consideration on ONLY flag):
        boolean okOnPausedTenant = isMethodAvailableOnPausedTenant(tenantRunning, annotation);
        if (!(okOnRunningTenant || okOnPausedTenant)) {
            if (tenantRunning) {
                methodCannotBeCalledOnRunningTenant(apiImpl, apiInterfaceName, method, tenantId);
            } else {
                methodCannotBeCalledOnPausedTenant(apiImpl, apiInterfaceName, method, tenantId);
            }
        }
    }

    protected void methodCannotBeCalledOnRunningTenant(final Object apiImpl, final String apiInterfaceName, final Method method, final long tenantId) {
        logTechnicalErrorMessage("Tenant is running. Method '" + apiInterfaceName + "." + method.getName() + "' on implementation '"
                + apiImpl.getClass().getSimpleName() + "' can only be called when the tenant is PAUSED (call TenantManagementAPI.pause() first)");
        throw new TenantStatusException("Tenant with ID " + tenantId + " is running, method '" + apiInterfaceName + "." + method.getName()
                + "()' cannot be called.");
    }

    protected void methodCannotBeCalledOnPausedTenant(final Object apiImpl, final String apiInterfaceName, final Method method, final long tenantId) {
        logTechnicalErrorMessage("Tenant in pause. Method '" + apiInterfaceName + "." + method.getName() + "' on implementation '"
                + apiImpl.getClass().getSimpleName() + "' cannot be called until the tenant mode is RUNNING again (call TenantManagementAPI.resume() first)");
        throw new TenantStatusException("Tenant with ID " + tenantId + " is in pause, no API call on this tenant can be made for now.");
    }

    protected boolean isMethodAvailableOnPausedTenant(final boolean tenantRunning, final AvailableWhenTenantIsPaused annotation) {
        return !tenantRunning && annotation != null;
    }

    protected boolean isMethodAvailableOnRunningTenant(final boolean tenantRunning, final AvailableWhenTenantIsPaused annotation) {
        return tenantRunning && (annotation == null || !annotation.only());
    }

    /**
     * @param tenantId
     *            the ID of the tenant to check
     * @param session
     *            the session to user
     * @param isInTransaction
     *          if the request is made in a transaction
     * @return true if the tenant is available, false otherwise (if the tenant is paused)
     */
    protected boolean isTenantAvailable(final long tenantId, final Session session, boolean isInTransaction) {
        final Object apiImpl;
        try {
            apiImpl = accessResolver.getAPIImplementation(TenantManagementAPI.class.getName());
            final Method method = ClassReflector.getMethod(apiImpl.getClass(), IS_PAUSED);
            final Boolean paused;
            if(isInTransaction){
                paused = (Boolean) invokeAPI(new Object[0], apiImpl, method);
            }else{
                final UserTransactionService userTransactionService = selectUserTransactionService(session, getSessionType(session));

                final Callable<Object> callable = new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        try {
                            return invokeAPI(new Object[0], apiImpl, method);
                        } catch (final Throwable cause) {
                            throw new ServerAPIRuntimeException(cause);
                        }
                    }
                };

                paused = (Boolean) userTransactionService.executeInTransaction(callable);
            }
            return !paused;
        } catch (final Throwable e) {
            throw new BonitaRuntimeException("Cannot determine if the tenant with ID " + tenantId + " is accessible", e);
        }
    }

}
