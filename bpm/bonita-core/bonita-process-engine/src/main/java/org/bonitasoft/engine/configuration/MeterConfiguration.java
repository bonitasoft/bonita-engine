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
import static io.micrometer.core.instrument.config.MeterFilter.denyUnless;

import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Danila Mazour
 */
@Configuration
public class MeterConfiguration {

    private final static String MONITORING_PREFIX = "org.bonitasoft.engine.monitoring.";

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "jmx.enable", enableIfMissing = true)
    public JmxMeterRegistry jmxMeterRegistry(JmxConfig jmxConfig) {
        return new JmxMeterRegistry(jmxConfig, SYSTEM);
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
}
