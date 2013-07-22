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
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandBuilderAccessor;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;

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

    TransactionExecutor getTransactionExecutor();

    PlatformSessionService getPlatformSessionService();

    SessionService getSessionService();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    DependencyBuilderAccessor getDependencyBuilderAccessor();

    PlatformCommandService getPlatformCommandService();

    SPlatformCommandBuilderAccessor getSPlatformCommandBuilderAccessor();

    NodeConfiguration getPlaformConfiguration();

    WorkService getWorkService();

}
