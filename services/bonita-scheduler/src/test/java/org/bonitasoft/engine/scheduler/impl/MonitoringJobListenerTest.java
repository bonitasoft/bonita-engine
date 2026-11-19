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
package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.scheduler.impl.MonitoringJobListener.JOB_JOBS_EXECUTED;
import static org.bonitasoft.engine.scheduler.impl.MonitoringJobListener.JOB_JOBS_RUNNING;

import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

public class MonitoringJobListenerTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);
    private final MonitoringJobListener monitoringJobListener = new MonitoringJobListener(meterRegistry);

    @Test
    public void should_count_executing_jobs() {
        monitoringJobListener.jobToBeExecuted();
        monitoringJobListener.jobToBeExecuted();
        monitoringJobListener.jobToBeExecuted();
        monitoringJobListener.jobWasExecuted(null, null);

        var gauge = meterRegistry.find(JOB_JOBS_RUNNING).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(2);
    }

    @Test
    public void should_count_executed_jobs() {
        monitoringJobListener.jobToBeExecuted();
        monitoringJobListener.jobToBeExecuted();
        monitoringJobListener.jobToBeExecuted();
        monitoringJobListener.jobWasExecuted(null, null);
        monitoringJobListener.jobWasExecuted(null, null);

        var counter = meterRegistry.find(JOB_JOBS_EXECUTED).counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2);
    }

}
