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
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.platform.PlatformManager;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Accessor for tenant level engine services.
 * <p>
 * All server side services of the platform can be accessed using this class. Using server side services instead of an
 * API might cause unexpected behaviors and
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

    TransactionService getTransactionService();

    TenantServiceAccessor getTenantServiceAccessor();

    PlatformSessionService getPlatformSessionService();

    ClassLoaderService getClassLoaderService();

    DependencyService getDependencyService();

    PlatformCommandService getPlatformCommandService();

    NodeConfiguration getPlatformConfiguration();

    PlatformManager getPlatformManager();

    CacheService getPlatformCacheService();

    void destroy();

    BroadcastService getBroadcastService();

    PlatformAuthenticationService getPlatformAuthenticationService();

    <T> T lookup(String serviceName) throws NotFoundException;

    ServicesResolver getServicesResolver();

    ApplicationEventPublisher getApplicationEventPublisher();
}
