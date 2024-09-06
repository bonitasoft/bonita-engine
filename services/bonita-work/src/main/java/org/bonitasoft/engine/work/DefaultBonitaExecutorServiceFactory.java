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
package org.bonitasoft.engine.work;

import java.util.concurrent.ThreadPoolExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Use ThreadPoolExecutor as ExecutorService
 * The handling of threads relies on the JVM
 * The rules to create new thread are:
 * - If the number of threads is less than the corePoolSize, create a new Thread to run a new task.
 * - If the number of threads is equal (or greater than) the corePoolSize, put the task into the queue.
 * - If the queue is full, and the number of threads is less than the maxPoolSize, create a new thread to run tasks in.
 * - If the queue is full, and the number of threads is greater than or equal to maxPoolSize, reject the task.
 * When the current number of threads are > than corePoolSize, they are kept idle during keepAliveTimeSeconds
 *
 * @author Baptiste Mesta
 */

@Component("bonitaExecutorServiceFactory")
public class DefaultBonitaExecutorServiceFactory implements BonitaExecutorServiceFactory {

    private static final String BONITA_WORK_EXECUTOR = "bonita-work-executor";
    private final Logger logger = LoggerFactory.getLogger(DefaultBonitaExecutorServiceFactory.class);

    private final long tenantId;
    private final MeterRegistry meterRegistry;
    private final ExecutorServiceMetricsProvider executorServiceMetricsProvider;
    private final BonitaThreadPoolExecutorFactory bonitaThreadPoolExecutorFactory;
    private final EngineClock engineClock;
    private final WorkFactory workFactory;
    private final WorkExecutionAuditor workExecutionAuditor;

    public DefaultBonitaExecutorServiceFactory(@Value("${tenantId}") long tenantId,
            MeterRegistry meterRegistry,
            EngineClock engineClock,
            WorkFactory workFactory,
            WorkExecutionAuditor workExecutionAuditor,
            ExecutorServiceMetricsProvider executorServiceMetricsProvider,
            BonitaThreadPoolExecutorFactory bonitaThreadPoolExecutorFactory) {
        this.tenantId = tenantId;
        this.meterRegistry = meterRegistry;
        this.workFactory = workFactory;
        this.workExecutionAuditor = workExecutionAuditor;
        this.engineClock = engineClock;
        this.executorServiceMetricsProvider = executorServiceMetricsProvider;
        this.bonitaThreadPoolExecutorFactory = bonitaThreadPoolExecutorFactory;
    }

    @Override
    public BonitaExecutorService createExecutorService(WorkExecutionCallback workExecutionCallback) {
        final ThreadPoolExecutor bonitaThreadPoolExecutor = bonitaThreadPoolExecutorFactory.create();
        final BonitaExecutorService bonitaExecutorService = new DefaultBonitaExecutorService(bonitaThreadPoolExecutor,
                workFactory,
                engineClock,
                workExecutionCallback,
                workExecutionAuditor,
                meterRegistry,
                tenantId);
        logger.info(
                "Creating a new Thread pool to handle works: {}", bonitaThreadPoolExecutor);

        //TODO this returns the timed executor service, this should be used instead of the BonitaExecutorService but we should change it everywhere
        executorServiceMetricsProvider
                .bindMetricsOnly(meterRegistry, bonitaThreadPoolExecutor, BONITA_WORK_EXECUTOR, tenantId);
        return bonitaExecutorService;
    }

    @Override
    public void unbind() {
        executorServiceMetricsProvider.unbind(meterRegistry, BONITA_WORK_EXECUTOR, tenantId);
    }

}
