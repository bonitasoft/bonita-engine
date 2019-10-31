package org.bonitasoft.engine.configuration.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.bonitasoft.engine.configuration.monitoring.LoggingMeterRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;

public class LoggingMeterRegistryTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    public static final Logger logger = LoggerFactory.getLogger(LoggingMeterRegistryTest.class);

    private LoggingMeterRegistry loggingMeterRegistry = new LoggingMeterRegistry(
            k -> k.equals("logging.step") ? Duration.ofMillis(10).toString() : null,
            Clock.SYSTEM);

    @Test
    public void should_print_readable_logs() throws Exception {
        AtomicInteger gauge = new AtomicInteger();

        loggingMeterRegistry.gauge("my.gauge", gauge);
        loggingMeterRegistry.gauge("my.gauge2", Tags.of("t1", "1", "t2", "2"), gauge);
        Counter counter = loggingMeterRegistry.counter("my.counter", "tenant", "12");

        systemOutRule.clearLog();
        for (int i = 0; i < 3; i++) {
            gauge.incrementAndGet();
            counter.increment(3);
            Thread.sleep(10);
        }
        await().until(() -> assertThat(systemOutRule.getLog())
                .contains("my.counter {tenant=12}")
                .contains("my.gauge value=")
                .contains("my.gauge2 {t1=1, t2=2} value=")
                .contains("throughput="));

    }

}
