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
package org.bonitasoft.engine.authorization;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import groovy.lang.GroovyClassLoader;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.api.permission.PermissionRule;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.impl.ServerLoggerWrapper;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * Permission service implementation
 *
 * @author Baptiste Mesta
 */
@Component
@ConditionalOnSingleCandidate(PermissionService.class)
public class PermissionServiceImpl implements PermissionService {

    private final ClassLoaderService classLoaderService;
    private final TechnicalLoggerService logger;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final long tenantId;
    private GroovyClassLoader groovyClassLoader;

    public PermissionServiceImpl(final ClassLoaderService classLoaderService, final TechnicalLoggerService logger,
            final SessionAccessor sessionAccessor, final SessionService sessionService,
            @Value("${tenantId}") final long tenantId) {
        this.classLoaderService = classLoaderService;
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.tenantId = tenantId;
    }

    @Override
    public boolean checkAPICallWithScript(final String className, final APICallContext context, final boolean reload)
            throws SExecutionException, ClassNotFoundException {
        checkStarted();
        //groovy class loader load class from files and cache then when loaded, no need to do some lazy loading or load all class on start
        Class<?> aClass;
        if (reload) {
            reload();
            aClass = groovyClassLoader.loadClass(className, true, true, true);
        } else {
            aClass = Class.forName(className, true, groovyClassLoader);
        }
        if (!PermissionRule.class.isAssignableFrom(aClass)) {
            throw new SExecutionException("The class " + aClass.getName()
                    + " does not implements org.bonitasoft.engine.api.permission.PermissionRule");
        }
        try {
            SSession session = getSession();
            final APISession apiSession = ModelConvertor.toAPISession(session, null);
            final PermissionRule permissionRule = (PermissionRule) aClass.newInstance();
            return permissionRule.isAllowed(apiSession, context, createAPIAccessorImpl(),
                    new ServerLoggerWrapper(permissionRule.getClass(), logger));
        } catch (final Throwable e) {
            throw new SExecutionException("The permission rule " + aClass.getName() + " threw an exception", e);
        }
    }

    public SSession getSession() throws SSessionNotFoundException, SessionIdNotSetException {
        return sessionService.getSession(sessionAccessor.getSessionId());
    }

    private void reload() throws SExecutionException {
        stop();
        try {
            start();
        } catch (SBonitaException e) {
            throw new SExecutionException("The permission rule service could not be reloaded", e);
        }
    }

    protected APIAccessorImpl createAPIAccessorImpl() {
        return new APIAccessorImpl();
    }

    private void checkStarted() throws SExecutionException {
        if (groovyClassLoader == null) {
            throw new SExecutionException("The permission rule service is not started");
        }
    }

    @Override
    public void start() throws SBonitaException {
        groovyClassLoader = new GroovyClassLoader(
                classLoaderService.getClassLoader(identifier(ScopeType.TENANT, tenantId)));
        groovyClassLoader.setShouldRecompile(true);
        try {
            final File folder = getBonitaHomeServer().getSecurityScriptsFolder(tenantId);
            groovyClassLoader.addClasspath(folder.getAbsolutePath());
        } catch (BonitaHomeNotSetException | IOException e) {
            throw new SExecutionException(e);
        }
    }

    BonitaHomeServer getBonitaHomeServer() {
        return BonitaHomeServer.getInstance();
    }

    @Override
    public void stop() {
        if (groovyClassLoader != null) {
            groovyClassLoader.clearCache();
            groovyClassLoader = null;
        }
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() throws SBonitaException {
        start();
    }

    @Override
    public boolean isAuthorized(APICallContext apiCallContext, boolean reload, Set<String> userPermissions,
            Set<String> resourceDynamicPermissions) throws SExecutionException {
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Let it go from Community");
        //FIXME implement static permission check
        return true;
    }
}
