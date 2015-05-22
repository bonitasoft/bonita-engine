/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.NoSessionRequired;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaContextException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.NodeNotStartedException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ServerAPIImpl implements ServerAPI {

    private static final String SESSION = "session";

    private static final String IS_NODE_STARTED_METHOD_NAME = "isNodeStarted";

    private static final long serialVersionUID = -161775388604256321L;
    private static final String IS_PAUSED = "isPaused";

    protected final APIAccessResolver accessResolver;

    private final boolean cleanSession;

    private TechnicalLoggerService technicalLogger;

    protected enum SessionType {
        PLATFORM, API;
    }

    public ServerAPIImpl() {
        this(true);
    }

    public ServerAPIImpl(final boolean cleanSession) {
        try {
            this.cleanSession = cleanSession;
            accessResolver = getServiceAccessorFactoryInstance().createAPIAccessResolver();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    /**
     * For Test Mock usage
     * 
     * @param cleanSession
     * @param accessResolver
     */
    public ServerAPIImpl(boolean cleanSession, APIAccessResolver accessResolver) {
        this.cleanSession = cleanSession;
        this.accessResolver = accessResolver;
    }

    void setTechnicalLogger(final TechnicalLoggerService technicalLoggerService) {
        technicalLogger = technicalLoggerService;
    }

    private ServiceAccessorFactory getServiceAccessorFactoryInstance() {
        return ServiceAccessorFactory.getInstance();
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        technicalTraceLog("Starting ", apiInterfaceName, methodName);
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        SessionAccessor sessionAccessor = null;
        Session session = null;
        try {
            try {
                session = (Session) options.get(SESSION);
                sessionAccessor = beforeInvokeMethod(session, apiInterfaceName);
                return invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues, session);
            } catch (final ServerAPIRuntimeException sapire) {
                throw sapire.getCause();
            }
        } catch (final BonitaRuntimeException bre) {
            fillGlobalContextForException(session, bre);
            throw createServerWrappedException(bre);
        } catch (final BonitaException be) {
            fillGlobalContextForException(session, be);
            throw createServerWrappedException(be);
        } catch (final UndeclaredThrowableException ute) {
            technicalDebugLog(ute);
            throw createServerWrappedException(ute);
        } catch (final Throwable cause) {
            technicalDebugLog(cause);
            final BonitaRuntimeException throwableToWrap = new BonitaRuntimeException(cause);
            fillGlobalContextForException(session, throwableToWrap);
            throw createServerWrappedException(throwableToWrap);
        } finally {
            cleanSessionIfNeeded(sessionAccessor);
            // reset class loader
            Thread.currentThread().setContextClassLoader(baseClassLoader);
            technicalTraceLog("End ", apiInterfaceName, methodName);
        }
    }

    private ServerWrappedException createServerWrappedException(final Throwable throwableToWrap) {
        return new ServerWrappedException(throwableToWrap);
    }

    private void fillGlobalContextForException(final Session session, final BonitaContextException be) {
        fillUserNameContextForException(session, be);
    }

    private void fillUserNameContextForException(final Session session, final BonitaContextException be) {
        if (session != null) {
            final String userName = session.getUserName();
            if (userName != null) {
                be.setUserName(userName);
            }
        }
    }

    private void cleanSessionIfNeeded(SessionAccessor sessionAccessor) {
        if (cleanSession) {
            // clean session id
            if (sessionAccessor != null) {
                sessionAccessor.deleteSessionId();
            }
        }
    }

    private void technicalDebugLog(final Throwable throwableToLog) {
        if (technicalLogger != null && technicalLogger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            technicalLogger.log(this.getClass(), TechnicalLogSeverity.DEBUG, throwableToLog);
        }
    }

    private void technicalTraceLog(String prefix, String apiInterfaceName, String methodName) {
        if (technicalLogger != null && technicalLogger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            technicalLogger.log(this.getClass(), TechnicalLogSeverity.TRACE, prefix + "Server API call " + apiInterfaceName + " " + methodName);
        }
    }

    SessionAccessor beforeInvokeMethod(final Session session, final String apiInterfaceName) throws BonitaHomeNotSetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, BonitaHomeConfigurationException, IOException, NoSuchMethodException,
            InvocationTargetException, SBonitaException {
        SessionAccessor sessionAccessor = null;

        final ServiceAccessorFactory serviceAccessorFactory = getServiceAccessorFactoryInstance();
        final PlatformServiceAccessor platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();

        ClassLoader serverClassLoader = null;
        if (session != null) {
            final SessionType sessionType = getSessionType(session);
            sessionAccessor = serviceAccessorFactory.createSessionAccessor();
            switch (sessionType) {
                case PLATFORM:
                    serverClassLoader = beforeInvokeMethodForPlatformSession(sessionAccessor, platformServiceAccessor, session);
                    break;

                case API:
                    serverClassLoader = beforeInvokeMethodForAPISession(sessionAccessor, serviceAccessorFactory, platformServiceAccessor, session);
                    break;

                default:
                    throw new InvalidSessionException("Unknown session type: " + session.getClass().getName());
            }
        } else if (accessResolver.needSession(apiInterfaceName)) {
            throw new InvalidSessionException("Session is null!");
        }
        if (serverClassLoader != null) {
            Thread.currentThread().setContextClassLoader(serverClassLoader);
        }

        return sessionAccessor;
    }

    private ClassLoader beforeInvokeMethodForAPISession(final SessionAccessor sessionAccessor, final ServiceAccessorFactory serviceAccessorFactory,
            final PlatformServiceAccessor platformServiceAccessor, final Session session) throws  SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        checkTenantSession(platformServiceAccessor, session);
        final long tenantId = ((APISession) session).getTenantId();
        final SessionService sessionService = platformServiceAccessor.getTenantServiceAccessor(tenantId).getSessionService();
        sessionService.renewSession(session.getId());
        sessionAccessor.setSessionInfo(session.getId(), tenantId);
        final ClassLoader serverClassLoader = getTenantClassLoader(platformServiceAccessor, session);
        setTechnicalLogger(serviceAccessorFactory.createTenantServiceAccessor(tenantId).getTechnicalLoggerService());
        return serverClassLoader;
    }

    private ClassLoader beforeInvokeMethodForPlatformSession(final SessionAccessor sessionAccessor, final PlatformServiceAccessor platformServiceAccessor,
            final Session session) throws SSessionException, SClassLoaderException {
        final PlatformSessionService platformSessionService = platformServiceAccessor.getPlatformSessionService();
        final PlatformLoginService loginService = platformServiceAccessor.getPlatformLoginService();

        if (!loginService.isValid(session.getId())) {
            throw new InvalidSessionException("Invalid session");
        }
        platformSessionService.renewSession(session.getId());
        sessionAccessor.setSessionInfo(session.getId(), -1);
        final ClassLoader serverClassLoader = getPlatformClassLoader(platformServiceAccessor);
        setTechnicalLogger(platformServiceAccessor.getTechnicalLoggerService());
        return serverClassLoader;
    }

    protected SessionType getSessionType(final Session session) {
        SessionType sessionType = null;
        if (session instanceof PlatformSession) {
            sessionType = SessionType.PLATFORM;
        } else if (session instanceof APISession) {
            sessionType = SessionType.API;
        }
        return sessionType;
    }

    Object invokeAPI(final String apiInterfaceName, final String methodName, final List<String> classNameParameters, final Object[] parametersValues,
            final Session session) throws Throwable {
        final Class<?>[] parameterTypes = getParameterTypes(classNameParameters);

        final Object apiImpl = accessResolver.getAPIImplementation(apiInterfaceName);
        final Method method = ClassReflector.getMethod(apiImpl.getClass(), methodName, parameterTypes);
        // No session required means that there is no transaction
        if (method.isAnnotationPresent(CustomTransactions.class) || method.isAnnotationPresent(NoSessionRequired.class)) {
            return invokeAPIOutsideTransaction(parametersValues, apiImpl, method, apiInterfaceName, session);
        }else{
            return invokeAPIInTransaction(parametersValues, apiImpl, method, session, apiInterfaceName);
        }
    }

    protected Object invokeAPIOutsideTransaction(Object[] parametersValues, Object apiImpl, Method method, String apiInterfaceName, Session session) throws Throwable {
        checkMethodAccessibility(apiImpl, apiInterfaceName, method, session, /* Not in transaction */false);
        return invokeAPI(parametersValues, apiImpl, method);
    }

    protected void checkMethodAccessibility(final Object apiImpl, final String apiInterfaceName, final Method method, final Session session, boolean isInTransaction) {
        if (!isNodeInAValidStateFor(method)) {
            logNodeNotStartedMessage(apiInterfaceName, method);
            throw new NodeNotStartedException();
        }
        // we don't check if tenant is in pause mode at platform level and when there is no session
        // when there is no session means that we are trying to log in, in this case it is the LoginApiExt that check if the user is the technical user
        // For tenant level method call:
        if (session instanceof APISession) {
            final long tenantId = ((APISession) session).getTenantId();
            checkTenantIsInAValidModeFor(apiImpl, method, apiInterfaceName, tenantId, session, isInTransaction);
        }
    }


    protected void checkTenantIsInAValidModeFor(final Object apiImpl, final Method method, final String apiInterfaceName, final long tenantId,
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
            apiImpl = accessResolver.getAPIImplementation(TenantAdministrationAPI.class.getName());
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


    protected void logNodeNotStartedMessage(final String apiInterfaceName, final Method method) {
        logTechnicalErrorMessage("Node not started. Method '" + apiInterfaceName + "." + method.getName()
                + "' cannot be called until node has been started (PlatformAPI.startNode()). Exact class: " + method.getDeclaringClass().getName());
    }

    protected void logTechnicalErrorMessage(final String message) {
        if (technicalLogger != null) {
            technicalLogger.log(this.getClass(), TechnicalLogSeverity.ERROR, message);
        } else {
            System.err.println(message);
        }
    }

    protected boolean isNodeInAValidStateFor(final Method method) {
        return method.isAnnotationPresent(AvailableOnStoppedNode.class) || isNodeStarted();
    }

    /**
     * @return true if the node is started, false otherwise.
     */
    private boolean isNodeStarted() {
        try {
            final Object apiImpl = accessResolver.getAPIImplementation(PlatformAPI.class.getName());
            final Method method = ClassReflector.getMethod(apiImpl.getClass(), IS_NODE_STARTED_METHOD_NAME, new Class[0]);
            return (Boolean) invokeAPI(new Object[0], apiImpl, method);
        } catch (final Throwable e) {
            return false;
        }
    }

    protected Object invokeAPIInTransaction(final Object[] parametersValues, final Object apiImpl, final Method method, final Session session, final String apiInterfaceName) throws Throwable {
        if (session == null) {
            throw new BonitaRuntimeException("session is null");
        }
        final UserTransactionService userTransactionService = selectUserTransactionService(session, getSessionType(session));

        final Callable<Object> callable = new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    checkMethodAccessibility(apiImpl, apiInterfaceName, method, session, /* Not in transaction */true);
                    return invokeAPI(parametersValues, apiImpl, method);
                } catch (final Throwable cause) {
                    throw new ServerAPIRuntimeException(cause);
                }
            }
        };

        return userTransactionService.executeInTransaction(callable);
    }

    protected UserTransactionService selectUserTransactionService(final Session session, final SessionType sessionType) throws BonitaHomeNotSetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        UserTransactionService transactionService = null;
        final ServiceAccessorFactory serviceAccessorFactory = getServiceAccessorFactoryInstance();
        final PlatformServiceAccessor platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
        switch (sessionType) {
            case PLATFORM:
                transactionService = platformServiceAccessor.getTransactionService();
                break;
            case API:
                final TenantServiceAccessor tenantAccessor = platformServiceAccessor.getTenantServiceAccessor(((APISession) session).getTenantId());
                transactionService = tenantAccessor.getUserTransactionService();
                break;
            default:
                throw new InvalidSessionException("Unknown session type: " + session.getClass().getName());
        }
        return transactionService;
    }

    protected Object invokeAPI(final Object[] parametersValues, final Object apiImpl, final Method method) throws Throwable {
        try {
            return method.invoke(apiImpl, parametersValues);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    protected Class<?>[] getParameterTypes(final List<String> classNameParameters) throws ClassNotFoundException {
        Class<?>[] parameterTypes = null;
        if (classNameParameters != null && !classNameParameters.isEmpty()) {
            parameterTypes = new Class<?>[classNameParameters.size()];
            for (int i = 0; i < parameterTypes.length; i++) {
                final String className = classNameParameters.get(i);
                Class<?> classType = null;
                if ("int".equals(className)) {
                    classType = int.class;
                } else if ("long".equals(className)) {
                    classType = long.class;
                } else if ("boolean".equals(className)) {
                    classType = boolean.class;
                } else {
                    classType = Class.forName(className);
                }
                parameterTypes[i] = classType;
            }
        }
        return parameterTypes;
    }

    private void checkTenantSession(final PlatformServiceAccessor platformAccessor, final Session session) throws SSchedulerException {
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
        if (!schedulerService.isStarted() && logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "The scheduler is not started!");
        }
        final APISession apiSession = (APISession) session;
        final TenantServiceAccessor tenantAccessor = platformAccessor.getTenantServiceAccessor(apiSession.getTenantId());
        final LoginService tenantLoginService = tenantAccessor.getLoginService();
        if (!tenantLoginService.isValid(apiSession.getId())) {
            throw new InvalidSessionException("Invalid session");
        }
    }

    private ClassLoader getTenantClassLoader(final PlatformServiceAccessor platformServiceAccessor, final Session session) throws SClassLoaderException {
        final APISession apiSession = (APISession) session;
        final TenantServiceAccessor tenantAccessor = platformServiceAccessor.getTenantServiceAccessor(apiSession.getTenantId());
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        return classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(), apiSession.getTenantId());
    }

    private ClassLoader getPlatformClassLoader(final PlatformServiceAccessor platformServiceAccessor) throws SClassLoaderException {
        ClassLoader classLoader = null;
        final PlatformService platformService = platformServiceAccessor.getPlatformService();
        // get the platform to put it in cache if needed
        if (!platformService.isPlatformCreated()) {
            try {
                platformServiceAccessor.getTransactionService().executeInTransaction(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        platformService.getPlatform();
                        return null;
                    }

                });
            } catch (final Exception e) {
                // do not throw exceptions: it's just in case the platform was not in cache
            }
        }
        if (platformService.isPlatformCreated()) {
            final ClassLoaderService classLoaderService = platformServiceAccessor.getClassLoaderService();
            classLoader = classLoaderService.getGlobalClassLoader();
        }
        return classLoader;
    }

}
