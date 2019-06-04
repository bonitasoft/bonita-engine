package org.bonitasoft.engine.scheduler.impl;


import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TENANT_ID;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class MonitoringJobListenerTest {


    public static final long TENANT1 = 1L;
    public static final long TENANT2 = 2L;
    private MonitoringJobListener monitoringJobListener = new MonitoringJobListener();
    private Map<String, Serializable> contextTenant1 = singletonMap(TENANT_ID, TENANT1);
    private Map<String, Serializable> contextTenant2 = singletonMap(TENANT_ID, TENANT2);

    @Test
    public void should_count_executing_jobs() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);

        assertThat(monitoringJobListener.getNumberOfExecutingJobs(TENANT1)).isEqualTo(2);
    }
    @Test
    public void should_count_executing_jobs_on_multiple_tenants() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant2);
        monitoringJobListener.jobToBeExecuted(contextTenant2);

        assertThat(monitoringJobListener.getNumberOfExecutingJobs(TENANT1)).isEqualTo(1);
        assertThat(monitoringJobListener.getNumberOfExecutingJobs(TENANT2)).isEqualTo(2);
    }
    @Test
    public void should_count_executed_jobs() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);

        assertThat(monitoringJobListener.getNumberOfExecutedJobs(1L)).isEqualTo(2);
    }

    @Test
    public void should_count_executed_jobs_on_multiple_tenants() {
        monitoringJobListener.jobToBeExecuted(contextTenant1);
        monitoringJobListener.jobWasExecuted(contextTenant1, null);
        monitoringJobListener.jobToBeExecuted(contextTenant2);
        monitoringJobListener.jobToBeExecuted(contextTenant2);
        monitoringJobListener.jobWasExecuted(contextTenant2, null);
        monitoringJobListener.jobWasExecuted(contextTenant2, null);

        assertThat(monitoringJobListener.getNumberOfExecutedJobs(TENANT1)).isEqualTo(1);
        assertThat(monitoringJobListener.getNumberOfExecutedJobs(TENANT2)).isEqualTo(2);
    }

}