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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.scheduler.BonitaJobListener;

public class MonitoringJobListener implements BonitaJobListener {

    private static final long serialVersionUID = 2830540082890033377L;

    public static final String JOB_JOBS_RUNNING = "bonita.bpmengine.job.running";
    public static final String JOB_JOBS_EXECUTED = "bonita.bpmengine.job.executed";

    private final Map<Long, AtomicLong> runningJobs = new ConcurrentHashMap<>();
    private final Map<Long, Counter> executedCounter = new ConcurrentHashMap<>();

    private MeterRegistry meterRegistry;

    public MonitoringJobListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        final Long tenantId = (Long) context.get(TENANT_ID);
        initializeOrGetRunningJob(tenantId).incrementAndGet();
    }

    private AtomicLong initializeOrGetRunningJob(Long tenantId) {
        AtomicLong counter = runningJobs.get(tenantId);
        if (counter == null) {
            synchronized (this) {
                if (runningJobs.get(tenantId) == null) {
                    AtomicLong atomicLong = new AtomicLong();
                    runningJobs.put(tenantId, atomicLong);
                    Gauge.builder(JOB_JOBS_RUNNING, atomicLong, AtomicLong::get)
                            .tag("tenant", tenantId.toString()).baseUnit("jobs")
                            .description("Number of jobs currently running")
                            .register(meterRegistry);
                }
                counter = runningJobs.get(tenantId);
            }
        }
        return counter;
    }

    private Counter initializeOrGetExecutedJobs(Long tenantId) {
        Counter counter = executedCounter.get(tenantId);
        if (counter == null) {
            synchronized (this) {
                if (executedCounter.get(tenantId) == null) {
                    executedCounter.put(tenantId,
                            meterRegistry.counter(JOB_JOBS_EXECUTED, "tenant", tenantId.toString()));
                }
                counter = executedCounter.get(tenantId);
            }
        }
        return counter;
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        final Long tenantId = (Long) context.get(TENANT_ID);
        initializeOrGetRunningJob(tenantId).decrementAndGet();
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        final Long tenantId = (Long) context.get(TENANT_ID);
        initializeOrGetRunningJob(tenantId).decrementAndGet();
        initializeOrGetExecutedJobs(tenantId).increment();
    }

}
