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
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 */
public class SpringPlatformServiceAccessor implements PlatformServiceAccessor {

    private PlatformService platformService;

    private PlatformLoginService platformLoginService;

    private SchedulerService schedulerService;

    private TechnicalLoggerService technicalLoggerService;

    private TransactionService transactionService;

    private TransactionExecutor transactionExecutor;

    private SessionService sessionService;

    private ClassLoaderService classLoaderService;

    private DependencyService dependencyService;

    private PlatformCommandService platformCommandService;

    private PlatformSessionService platformSessionService;

    private NodeConfiguration platformConfguration;

    private PlatformCacheService platformCacheService;

    @Override
    public TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = SpringPlatformFileSystemBeanAccessor.getService(TransactionService.class);
        }
        return transactionService;
    }

    @Override
    public TechnicalLoggerService getTechnicalLoggerService() {
        if (technicalLoggerService == null) {
            technicalLoggerService = SpringPlatformFileSystemBeanAccessor.getService("platformTechnicalLoggerService", TechnicalLoggerService.class);
        }
        return technicalLoggerService;
    }

    @Override
    public PlatformLoginService getPlatformLoginService() {
        if (platformLoginService == null) {
            platformLoginService = SpringPlatformFileSystemBeanAccessor.getService(PlatformLoginService.class);
        }
        return platformLoginService;
    }

    @Override
    public SchedulerService getSchedulerService() {
        if (schedulerService == null) {
            schedulerService = SpringPlatformFileSystemBeanAccessor.getService(SchedulerService.class);
        }
        return schedulerService;
    }

    @Override
    public PlatformService getPlatformService() {
        if (platformService == null) {
            platformService = SpringPlatformFileSystemBeanAccessor.getService(PlatformService.class);
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
            transactionExecutor = SpringPlatformFileSystemBeanAccessor.getService(TransactionExecutor.class);
        }
        return transactionExecutor;
    }

    @Override
    public SessionService getSessionService() {
        if (sessionService == null) {
            sessionService = SpringPlatformFileSystemBeanAccessor.getService(SessionService.class);
        }
        return sessionService;
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        if (classLoaderService == null) {
            classLoaderService = SpringPlatformFileSystemBeanAccessor.getService("classLoaderService", ClassLoaderService.class);
        }
        return classLoaderService;
    }

    @Override
    public DependencyService getDependencyService() {
        if (dependencyService == null) {
            dependencyService = SpringPlatformFileSystemBeanAccessor.getService("platformDependencyService", DependencyService.class);
        }
        return dependencyService;
    }

    @Override
    public PlatformCommandService getPlatformCommandService() {
        if (platformCommandService == null) {
            platformCommandService = SpringPlatformFileSystemBeanAccessor.getService("platformCommandService", PlatformCommandService.class);
        }
        return platformCommandService;
    }

    @Override
    public PlatformSessionService getPlatformSessionService() {
        if (platformSessionService == null) {
            platformSessionService = SpringPlatformFileSystemBeanAccessor.getService(PlatformSessionService.class);
        }
        return platformSessionService;
    }

    @Override
    public void initializeServiceAccessor(final ClassLoader classLoader) {
        SpringPlatformFileSystemBeanAccessor.initializeContext(classLoader);
    }

    @Override
    public NodeConfiguration getPlatformConfiguration() {
        if (platformConfguration == null) {
            platformConfguration = SpringPlatformFileSystemBeanAccessor.getService(NodeConfiguration.class);
        }
        return platformConfguration;
    }

    @Override
    public PlatformCacheService getPlatformCacheService() {
        if (platformCacheService == null) {
            platformCacheService = SpringPlatformFileSystemBeanAccessor.getService(PlatformCacheService.class);
        }
        return platformCacheService;
    }

    @Override
    public void destroy() {
        SpringPlatformFileSystemBeanAccessor.destroy();
    }

}
