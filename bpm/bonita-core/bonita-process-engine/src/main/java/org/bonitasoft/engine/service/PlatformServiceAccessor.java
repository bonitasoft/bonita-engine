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
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * Accessor for tenant level engine services.
 * <p>
 * All server side services of the platform can be accessed using this class. Using server side services instead of an API might cause unexpected behaviors and
 * damage your data.
 * 
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 */
public interface PlatformServiceAccessor extends ServiceAccessor {

    PlatformService getPlatformService();

    PlatformLoginService getPlatformLoginService();

    SchedulerService getSchedulerService();

    TechnicalLoggerService getTechnicalLoggerService();

    TransactionService getTransactionService();

    TenantServiceAccessor getTenantServiceAccessor(long tenantId);

    TransactionExecutor getTransactionExecutor();

    PlatformSessionService getPlatformSessionService();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    PlatformCommandService getPlatformCommandService();

    NodeConfiguration getPlatformConfiguration();

    PlatformCacheService getPlatformCacheService();

    void destroy();
    BroadcastService getBroadcastService();

    PlatformAuthenticationService getPlatformAuthenticationService();
}
