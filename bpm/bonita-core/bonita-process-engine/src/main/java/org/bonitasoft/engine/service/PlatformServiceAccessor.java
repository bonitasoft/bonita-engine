/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.PlatformMonitoringService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderAccessor;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 */
public interface PlatformServiceAccessor extends ServiceAccessor {

    PlatformService getPlatformService();

    SPlatformBuilder getSPlatformBuilder();

    PlatformLoginService getPlatformLoginService();

    SchedulerService getSchedulerService();

    TechnicalLoggerService getTechnicalLoggerService();

    TransactionService getTransactionService();

    STenantBuilder getSTenantBuilder();

    IdentityService getIdentityService();

    IdentityModelBuilder getIdentityModelBuilder();

    TenantServiceAccessor getTenantServiceAccessor(long tenantId);

    PlatformMonitoringService getPlatformMonitoringService();

    TransactionExecutor getTransactionExecutor();

    PlatformSessionService getPlatformSessionService();

    SessionService getSessionService();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    DependencyBuilderAccessor getDependencyBuilderAccessor();

    PlatformCommandService getPlatformCommandService();

    SPlatformCommandBuilderAccessor getSPlatformCommandBuilderAccessor();

    SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor();

    NodeConfiguration getPlaformConfiguration();

}
