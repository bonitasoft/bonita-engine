package org.bonitasoft.engine.configuration;

import static org.bonitasoft.engine.configuration.EnginePlatformConfiguration.MONITORING_PREFIX;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;
import org.bonitasoft.engine.persistence.TenantHibernatePersistenceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineTenantConfiguration {

    @Bean
    @ConditionalOnProperty(value = MONITORING_PREFIX + "metrics.hibernate.enable", enableIfMissing = true)
    public HibernateMetrics hibernateMetrics(@Qualifier("meterRegistry") MeterRegistry meterRegistry,
                                             TenantHibernatePersistenceService tenantHibernatePersistenceService,
                                             @Value("${tenantId}") long tenantId) {
        HibernateMetrics hibernateMetrics = new HibernateMetrics(tenantHibernatePersistenceService.getSessionFactory(), "persistence", Tags.of("tenant", String.valueOf(tenantId)));
        hibernateMetrics.bindTo(meterRegistry);
        return hibernateMetrics;
    }

}
