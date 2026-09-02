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
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.scheduler.BonitaJobListener;

public class MonitoringJobListener implements BonitaJobListener {

    private static final long serialVersionUID = 2830540082890033377L;

    public static final String JOB_JOBS_RUNNING = "bonita.bpmengine.job.running";
    public static final String JOB_JOBS_EXECUTED = "bonita.bpmengine.job.executed";

    private AtomicLong runningJobs;
    private Counter executedCounter;

    private final MeterRegistry meterRegistry;

    public MonitoringJobListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void jobToBeExecuted() {
        initializeOrGetRunningJob().incrementAndGet();
    }

    private AtomicLong initializeOrGetRunningJob() {
        if (runningJobs == null) {
            synchronized (this) {
                runningJobs = new AtomicLong();
                Gauge.builder(JOB_JOBS_RUNNING, runningJobs, AtomicLong::get)
                        .baseUnit("jobs")
                        .description("Number of jobs currently running")
                        .register(meterRegistry);
            }
        }
        return runningJobs;
    }

    private Counter initializeOrGetExecutedJobs() {
        if (executedCounter == null) {
            synchronized (this) {
                executedCounter = meterRegistry.counter(JOB_JOBS_EXECUTED);
            }
        }
        return executedCounter;
    }

    @Override
    public void jobExecutionVetoed() {
        initializeOrGetRunningJob().decrementAndGet();
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        initializeOrGetRunningJob().decrementAndGet();
        initializeOrGetExecutedJobs().increment();
    }

}
