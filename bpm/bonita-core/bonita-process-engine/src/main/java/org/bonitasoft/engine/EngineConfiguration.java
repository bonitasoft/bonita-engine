/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.engine;

import java.util.Properties;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.Data;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.impl.EventServiceImpl;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.monitoring.NoOpExecutorServiceMetricsProvider;
import org.bonitasoft.engine.persistence.HibernateConfigurationProvider;
import org.bonitasoft.engine.persistence.HibernateMetricsBinder;
import org.bonitasoft.engine.persistence.HibernatePersistenceService;
import org.bonitasoft.engine.persistence.QueryBuilderFactory;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@ComponentScan("org.bonitasoft.engine")
@Data
public class EngineConfiguration {

    /**
     * Whether the runtime should exit after its initialization or not.
     */
    @Value("${bonita.runtime.startup.update-only:false}")
    private boolean updateOnlyAtStartup;

    @Bean("platformEventService")
    @ConditionalOnMissingBean(name = "platformEventService")
    EventService platformEventService() {
        return new EventServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    HibernatePersistenceService persistenceService(final ReadSessionAccessor sessionAccessor,
            final HibernateConfigurationProvider hbmConfigurationProvider, final Properties extraHibernateProperties,
            final SequenceManager sequenceManager, HibernateMetricsBinder hibernateMetricsBinder,
            QueryBuilderFactory queryBuilderFactory) {
        return new HibernatePersistenceService(sessionAccessor, hbmConfigurationProvider,
                extraHibernateProperties, sequenceManager, queryBuilderFactory, hibernateMetricsBinder);
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
