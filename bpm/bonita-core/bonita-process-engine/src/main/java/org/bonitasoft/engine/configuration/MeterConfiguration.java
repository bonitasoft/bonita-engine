package org.bonitasoft.engine.configuration;

import static io.micrometer.core.instrument.Clock.SYSTEM;

import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
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
        return new LoggingMeterRegistry(loggingRegistryConfig, SYSTEM);
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
