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

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.ServicesResolver;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.transaction.TransactionService;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 */
public class SpringPlatformServiceAccessor implements PlatformServiceAccessor {

    private final SpringBeanAccessor beanAccessor;

    public SpringPlatformServiceAccessor(SpringBeanAccessor beanAccessor) {
        this.beanAccessor = beanAccessor;
    }

    @Override
    public TransactionService getTransactionService() {
        return beanAccessor.getService(TransactionService.class);
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        return beanAccessor.getService("platformTechnicalLoggerService", TechnicalLoggerService.class);
    }

    @Override
    public PlatformLoginService getPlatformLoginService() {
        return beanAccessor.getService(PlatformLoginService.class);
    }

    @Override
    public SchedulerService getSchedulerService() {
        return beanAccessor.getService(SchedulerService.class);
    }

    @Override
    public PlatformService getPlatformService() {
        return beanAccessor.getService(PlatformService.class);
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        return beanAccessor.getService("classLoaderService", ClassLoaderService.class);
    }

    @Override
    public DependencyService getDependencyService() {
        return beanAccessor.getService("platformDependencyService", DependencyService.class);
    }

    @Override
    public PlatformCommandService getPlatformCommandService() {
        return beanAccessor.getService("platformCommandService", PlatformCommandService.class);
    }

    @Override
    public PlatformSessionService getPlatformSessionService() {
        return beanAccessor.getService(PlatformSessionService.class);
    }

    @Override
    public NodeConfiguration getPlatformConfiguration() {
        return beanAccessor.getService(NodeConfiguration.class);
    }

    @Override
    public PlatformCacheService getPlatformCacheService() {
        return beanAccessor.getService(PlatformCacheService.class);

    }

    @Override
    public void destroy() {
        beanAccessor.destroy();
    }

    @Override
    public BroadcastService getBroadcastService() {
        return beanAccessor.getService(BroadcastService.class);
    }

    @Override
    public PlatformAuthenticationService getPlatformAuthenticationService() {
        return beanAccessor.getService(PlatformAuthenticationService.class);
    }

    @Override
    public <T> T lookup(String serviceName) throws NotFoundException {
        try{
            return beanAccessor.getService(serviceName);
        }catch (NoSuchBeanDefinitionException e) {
            throw new NotFoundException(e);
        }

    }

    @Override
    public ServicesResolver getServicesResolver() {
        return beanAccessor.getService(ServicesResolver.class);
    }
}
