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
package org.bonitasoft.engine.service.impl;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import groovy.lang.GroovyResourceLoader;
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
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Permission service implementation
 *
 * @author Baptiste Mesta
 */
public class PermissionServiceImpl implements PermissionService {

    private final ClassLoaderService classLoaderService;
    private final TechnicalLoggerService logger;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final long tenantId;
    private GroovyClassLoader groovyClassLoader;

    public PermissionServiceImpl(final ClassLoaderService classLoaderService, final TechnicalLoggerService logger,
            final SessionAccessor sessionAccessor, final SessionService sessionService, final long tenantId) {
        this.classLoaderService = classLoaderService;
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.tenantId = tenantId;
    }

    @Override
    public boolean checkAPICallWithScript(final String className, final APICallContext context, final boolean reload) throws SExecutionException,
            ClassNotFoundException {
        checkStarted();
        SSession session;
        //groovy class loader load class from files and cache then when loaded, no need to do some lazy loading or load all class on start
        Class<?> aClass;
        if (reload) {
            groovyClassLoader.clearCache();
            aClass = groovyClassLoader.loadClass(className, true, true, true);
        } else {
            aClass = Class.forName(className, true, groovyClassLoader);
        }
        if (!PermissionRule.class.isAssignableFrom(aClass)) {
            throw new SExecutionException("The class " + aClass.getName() + " does not implements org.bonitasoft.engine.api.permission.PermissionRule");
        }
        try {
            session = sessionService.getSession(sessionAccessor.getSessionId());
            final APISession apiSession = ModelConvertor.toAPISession(session, null);
            final PermissionRule permissionRule = (PermissionRule) aClass.newInstance();
            return permissionRule.isAllowed(apiSession, context, createAPIAccessorImpl(), new ServerLoggerWrapper(permissionRule.getClass(), logger));
        } catch (final Throwable e) {
            throw new SExecutionException("The permission rule thrown an exception", e);
        }
    }

    protected APIAccessorImpl createAPIAccessorImpl() {
        return new APIAccessorImpl();
    }

    private void checkStarted() throws SExecutionException {
        if (groovyClassLoader == null) {
            throw new SExecutionException("The service is not started");
        }
    }

    @Override
    public void start() throws SBonitaException {
        groovyClassLoader = new GroovyClassLoader(classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(), tenantId));
        groovyClassLoader.setShouldRecompile(true);
        try {
            final File folder = BonitaHomeServer.getInstance().getSecurityScriptsFolder(tenantId);
            groovyClassLoader.addClasspath(folder.getAbsolutePath());
        } catch (BonitaHomeNotSetException e) {
            throw new SExecutionException(e);
        } catch (IOException e) {
            throw new SExecutionException(e);
        }
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
}
