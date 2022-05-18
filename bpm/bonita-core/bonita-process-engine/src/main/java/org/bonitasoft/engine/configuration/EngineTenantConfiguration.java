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
package org.bonitasoft.engine.configuration;

import org.bonitasoft.engine.authentication.AuthenticationConfiguration;
import org.bonitasoft.engine.business.application.impl.ApplicationConfiguration;
import org.bonitasoft.engine.cache.CacheServiceConfiguration;
import org.bonitasoft.engine.core.operation.OperationServiceConfiguration;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.impl.EventServiceImpl;
import org.bonitasoft.engine.identity.IdentityConfiguration;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.PageConfiguration;
import org.bonitasoft.engine.session.SessionServiceConfiguration;
import org.bonitasoft.engine.work.WorkServiceConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({
        "org.bonitasoft.engine.tenant",
        "org.bonitasoft.engine.execution",
        "org.bonitasoft.engine.operation",
        "org.bonitasoft.engine.business.application",
        "org.bonitasoft.engine.core.login",
        "org.bonitasoft.engine.authorization",
        "org.bonitasoft.engine.profile"
})
@Import({
        WorkServiceConfiguration.class,
        OperationServiceConfiguration.class,
        SessionServiceConfiguration.class,
        AuthenticationConfiguration.class,
        IdentityConfiguration.class,
        CacheServiceConfiguration.class,
        ApplicationConfiguration.class,
        PageConfiguration.class
})
public class EngineTenantConfiguration {

    //There is one instance of that bean in the platform context and one in the tenant context
    // Also it is overridden in cluster mode
    // the bean is injected using the name "tenantEventService" and is overridden thanks to the ConditionalOnMissingBean condition on the name
    @Bean("tenantEventService")
    @ConditionalOnMissingBean(name = "tenantEventService")
    EventService tenantEventService(TechnicalLoggerService logger) {
        return new EventServiceImpl(logger);
    }

}
