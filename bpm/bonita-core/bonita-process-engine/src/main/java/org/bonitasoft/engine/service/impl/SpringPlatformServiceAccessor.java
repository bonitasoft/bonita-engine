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
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 */
public class SpringPlatformServiceAccessor implements PlatformServiceAccessor {

    private final SpringPlatformFileSystemBeanAccessor beanAccessor;

    private PlatformService platformService;

    private PlatformLoginService platformLoginService;

    private SchedulerService schedulerService;

    private TechnicalLoggerService technicalLoggerService;

    private TransactionService transactionService;

    private TransactionExecutor transactionExecutor;

    private ClassLoaderService classLoaderService;

    private DependencyService dependencyService;

    private PlatformCommandService platformCommandService;

    private PlatformSessionService platformSessionService;

    private NodeConfiguration platformConfguration;

    private PlatformCacheService platformCacheService;
    private BroadcastService broadcastService;
    private PlatformAuthenticationService platformAuthenticationService;

    public SpringPlatformServiceAccessor() {
        beanAccessor = SpringFileSystemBeanAccessorFactory.getPlatformAccessor();
    }
    
    @Override
    public TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = beanAccessor.getService(TransactionService.class);
        }
        return transactionService;
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        if (technicalLoggerService == null) {
            technicalLoggerService = beanAccessor.getService("platformTechnicalLoggerService", TechnicalLoggerService.class);
        }
        return technicalLoggerService;
    }

    @Override
    public PlatformLoginService getPlatformLoginService() {
        if (platformLoginService == null) {
            platformLoginService = beanAccessor.getService(PlatformLoginService.class);
        }
        return platformLoginService;
    }

    @Override
    public SchedulerService getSchedulerService() {
        if (schedulerService == null) {
            schedulerService = beanAccessor.getService(SchedulerService.class);
        }
        return schedulerService;
    }

    @Override
    public PlatformService getPlatformService() {
        if (platformService == null) {
            platformService = beanAccessor.getService(PlatformService.class);
        }
        return platformService;
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(final long tenantId) {
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @Override
    public TransactionExecutor getTransactionExecutor() {
        if (transactionExecutor == null) {
            transactionExecutor = beanAccessor.getService(TransactionExecutor.class);
        }
        return transactionExecutor;
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        if (classLoaderService == null) {
            classLoaderService = beanAccessor.getService("classLoaderService", ClassLoaderService.class);
        }
        return classLoaderService;
    }

    @Override
    public DependencyService getDependencyService() {
        if (dependencyService == null) {
            dependencyService = beanAccessor.getService("platformDependencyService", DependencyService.class);
        }
        return dependencyService;
    }

    @Override
    public PlatformCommandService getPlatformCommandService() {
        if (platformCommandService == null) {
            platformCommandService = beanAccessor.getService("platformCommandService", PlatformCommandService.class);
        }
        return platformCommandService;
    }

    @Override
    public PlatformSessionService getPlatformSessionService() {
        if (platformSessionService == null) {
            platformSessionService = beanAccessor.getService(PlatformSessionService.class);
        }
        return platformSessionService;
    }

    @Override
    public NodeConfiguration getPlatformConfiguration() {
        if (platformConfguration == null) {
            platformConfguration = beanAccessor.getService(NodeConfiguration.class);
        }
        return platformConfguration;
    }

    @Override
    public PlatformCacheService getPlatformCacheService() {
        if (platformCacheService == null) {
            platformCacheService = beanAccessor.getService(PlatformCacheService.class);
        }
        return platformCacheService;
    }

    @Override
    public void destroy() {
        beanAccessor.destroy();
    }

    @Override
    public BroadcastService getBroadcastService() {
        if (broadcastService == null) {
            broadcastService = beanAccessor.getService(BroadcastService.class);
        }
        return broadcastService;
    }

    @Override
    public PlatformAuthenticationService getPlatformAuthenticationService() {
        if (platformAuthenticationService == null) {
            platformAuthenticationService = beanAccessor.getService(PlatformAuthenticationService.class);
        }
        return platformAuthenticationService;
    }
}
