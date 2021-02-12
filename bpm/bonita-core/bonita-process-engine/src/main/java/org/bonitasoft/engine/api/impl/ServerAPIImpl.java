/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.NoSessionRequired;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.classloader.ClassLoaderIdentifier;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ServerAPIImpl implements ServerAPI {

    private static Logger logger = LoggerFactory.getLogger(ServerAPIImpl.class);

    private static final String SESSION = "session";
    private static final long serialVersionUID = -161775388604256321L;

    private final APIAccessResolver accessResolver;

    private final boolean cleanSession;

    public ServerAPIImpl(boolean cleanSession) {
        try {
            this.cleanSession = cleanSession;
            accessResolver = getServiceAccessorFactoryInstance().createAPIAccessResolver();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public ServerAPIImpl() {
        this(true);
    }

    /**
     * For Test Mock usage
     */
    public ServerAPIImpl(APIAccessResolver accessResolver) {
        this.cleanSession = true;
        this.accessResolver = accessResolver;
    }

    ServiceAccessorFactory getServiceAccessorFactoryInstance() {
        return ServiceAccessorFactory.getInstance();
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName,
            final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        logger.trace("Starting Server API call {} {}", apiInterfaceName, methodName);

        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        SessionAccessor sessionAccessor = null;
        Session session = null;
        try {
            final Object api = accessResolver.getAPIImplementation(Class.forName(apiInterfaceName));
            try {
                session = (Session) options.get(SESSION);
                sessionAccessor = beforeInvokeMethod(session, api);
                return invokeAPI(api, apiInterfaceName, methodName, classNameParameters, parametersValues, session);
            } catch (final ServerAPIRuntimeException e) {
                throw e.getCause();
            }
        } catch (final BonitaRuntimeException | BonitaException bre) {
            fillGlobalContextForException(session, bre);
            throw createServerWrappedException(bre);
        } catch (final UndeclaredThrowableException ute) {
            throw createServerWrappedException(ute);
        } catch (final Throwable cause) {
            final BonitaRuntimeException throwableToWrap = wrapThrowable(cause);
            fillGlobalContextForException(session, throwableToWrap);
            throw createServerWrappedException(throwableToWrap);
        } finally {
            cleanSessionIfNeeded(sessionAccessor);
            // reset class loader
            Thread.currentThread().setContextClassLoader(baseClassLoader);
            logger.trace("End Server API call {} {}", apiInterfaceName, methodName);
        }
    }

    protected BonitaRuntimeException wrapThrowable(final Throwable cause) {
        return new BonitaRuntimeException(cause);
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

    private SessionAccessor beforeInvokeMethod(final Session session, final Object api)
            throws BonitaHomeNotSetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, BonitaHomeConfigurationException,
            IOException,
            SBonitaException {
        SessionAccessor sessionAccessor = null;

        final ServiceAccessorFactory serviceAccessorFactory = getServiceAccessorFactoryInstance();
        final PlatformServiceAccessor platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();

        ClassLoader serverClassLoader = null;
        if (session != null) {
            final SessionType sessionType = getSessionType(session);
            sessionAccessor = serviceAccessorFactory.createSessionAccessor();
            switch (sessionType) {
                case PLATFORM:
                    serverClassLoader = beforeInvokeMethodForPlatformSession(sessionAccessor, platformServiceAccessor,
                            session);
                    break;

                case API:
                    serverClassLoader = beforeInvokeMethodForAPISession(sessionAccessor, platformServiceAccessor,
                            session);
                    break;

                default:
                    throw new InvalidSessionException("Unknown session type: " + session.getClass().getName());
            }
        } else if (needSession(api)) {
            throw new InvalidSessionException("Session is null!");
        }
        if (serverClassLoader != null) {
            Thread.currentThread().setContextClassLoader(serverClassLoader);
        }
        return sessionAccessor;
    }

    private boolean needSession(Object api) {
        //require a session if "NoSessionRequired" is not present
        Class<?>[] interfaces = api.getClass().getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (anInterface.isAnnotationPresent(NoSessionRequired.class)) {
                return false;
            }
        }
        return true;
    }

    private ClassLoader beforeInvokeMethodForAPISession(SessionAccessor sessionAccessor,
            PlatformServiceAccessor platformServiceAccessor, Session session) throws SBonitaException {
        checkTenantSession(platformServiceAccessor, session);
        long tenantId = ((APISession) session).getTenantId();
        SessionService sessionService = platformServiceAccessor.getTenantServiceAccessor(tenantId).getSessionService();
        sessionService.renewSession(session.getId());
        sessionAccessor.setSessionInfo(session.getId(), tenantId);
        return getTenantClassLoader(platformServiceAccessor, session);
    }

    private ClassLoader beforeInvokeMethodForPlatformSession(final SessionAccessor sessionAccessor,
            final PlatformServiceAccessor platformServiceAccessor,
            final Session session) throws SSessionException, SClassLoaderException {
        final PlatformSessionService platformSessionService = platformServiceAccessor.getPlatformSessionService();
        final PlatformLoginService loginService = platformServiceAccessor.getPlatformLoginService();

        if (!loginService.isValid(session.getId())) {
            throw new InvalidSessionException("Invalid session");
        }
        platformSessionService.renewSession(session.getId());
        sessionAccessor.setSessionInfo(session.getId(), -1);
        return getPlatformClassLoader(platformServiceAccessor);
    }

    private SessionType getSessionType(final Session session) {
        SessionType sessionType = null;
        if (session instanceof PlatformSession) {
            sessionType = SessionType.PLATFORM;
        } else if (session instanceof APISession) {
            sessionType = SessionType.API;
        }
        return sessionType;
    }

    private Object invokeAPI(Object api, String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues,
            final Session session) throws Throwable {
        final Class<?>[] parameterTypes = getParameterTypes(classNameParameters);

        final Method method = ClassReflector.getMethod(api.getClass(), methodName, parameterTypes);
        // No session required means that there is no transaction
        if (method.isAnnotationPresent(CustomTransactions.class)
                || Class.forName(apiInterfaceName).isAnnotationPresent(NoSessionRequired.class)) {
            return invokeAPIOutsideTransaction(parametersValues, api, method, apiInterfaceName, session);
        } else {
            return invokeAPIInTransaction(parametersValues, api, method, session, apiInterfaceName);
        }
    }

    private Object invokeAPIOutsideTransaction(Object[] parametersValues, Object apiImpl, Method method,
            String apiInterfaceName, Session session)
            throws Throwable {
        checkMethodAccessibility(apiImpl, apiInterfaceName, method, session, /* Not in transaction */false);
        return invokeAPI(method, apiImpl, parametersValues);
    }

    protected void checkMethodAccessibility(final Object apiImpl, final String apiInterfaceName, final Method method,
            final Session session,
            boolean isAlreadyInTransaction) {
        final MethodAvailability methodAvailability = getMethodAvailability(apiImpl, method);
        if (methodAvailability.isDeprecated) {
            logger.warn("The API method {}.{} is deprecated. It will be deleted in a future release. " +
                    "Please plan to update your code to use the replacement method instead. Check the Javadoc for more details.",
                    apiInterfaceName, method.getName());
        }
        if (!methodAvailability.isAvailableWhenPlatformIsStopped && !isNodeStarted()) {
            logger.error(
                    "Node not started. Method '{}. {}' cannot be called until node has been started (PlatformAPI.startNode()). Exact class: {}",
                    apiInterfaceName, method.getName(), method.getDeclaringClass().getName());
            throw new NodeNotStartedException();
        }
        // we don't check if tenant is in pause mode at platform level and when there is no session
        // when there is no session means that we are trying to log in, in this case it is the LoginApiExt that check if the user is the technical user
        // For tenant level method call:
        if (!(session instanceof APISession)) {
            return;
        }

        final long tenantId = ((APISession) session).getTenantId();
        if (methodAvailability.isAvailableOnPausedTenant && methodAvailability.isAvailableOnRunningTenant) {
            //method can be called on paused and running tenant.
            return;
        }
        boolean tenantIsPaused = isTenantPaused(tenantId, session, isAlreadyInTransaction);
        if (tenantIsPaused && !methodAvailability.isAvailableOnPausedTenant) {
            throw new TenantStatusException(
                    MessageFormat.format("Unable to call API method {0}.{1}, The tenant {2} is paused.",
                            apiInterfaceName, method.getName(), tenantId));
        }
        if (!tenantIsPaused && !methodAvailability.isAvailableOnRunningTenant) {
            throw new TenantStatusException(MessageFormat.format(
                    "Unable to call API method {0}.{1}, The tenant {2} is running and this method can only be called when tenant is paused.",
                    apiInterfaceName, method.getName(), tenantId));
        }
    }

    private static class MethodAvailability {

        boolean isDeprecated;
        boolean isAvailableOnRunningTenant = true;
        boolean isAvailableOnPausedTenant = true;
        boolean isAvailableWhenPlatformIsStopped;
    }

    private MethodAvailability getMethodAvailability(Object apiInstance, Method method) {
        AvailableWhenTenantIsPaused availableWhenTenantIsPaused = Optional
                .ofNullable(method.getAnnotation(AvailableWhenTenantIsPaused.class))
                .orElseGet(() -> apiInstance.getClass().getAnnotation(AvailableWhenTenantIsPaused.class));
        AvailableOnStoppedNode availableOnStoppedNode = method.getAnnotation(AvailableOnStoppedNode.class);
        MethodAvailability methodAvailability = new MethodAvailability();
        // Deprecation
        methodAvailability.isDeprecated = method.isAnnotationPresent(Deprecated.class);

        // Tenant status
        if (availableWhenTenantIsPaused == null) {
            methodAvailability.isAvailableOnPausedTenant = false;
        } else if (availableWhenTenantIsPaused.onlyAvailableWhenPaused()) {
            methodAvailability.isAvailableOnRunningTenant = false;
        }
        // Platform status
        if (availableOnStoppedNode != null) {
            methodAvailability.isAvailableWhenPlatformIsStopped = true;
        }
        return methodAvailability;
    }

    /**
     * @param tenantId
     *        the ID of the tenant to check
     * @param session
     *        the session to user
     * @param isAlreadyInTransaction
     *        if the request is made in a transaction
     * @return true if the tenant is available, false otherwise (if the tenant is paused)
     */
    protected boolean isTenantPaused(final long tenantId, final Session session, boolean isAlreadyInTransaction) {
        try {
            TenantAdministrationAPI tenantAdministrationAPI = accessResolver
                    .getAPIImplementation(TenantAdministrationAPI.class);
            if (isAlreadyInTransaction) {
                return tenantAdministrationAPI.isPaused();
            } else {
                return selectUserTransactionService(session, getSessionType(session))
                        .executeInTransaction(tenantAdministrationAPI::isPaused);
            }
        } catch (final Throwable e) {
            throw new BonitaRuntimeException("Cannot determine if the tenant with ID " + tenantId + " is accessible",
                    e);
        }
    }

    /**
     * @return true if the node is started, false otherwise.
     */
    private boolean isNodeStarted() {
        try {
            return accessResolver.getAPIImplementation(PlatformAPI.class).isNodeStarted();
        } catch (final Throwable e) {
            return false;
        }
    }

    protected Object invokeAPIInTransaction(final Object[] parametersValues, final Object apiImpl, final Method method,
            final Session session,
            final String apiInterfaceName) throws Throwable {
        if (session == null) {
            throw new BonitaRuntimeException("session is null");
        }
        final UserTransactionService userTransactionService = selectUserTransactionService(session,
                getSessionType(session));

        return userTransactionService.executeInTransaction(() -> {
            try {
                checkMethodAccessibility(apiImpl, apiInterfaceName, method, session,
                        /* Already in a transaction */true);
                return invokeAPI(method, apiImpl, parametersValues);
            } catch (final Throwable cause) {
                throw new ServerAPIRuntimeException(cause);
            }
        });
    }

    UserTransactionService selectUserTransactionService(final Session session, final SessionType sessionType)
            throws BonitaHomeNotSetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, IOException,
            BonitaHomeConfigurationException {
        UserTransactionService transactionService;
        final ServiceAccessorFactory serviceAccessorFactory = getServiceAccessorFactoryInstance();
        final PlatformServiceAccessor platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
        switch (sessionType) {
            case PLATFORM:
                transactionService = platformServiceAccessor.getTransactionService();
                break;
            case API:
                final TenantServiceAccessor tenantAccessor = platformServiceAccessor
                        .getTenantServiceAccessor(((APISession) session).getTenantId());
                transactionService = tenantAccessor.getUserTransactionService();
                break;
            default:
                throw new InvalidSessionException("Unknown session type: " + session.getClass().getName());
        }
        return transactionService;
    }

    protected Object invokeAPI(final Method method, final Object apiImpl, final Object... parametersValues)
            throws Throwable {
        try {
            return method.invoke(apiImpl, parametersValues);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Class<?>[] getParameterTypes(final List<String> classNameParameters) throws ClassNotFoundException {
        Class<?>[] parameterTypes = null;
        if (classNameParameters != null && !classNameParameters.isEmpty()) {
            parameterTypes = new Class<?>[classNameParameters.size()];
            for (int i = 0; i < parameterTypes.length; i++) {
                final String className = classNameParameters.get(i);
                Class<?> classType;
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

    private void checkTenantSession(final PlatformServiceAccessor platformAccessor, final Session session)
            throws SSchedulerException {
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        if (!schedulerService.isStarted()) {
            logger.debug("The scheduler is not started!");
        }
        final APISession apiSession = (APISession) session;
        final TenantServiceAccessor tenantAccessor = platformAccessor
                .getTenantServiceAccessor(apiSession.getTenantId());
        final LoginService tenantLoginService = tenantAccessor.getLoginService();
        if (!tenantLoginService.isValid(apiSession.getId())) {
            throw new InvalidSessionException("Invalid session");
        }
    }

    private ClassLoader getTenantClassLoader(final PlatformServiceAccessor platformServiceAccessor,
            final Session session) throws SClassLoaderException {
        final APISession apiSession = (APISession) session;
        final TenantServiceAccessor tenantAccessor = platformServiceAccessor
                .getTenantServiceAccessor(apiSession.getTenantId());
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        return classLoaderService.getClassLoader(identifier(ScopeType.TENANT, apiSession.getTenantId()));
    }

    private ClassLoader getPlatformClassLoader(final PlatformServiceAccessor platformServiceAccessor)
            throws SClassLoaderException {
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
            } catch (final Exception ignored) {
                // do not throw exceptions: it's just in case the platform was not in cache
            }
        }
        if (platformService.isPlatformCreated()) {
            final ClassLoaderService classLoaderService = platformServiceAccessor.getClassLoaderService();
            classLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        }
        return classLoader;
    }

    protected enum SessionType {
        PLATFORM, API
    }

}
