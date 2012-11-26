/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LogAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.MigrationAPI;
import org.bonitasoft.engine.api.MonitoringAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.PlatformMonitoringAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.transaction.GetCurrentTenantClassLoader;
import org.bonitasoft.engine.api.impl.transaction.GetPlatformClassLoader;
import org.bonitasoft.engine.api.impl.transaction.IsPlatformCreated;
import org.bonitasoft.engine.api.impl.transaction.RenewSSession;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.impl.SessionAccessorNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.session.SessionService;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class ServerAPIImpl implements ServerAPI {

    private static final long serialVersionUID = -241989293071026657L;

    private static final List<String> NO_SESSION_APIS = Arrays.asList(PlatformLoginAPI.class.getName(), LoginAPI.class.getName());

    private final Map<String, Object> apis = new HashMap<String, Object>(12);

    public ServerAPIImpl() {
        initMap();
    }

    @Override
    public Object invokeMethod(final Map<String, Serializable> options, final String apiInterfaceName, final String methodName,
            final List<String> classNameParameters, final Object[] parametersValues) throws ServerWrappedException {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Session session = (Session) options.get("session");
            final ClassLoader serverClassLoader = checkSession(apiInterfaceName, session);
            if (serverClassLoader != null) {
                Thread.currentThread().setContextClassLoader(serverClassLoader);
            }
            return invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues);
        } finally {
            Thread.currentThread().setContextClassLoader(baseClassLoader);
        }
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
            final Object api = apis.get(apiInterfaceName);
            if (api == null) {
                throw new APIImplementationNotFoundException("No API implementation was found for: " + apiInterfaceName);
            }
            final Method method = api.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(api, parametersValues);
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

    private ClassLoader checkSession(final String apiInterfaceName, final Session session) throws ServerWrappedException {
        ClassLoader classLoader = null;
        if (session != null) {
            if (session instanceof PlatformSession) {
                classLoader = checkPlatformSession(session);
            } else if (session instanceof APISession) {
                classLoader = checkTenantSession(session);
            } else {
                throw new ServerWrappedException(new InvalidSessionException("Unknown session type: " + session.getClass().getName()));
            }
        } else {
            if (!NO_SESSION_APIS.contains(apiInterfaceName)) {
                throw new ServerWrappedException(new InvalidSessionException("Session is null!"));
            }
        }
        return classLoader;
    }

    protected ClassLoader checkTenantSession(final Session session) throws ServerWrappedException {
        final APISession apiSession = (APISession) session;
        try {
            final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            final PlatformServiceAccessor platformServiceAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
            final TenantServiceAccessor tenantAccessor = platformServiceAccessor.getTenantServiceAccessor(apiSession.getTenantId());
            final LoginService loginService = tenantAccessor.getLoginService();
            if (!loginService.isValid(apiSession.getId())) {
                throw new ServerWrappedException(new InvalidSessionException("Invalid session"));
            }
            final SessionService sessionService = platformServiceAccessor.getSessionService();
            final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
            final RenewSSession renewSSession = new RenewSSession(sessionService, session.getId());
            transactionExecutor.execute(renewSSession);
            serviceAccessorFactory.createSessionAccessor().setSessionInfo(apiSession.getId(), apiSession.getTenantId());
            final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
            final GetCurrentTenantClassLoader getCurrentTenantClassLoader = new GetCurrentTenantClassLoader(classLoaderService, apiSession.getTenantId());
            transactionExecutor.execute(getCurrentTenantClassLoader);
            return getCurrentTenantClassLoader.getResult();
        } catch (final ServerWrappedException swe) {
            throw swe;
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        }
    }

    protected ClassLoader checkPlatformSession(final Session session) throws ServerWrappedException {
        ClassLoader classLoader = null;
        try {
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformLoginService loginService = platformServiceAccessor.getPlatformLoginService();
            final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            if (!loginService.isValid(session.getId())) {
                throw new ServerWrappedException(new InvalidSessionException("Invalid session"));
            }
            final TransactionExecutor transactionExecutor = platformServiceAccessor.getTransactionExecutor();
            final PlatformSessionService platformSessionService = platformServiceAccessor.getPlatformSessionService();
            final RenewSSession renewSSession = new RenewSSession(platformSessionService, session.getId());
            transactionExecutor.execute(renewSSession);
            serviceAccessorFactory.createSessionAccessor().setSessionInfo(session.getId(), -1);
            final PlatformService platformService = platformServiceAccessor.getPlatformService();
            final IsPlatformCreated isPlatformCreated = new IsPlatformCreated(platformService);
            transactionExecutor.execute(isPlatformCreated);
            if (isPlatformCreated.getResult()) {
                final ClassLoaderService classLoaderService = platformServiceAccessor.getClassLoaderService();
                final GetPlatformClassLoader getPlatformClassLoader = new GetPlatformClassLoader(classLoaderService);
                transactionExecutor.execute(getPlatformClassLoader);
                classLoader = getPlatformClassLoader.getResult();
            }
        } catch (final ServerWrappedException swe) {
            throw swe;
        } catch (final Exception e) {
            throw new ServerWrappedException(e);
        }
        return classLoader;
    }

    private void initMap() {
        apis.put(PlatformAPI.class.getName(), new PlatformAPIImpl());
        apis.put(PlatformLoginAPI.class.getName(), new PlatformLoginAPIImpl());
        apis.put(PlatformMonitoringAPI.class.getName(), new PlatformMonitoringAPIImpl());
        apis.put(LoginAPI.class.getName(), new LoginAPIImpl());
        apis.put(IdentityAPI.class.getName(), new IdentityAPIImpl());
        apis.put(MonitoringAPI.class.getName(), new MonitoringAPIImpl());
        apis.put(ProcessAPI.class.getName(), new ProcessAPIImpl());
        apis.put(MigrationAPI.class.getName(), new MigrationAPIImpl());
        apis.put(LogAPI.class.getName(), new LogAPIImpl());
        apis.put(CommandAPI.class.getName(), new CommandAPIImpl());
        apis.put(PlatformCommandAPI.class.getName(), new PlatformCommandAPIImpl());
    }
}
