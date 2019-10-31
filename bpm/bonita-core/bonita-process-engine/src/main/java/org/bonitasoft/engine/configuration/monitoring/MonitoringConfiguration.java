/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.configuration.monitoring;

import static io.micrometer.core.instrument.Clock.SYSTEM;
import static io.micrometer.core.instrument.config.MeterFilter.denyNameStartsWith;
import static io.micrometer.core.instrument.config.MeterFilter.denyUnless;

import java.util.List;

import org.bonitasoft.engine.configuration.ConditionalOnProperty;
import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMeterBinderProvider;
import org.bonitasoft.engine.monitoring.EmptyExecutorServiceMeterBinderProvider;
import org.bonitasoft.engine.monitoring.ExecutorServiceMeterBinderProvider;
import org.bonitasoft.engine.persistence.HibernateMetricsBinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;

/**
 * Configuration related to Monitoring
 */
@Configuration
public class MonitoringConfiguration {

    public final static String MONITORING_PREFIX = "org.bonitasoft.engine.monitoring.";
    private final static String PUBLISHER = "publisher.";
    public final static String METRICS = "metrics.";

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + PUBLISHER + "jmx.enable", enableIfMissing = true)
    public JmxMeterRegistry jmxMeterRegistry(JmxConfig jmxConfig) {
        JmxMeterRegistry jmxMeterRegistry = new JmxMeterRegistry(jmxConfig, SYSTEM);
        jmxMeterRegistry.config()
                //ignore jvm and tomcat related metrics that are already exposed on jmx
                .meterFilter(denyNameStartsWith("jvm."))
                .meterFilter(denyNameStartsWith("tomcat."));
        return jmxMeterRegistry;
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + PUBLISHER + "logging.enable", enableIfMissing = false)
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
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "jvm.memory.enable", enableIfMissing = false)
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "jvm.threads.enable", enableIfMissing = false)
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "jvm.gc.enable", enableIfMissing = false)
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + METRICS + "tomcat.enable", enableIfMissing = false)
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
    @ConditionalOnProperty(value = MONITORING_PREFIX
            + METRICS + "executors.enable" /* , havingValue=true */, enableIfMissing = false)
    public ExecutorServiceMeterBinderProvider meterBinder() {
        return new DefaultExecutorServiceMeterBinderProvider();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX
            + METRICS + "executors.enable", havingValue = false, enableIfMissing = true)
    public ExecutorServiceMeterBinderProvider emptyMeterBinder() {
        return new EmptyExecutorServiceMeterBinderProvider();
    }

    @Bean
    @ConditionalOnProperty(value = "bonita.platform.persistence.generate_statistics", enableIfMissing = false)
    public HibernateMetricsBinder activatedHibernateMetrics(MeterRegistry meterRegistry) {
        return ((sessionFactory) -> new HibernateMetrics(sessionFactory, "persistence", null).bindTo(meterRegistry));
    }

    @Bean
    @ConditionalOnProperty(value = "bonita.platform.persistence.generate_statistics", havingValue = false, enableIfMissing = true)
    public HibernateMetricsBinder deactivatedHibernateMetrics() {
        return ((sessionFactory) -> {
        });
    }

}
