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

import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.api.permission.PermissionRule;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import groovy.lang.GroovyClassLoader;

/**
 * @author Baptiste Mesta
 */
public class PermissionServiceImpl implements PermissionService {

    private final CacheService cacheService;
    private final ClassLoaderService classLoaderService;
    private final TechnicalLoggerService logger;
    private SessionAccessor sessionAccessor;
    private SessionService sessionService;

    public PermissionServiceImpl(CacheService cacheService, ClassLoaderService classLoaderService, TechnicalLoggerService logger,
            SessionAccessor sessionAccessor, SessionService sessionService) {
        this.cacheService = cacheService;
        this.classLoaderService = classLoaderService;
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public boolean checkAPICallWithScript(String scriptName, APICallContext context) throws SExecutionException {
        SSession session;
        try {
            session = sessionService.getSession(sessionAccessor.getSessionId());

            APISession apiSession = ModelConvertor.toAPISession(session, null);

            ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(), session.getTenantId());
            GroovyClassLoader groovyClassLoader = new GroovyClassLoader(localClassLoader);
            Class aClass = groovyClassLoader.parseClass(scriptName);

            PermissionRule permissionRule = (PermissionRule) aClass.newInstance();




            return permissionRule.check(apiSession,context,new APIAccessorImpl(), new ServerLoggerWrapper(permissionRule.getClass(),logger));
        } catch (Throwable e) {
            throw new SExecutionException(e);
        }
    }
}
