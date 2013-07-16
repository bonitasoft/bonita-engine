/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.impl.SessionAccessorNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ServerAPIImpl implements ServerAPI {

    private static final long serialVersionUID = -161775388604256321L;

    private final APIAccessResolver accessResolver;

    private TechnicalLoggerService technicalLogger;

    private enum SessionType {
        PLATFORM, API;
    }

    public ServerAPIImpl() {
        try {
            accessResolver = ServiceAccessorFactory.getInstance().createAPIAccessResolver();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private void setTechnicalLogger(final TechnicalLoggerService technicalLoggerService) {
        technicalLogger = technicalLoggerService;
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();

        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = beforeInvokeMethod(options, apiInterfaceName);
            return invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues);
        } catch (final UndeclaredThrowableException e) {
            if (technicalLogger != null && technicalLogger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                technicalLogger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
            }
            throw new ServerWrappedException(e);
        } catch (final ServerWrappedException e) {
            throw e;
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        } finally {
            // clean session id
            if (sessionAccessor != null) {
                sessionAccessor.deleteSessionId();
            }
            // reset class loader
            Thread.currentThread().setContextClassLoader(baseClassLoader);
        }
    }

    private SessionAccessor beforeInvokeMethod(final Map<String, Serializable> options, final String apiInterfaceName) throws ServerWrappedException {
        // boolean hasSetSessionAccessor = false;
        TransactionService txService = null;

        try {
            final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            final PlatformServiceAccessor platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
            txService = platformServiceAccessor.getTransactionService();
            txService.begin();

            final SessionAccessor sessionAccessor = serviceAccessorFactory.createSessionAccessor();
            final Session session = getSession(options);
            ClassLoader serverClassLoader = null;
            if (session != null) {
                final long sessionId = session.getId();
                switch (getSessionType(session)) {
                    case PLATFORM:
                        final PlatformSessionService platformSessionService = platformServiceAccessor.getPlatformSessionService();
                        final PlatformLoginService loginService = platformServiceAccessor.getPlatformLoginService();

                        if (!loginService.isValid(sessionId)) {
                            throw new ServerWrappedException(new InvalidSessionException("Invalid session"));
                        }
                        platformSessionService.renewSession(sessionId);
                        sessionAccessor.setSessionInfo(sessionId, -1);
                        serverClassLoader = getPlatformClassLoader(platformServiceAccessor);
                        setTechnicalLogger(platformServiceAccessor.getTechnicalLoggerService());
                        break;
                    case API:
                        final SessionService sessionService = platformServiceAccessor.getSessionService();
                        checkTenantSession(platformServiceAccessor, session);
                        sessionService.renewSession(sessionId);
                        sessionAccessor.setSessionInfo(sessionId, ((APISession) session).getTenantId());
                        serverClassLoader = getTenantClassLoader(platformServiceAccessor, session);
                        setTechnicalLogger(serviceAccessorFactory.createTenantServiceAccessor(((APISession) session).getTenantId()).getTechnicalLoggerService());
                        break;
                    default:
                        throw new ServerWrappedException(new InvalidSessionException("Unknown session type: " + session.getClass().getName()));
                }
            } else if (accessResolver.needSession(apiInterfaceName)) {
                throw new ServerWrappedException(new InvalidSessionException("Session is null!"));
            }
            if (serverClassLoader != null) {
                Thread.currentThread().setContextClassLoader(serverClassLoader);
            }
            return sessionAccessor;
        } catch (final ServerWrappedException swe) {
            throw swe;
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        } finally {
            try {
                if (txService != null && txService.isTransactionActive()) {
                    txService.complete();
                }
            } catch (final STransactionException e) {
                throw new ServerWrappedException(e);
            }
        }
    }

    private Session getSession(final Map<String, Serializable> options) {
        return (Session) options.get("session");
    }

    private SessionType getSessionType(final Session session) throws ServerWrappedException {
        SessionType sessionType = null;
        if (session instanceof PlatformSession) {
            sessionType = SessionType.PLATFORM;
        } else if (session instanceof APISession) {
            sessionType = SessionType.API;
        }
        return sessionType;
    }

    private Object invokeAPI(final String apiInterfaceName, final String methodName, final List<String> classNameParameters, final Object[] parametersValues)
            throws ServerWrappedException {
        try {
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
            final Object api = accessResolver.getAPIImplementation(apiInterfaceName);
            return ClassReflector.invokeMethod(api, methodName, parameterTypes, parametersValues);
        } catch (final InvocationTargetException ite) {
            throw new ServerWrappedException(ite.getCause());
        } catch (final IllegalArgumentException iae) {
            throw new BonitaRuntimeException(iae);
        } catch (final IllegalAccessException iae) {
            throw new BonitaRuntimeException(iae);
        } catch (final SecurityException se) {
            throw new BonitaRuntimeException(se);
        } catch (final NoSuchMethodException nsme) {
            throw new BonitaRuntimeException(nsme);
        } catch (final ClassNotFoundException cnfe) {
            throw new BonitaRuntimeException(cnfe);
        } catch (final SessionAccessorNotFoundException sanfe) {
            throw new BonitaRuntimeException(sanfe);
        } catch (final APIImplementationNotFoundException apiinfe) {
            throw new ServerWrappedException(apiinfe);
        }
    }

    private void checkTenantSession(final PlatformServiceAccessor platformServiceAccessor, final Session session) throws ServerWrappedException {
        try {
            final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
            final TechnicalLoggerService logger = platformServiceAccessor.getTechnicalLoggerService();
            if (!schedulerService.isStarted() && logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "The scheduler is not started!");
            }
            final APISession apiSession = (APISession) session;
            final STenant tenant = platformServiceAccessor.getPlatformService().getTenant(apiSession.getTenantId());
            if (!PlatformService.ACTIVATED.equals(tenant.getStatus())) {
                throw new ServerWrappedException(new InvalidSessionException("The tenantd is not activated"));
            }
            final TenantServiceAccessor tenantAccessor = platformServiceAccessor.getTenantServiceAccessor(apiSession.getTenantId());
            final LoginService tenantLoginService = tenantAccessor.getLoginService();
            if (!tenantLoginService.isValid(apiSession.getId())) {
                throw new ServerWrappedException(new InvalidSessionException("Invalid session"));
            }
        } catch (final ServerWrappedException swe) {
            throw swe;
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        }
    }

    private ClassLoader getTenantClassLoader(final PlatformServiceAccessor platformServiceAccessor, final Session session) throws ServerWrappedException {
        final APISession apiSession = (APISession) session;
        try {
            final TenantServiceAccessor tenantAccessor = platformServiceAccessor.getTenantServiceAccessor(apiSession.getTenantId());
            final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
            return classLoaderService.getLocalClassLoader("tenant", apiSession.getTenantId());
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        }
    }

    private ClassLoader getPlatformClassLoader(final PlatformServiceAccessor platformServiceAccessor) throws ServerWrappedException {
        ClassLoader classLoader = null;
        try {
            final PlatformService platformService = platformServiceAccessor.getPlatformService();
            if (platformService.isPlatformCreated()) {
                final ClassLoaderService classLoaderService = platformServiceAccessor.getClassLoaderService();
                classLoader = classLoaderService.getGlobalClassLoader();
            }
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        }
        return classLoader;
    }

}
