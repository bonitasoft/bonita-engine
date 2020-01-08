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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TENANT_ID;
import static org.bonitasoft.engine.scheduler.impl.MonitoringJobListener.JOB_JOBS_EXECUTED;
import static org.bonitasoft.engine.scheduler.impl.MonitoringJobListener.JOB_JOBS_RUNNING;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

public class MonitoringJobListenerTest {

    private static final long TENANT1 = 1L;
    private static final long TENANT2 = 2L;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);
    private MonitoringJobListener monitoringJobListener = new MonitoringJobListener(meterRegistry);
    private Map<String, Serializable> contextTenant1 = singletonMap(TENANT_ID, TENANT1);
    private Map<String, Serializable> contextTenant2 = singletonMap(TENANT_ID, TENANT2);

    @Test
    public void should_count_executing_jobs() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);

        assertThat(meterRegistry.find(JOB_JOBS_RUNNING).tag("tenant", "1").gauge().value()).isEqualTo(2);
    }

    @Test
    public void should_count_executing_jobs_on_multiple_tenants() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant2);
        monitoringJobListener.jobToBeExecuted(contextTenant2);

        assertThat(meterRegistry.find(JOB_JOBS_RUNNING).tag("tenant", "1").gauge().value()).isEqualTo(1);
        assertThat(meterRegistry.find(JOB_JOBS_RUNNING).tag("tenant", "2").gauge().value()).isEqualTo(2);
    }

    @Test
    public void should_count_executed_jobs() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);

        assertThat(meterRegistry.find(JOB_JOBS_EXECUTED).tag("tenant", "1").counter().count()).isEqualTo(2);
    }

    @Test
    public void should_count_executed_jobs_on_multiple_tenants() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);
        monitoringJobListener.jobToBeExecuted(contextTenant2);
        monitoringJobListener.jobToBeExecuted(contextTenant2);
        monitoringJobListener.jobWasExecuted(contextTenant2, null);
        monitoringJobListener.jobWasExecuted(contextTenant2, null);

        assertThat(meterRegistry.find(JOB_JOBS_EXECUTED).tag("tenant", "1").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.find(JOB_JOBS_EXECUTED).tag("tenant", "2").counter().count()).isEqualTo(2);
    }

}
