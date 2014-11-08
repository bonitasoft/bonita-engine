/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.service.impl;

import java.io.File;

import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.api.permission.PermissionRule;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import groovy.lang.GroovyClassLoader;

/**
 *
 * Permission service implementation
 *
 * @author Baptiste Mesta
 */
public class PermissionServiceImpl implements PermissionService {

    private final ClassLoaderService classLoaderService;
    private final TechnicalLoggerService logger;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final String scriptFolder;
    private long tenantId;
    private GroovyClassLoader groovyClassLoader;

    public PermissionServiceImpl(ClassLoaderService classLoaderService, TechnicalLoggerService logger,
            SessionAccessor sessionAccessor, SessionService sessionService, String scriptFolder, long tenantId) {
        this.classLoaderService = classLoaderService;
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.scriptFolder = scriptFolder;
        this.tenantId = tenantId;
    }

    @Override
    public boolean checkAPICallWithScript(String className, APICallContext context, boolean reload) throws SExecutionException, ClassNotFoundException {
        checkStarted();
        SSession session;
        //groovy class loader load class from files and cache then when loaded, no need to do some lazy loading or load all class on start
        Class aClass;
        if(reload){
            groovyClassLoader.clearCache();
            aClass = groovyClassLoader.loadClass(className,true,true,true);
        }else{
            aClass = Class.forName(className,true,groovyClassLoader);
        }
        if(!PermissionRule.class.isAssignableFrom(aClass)){
            throw new SExecutionException("The class "+aClass.getName()+" does not implements org.bonitasoft.engine.api.permission.PermissionRule");
        }
        try {
            session = sessionService.getSession(sessionAccessor.getSessionId());
            APISession apiSession = ModelConvertor.toAPISession(session, null);
            PermissionRule permissionRule = (PermissionRule) aClass.newInstance();
            return permissionRule.check(apiSession, context, new APIAccessorImpl(), new ServerLoggerWrapper(permissionRule.getClass(), logger));
        } catch (Throwable e) {
            throw new SExecutionException("The permission rule thrown an exception",e);
        }
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

        File file = new File(scriptFolder);
        if (file.exists() && file.isDirectory()) {
            groovyClassLoader.addClasspath(file.getAbsolutePath());
        } else {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The security script folder " + file
                    + " does not exists or is a file, PermissionRules will be loaded only from the tenant classloader");
        }
    }

    @Override
    public void stop() throws SBonitaException {
        if(groovyClassLoader != null){
            groovyClassLoader.clearCache();
            groovyClassLoader = null;
        }
    }

    @Override
    public void pause() throws SBonitaException {
        stop();
    }

    @Override
    public void resume() throws SBonitaException {
        start();
    }
}


