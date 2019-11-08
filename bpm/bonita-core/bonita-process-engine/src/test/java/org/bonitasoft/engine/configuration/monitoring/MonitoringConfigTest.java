package org.bonitasoft.engine.configuration.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMetricsProvider;
import org.bonitasoft.engine.monitoring.NoOpExecutorServiceMetricsProvider;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.persistence.HibernateMetricsBinder;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;

public class MonitoringConfigTest {

    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = MonitoringConfiguration.class)
    public abstract static class BaseMeterConfigurationTest {

        @Autowired
        @Qualifier("meterRegistry")
        protected MeterRegistry meterRegistry;

        @Autowired
        protected ExecutorServiceMetricsProvider executorServiceMetricsProvider;

    }

    public static class DefaultMonitoringConfigTest extends BaseMeterConfigurationTest {

        @Test
        public void should_have_all_registries_activated_by_default() {
            assertThat(meterRegistry).isInstanceOf(JmxMeterRegistry.class);
        }

        @Test
        public void should_have_no_jvm_metrics_activated_by_default() {
            assertThat(meterRegistry.find("jvm.memory.used").gauge()).isNull();
            assertThat(meterRegistry.find("jvm.threads.live").gauge()).isNull();
            assertThat(meterRegistry.find("jvm.gc.max.data.size").gauge()).isNull();
        }

        @Test
        public void should_have_no_executor_metrics_values_by_default() {
            assertThat(executorServiceMetricsProvider).isInstanceOf(NoOpExecutorServiceMetricsProvider.class);
        }

        @Test
        public void should_have_no_hibernate_metrics_by_default() {
            assertThat(meterRegistry.find("hibernate.query.natural.id.executions.max").gauge()).isNull();
        }
    }

    @TestPropertySource(properties = {
            "org.bonitasoft.engine.monitoring.publisher.jmx.enable=true",
            "org.bonitasoft.engine.monitoring.publisher.logging.enable=true",
            "org.bonitasoft.engine.monitoring.metrics.jvm.memory.enable=true",
            "org.bonitasoft.engine.monitoring.metrics.jvm.threads.enable=true",
            "org.bonitasoft.engine.monitoring.metrics.jvm.gc.enable=true",
            "org.bonitasoft.engine.monitoring.metrics.executors.enable=true",
            "bonita.platform.persistence.generate_statistics=true"
    })
    public static class OverriddenPropertiesMonitoringConfigTest extends BaseMeterConfigurationTest {

        @Rule
        public MockitoRule mockitoRule = MockitoJUnit.rule();

        @Autowired
        private HibernateMetricsBinder hibernateMetricsBinder;

        @Mock
        private SessionFactory sessionFactory;

        @Test
        public void should_have_all_registries_activated() {
            assertThat(meterRegistry).isInstanceOf(CompositeMeterRegistry.class);
            assertThat(((CompositeMeterRegistry) meterRegistry).getRegistries())
                    .allMatch(m -> m instanceof LoggingMeterRegistry || m instanceof JmxMeterRegistry);
        }

        @Test
        public void should_have_jvm_metrics_activated() {
            assertThat(meterRegistry.find("jvm.memory.used").gauge()).isNotNull();
            assertThat(meterRegistry.find("jvm.threads.live").gauge()).isNotNull();
            assertThat(meterRegistry.find("jvm.gc.max.data.size").gauge()).isNotNull();
        }

        @Test
        public void should_provide_ExecutorServiceMeterBinder() {
            assertThat(executorServiceMetricsProvider)
                    .isInstanceOf(DefaultExecutorServiceMetricsProvider.class);
        }

        @Test
        public void should_have_hibernate_metrics_activated() {
            final Statistics stats = mock(Statistics.class);
            doReturn(true).when(stats).isStatisticsEnabled();
            doReturn(stats).when(sessionFactory).getStatistics();

            hibernateMetricsBinder.bindMetrics(sessionFactory);
            assertThat(meterRegistry.find("hibernate.query.natural.id.executions.max").gauge()).isNotNull();
        }
    }
}
