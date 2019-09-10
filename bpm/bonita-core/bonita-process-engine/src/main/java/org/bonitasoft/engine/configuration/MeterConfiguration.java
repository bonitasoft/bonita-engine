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
package org.bonitasoft.engine.configuration;

import static io.micrometer.core.instrument.Clock.SYSTEM;
import static io.micrometer.core.instrument.config.MeterFilter.denyNameStartsWith;
import static io.micrometer.core.instrument.config.MeterFilter.denyUnless;

import java.util.List;

import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMeterBinderProvider;
import org.bonitasoft.engine.monitoring.EmptyExecutorServiceMeterBinderProvider;
import org.bonitasoft.engine.monitoring.ExecutorServiceMeterBinderProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;

/**
 * @author Danila Mazour
 */
@Configuration
public class MeterConfiguration {

    private final static String MONITORING_PREFIX = "org.bonitasoft.engine.monitoring.";

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "jmx.enable", enableIfMissing = true)
    public JmxMeterRegistry jmxMeterRegistry(JmxConfig jmxConfig) {
        JmxMeterRegistry jmxMeterRegistry = new JmxMeterRegistry(jmxConfig, SYSTEM);
        jmxMeterRegistry.config()
                //ignore jvm related metrics that are already exposed on jmx
                .meterFilter(denyNameStartsWith("jvm."));
        return jmxMeterRegistry;
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "logging.enable", enableIfMissing = false)
    public LoggingMeterRegistry loggingMeterRegistry(LoggingRegistryConfig loggingRegistryConfig) {
        LoggingMeterRegistry registry = new LoggingMeterRegistry(loggingRegistryConfig, SYSTEM);
        //deny all meters that are not bonita related
        registry.config()
                .meterFilter(denyUnless(id -> id.getName().startsWith("org.bonitasoft.engine.")));
        return registry;
    }

    @Bean
    public LoggingRegistryConfig loggingRegistryConfig(ApplicationContext context) {
        return key -> context.getEnvironment().getProperty(MONITORING_PREFIX + key);
    }

    @Bean
    public JmxConfig jmxRegistryConfig(ApplicationContext context) {
        return key -> context.getEnvironment().getProperty(MONITORING_PREFIX + key);
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "metrics.jvm.memory.enable", enableIfMissing = false)
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "metrics.jvm.threads.enable", enableIfMissing = false)
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "metrics.jvm.gc.enable", enableIfMissing = false)
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
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
            + "metrics.executors.enable" /* , havingValue=true */, enableIfMissing = false)
    public ExecutorServiceMeterBinderProvider meterBinder() {
        return new DefaultExecutorServiceMeterBinderProvider();
    }

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX
            + "metrics.executors.enable", havingValue = false, enableIfMissing = true)
    public ExecutorServiceMeterBinderProvider emptyMeterBinder() {
        return new EmptyExecutorServiceMeterBinderProvider();
    }

}
