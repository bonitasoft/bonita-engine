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
package org.bonitasoft.engine.configuration.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        await().untilAsserted(() -> assertThat(systemOutRule.getLog())
                .contains("my.counter {tenant=12}")
                .contains("my.gauge value=")
                .contains("my.gauge2 {t1=1, t2=2} value=")
                .contains("throughput="));

    }

}
