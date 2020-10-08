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
package org.bonitasoft.engine.platform.configuration.monitoring;

import static io.micrometer.core.instrument.Clock.SYSTEM;
import static io.micrometer.core.instrument.config.MeterFilter.denyNameStartsWith;
import static io.micrometer.core.instrument.config.MeterFilter.denyUnless;

import java.util.List;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMetricsProvider;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.monitoring.NoOpExecutorServiceMetricsProvider;
import org.bonitasoft.engine.persistence.HibernateMetricsBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration related to Monitoring
 */
@Configuration
public class MonitoringConfiguration {

    public final static String MONITORING_PREFIX = "org.bonitasoft.engine.monitoring.";
    private final static String PUBLISHER = "publisher.";
    public final static String METRICS = "metrics.";

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + PUBLISHER + "jmx.enable", matchIfMissing = true)
    public JmxMeterRegistry jmxMeterRegistry(JmxConfig jmxConfig) {
        JmxMeterRegistry jmxMeterRegistry = new JmxMeterRegistry(jmxConfig, SYSTEM);
        jmxMeterRegistry.config()
                //ignore jvm and tomcat related metrics that are already exposed on jmx
                .meterFilter(denyNameStartsWith("jvm."))
                .meterFilter(denyNameStartsWith("tomcat."));
        return jmxMeterRegistry;
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + PUBLISHER + "logging.enable")
    public LoggingMeterRegistry loggingMeterRegistry(LoggingRegistryConfig loggingRegistryConfig) {
        LoggingMeterRegistry registry = new LoggingMeterRegistry(loggingRegistryConfig, SYSTEM);
        //deny all meters that are not bonita related
        registry.config().meterFilter(denyUnless(id -> id.getName().startsWith("bonita.bpmengine.")));
        return registry;
    }

    @Bean
    public LoggingRegistryConfig loggingRegistryConfig(ApplicationContext context) {
        return key -> context.getEnvironment().getProperty(MONITORING_PREFIX + PUBLISHER + key);
    }

    @Bean
    public JmxConfig jmxRegistryConfig(ApplicationContext context) {
        return key -> context.getEnvironment().getProperty(MONITORING_PREFIX + PUBLISHER + key);
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "jvm.memory.enable")
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "jvm.threads.enable")
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "jvm.gc.enable")
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "tomcat.enable")
    public TomcatMetrics tomcatMetrics() {
        return new TomcatMetrics();
    }

    @Bean
    public MeterRegistry meterRegistry(List<MeterRegistry> meterRegistries, List<MeterBinder> meterBinders) {
        MeterRegistryFactory meterRegistryFactory = new MeterRegistryFactory();
        meterRegistryFactory.setMeterRegistries(meterRegistries);
        meterRegistryFactory.setMeterBinders(meterBinders);
        return meterRegistryFactory.create();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "executors.enable")
    public ExecutorServiceMetricsProvider meterBinder() {
        return new DefaultExecutorServiceMetricsProvider();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS
            + "executors.enable", havingValue = "false", matchIfMissing = true)
    public ExecutorServiceMetricsProvider emptyMeterBinder() {
        return new NoOpExecutorServiceMetricsProvider();
    }

    @Bean
    @ConditionalOnProperty(value = "bonita.platform.persistence.generate_statistics")
    public HibernateMetricsBinder activatedHibernateMetrics(MeterRegistry meterRegistry) {
        return ((sessionFactory) -> new HibernateMetrics(sessionFactory, "persistence", null).bindTo(meterRegistry));
    }

    @Bean
    @ConditionalOnProperty(value = "bonita.platform.persistence.generate_statistics", havingValue = "false", matchIfMissing = true)
    public HibernateMetricsBinder deactivatedHibernateMetrics() {
        return ((sessionFactory) -> {
        });
    }

}
