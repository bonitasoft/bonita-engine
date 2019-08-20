package org.bonitasoft.engine.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MeterConfiguration.class)
@TestPropertySource(properties = {
        "org.bonitasoft.engine.monitoring.jmx.enable=true",
        "org.bonitasoft.engine.monitoring.logging.enable=true",
        "org.bonitasoft.engine.monitoring.metrics.jvm.memory.enable=true",
        "org.bonitasoft.engine.monitoring.metrics.jvm.threads.enable=true",
        "org.bonitasoft.engine.monitoring.metrics.jvm.gc.enable=true"
})
public class MeterConfigurationTest {


    @Autowired
    @Qualifier("meterRegistry")
    private MeterRegistry meterRegistry;

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

}