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
package org.bonitasoft.engine.platform.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.classloader.ClassloaderConfiguration;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.impl.EventServiceImpl;
import org.bonitasoft.engine.lock.LockServiceConfiguration;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.monitoring.NoOpExecutorServiceMetricsProvider;
import org.bonitasoft.engine.persistence.HibernateMetricsBinder;
import org.bonitasoft.engine.platform.session.PlatformSessionConfiguration;
import org.bonitasoft.engine.scheduler.SchedulerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Bonita Engine Spring configuration at platform-level
 *
 * @author Danila Mazour
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
@Configuration
@Import({
        SchedulerConfiguration.class,
        LockServiceConfiguration.class,
        PlatformSessionConfiguration.class,
        ClassloaderConfiguration.class
})
@ComponentScan("org.bonitasoft.engine.platform")
public class EnginePlatformConfiguration {

    @Bean("platformEventService")
    @ConditionalOnMissingBean(name = "platformEventService")
    EventService platformEventService(TechnicalLoggerService loggerService) {
        return new EventServiceImpl(loggerService);
    }

    @Bean
    @ConditionalOnMissingBean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public HibernateMetricsBinder noOpHibernateMetrics() {
        return (sessionFactory -> {
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public ExecutorServiceMetricsProvider noOpExecutorServiceBinder() {
        return new NoOpExecutorServiceMetricsProvider();
    }

}
