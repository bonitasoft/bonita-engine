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
package org.bonitasoft.engine.monitoring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * @author Emmanuel Duchastenier
 */
public interface ExecutorServiceMetricsProvider {

    /**
     * bind the executor service to the registry, only support ThreadPool right now, see
     * {@link DefaultExecutorServiceMetricsProvider#unbind(MeterRegistry, String, long)}
     *
     * @return the monitored executor service with monitoring on execution time
     */
    ExecutorService bind(MeterRegistry meterRegistry, ThreadPoolExecutor executorService, String executorServiceName,
            long tenantId);

    /**
     * bind the executor service to the registry, only support ThreadPool right now, see
     * {@link DefaultExecutorServiceMetricsProvider#unbind(MeterRegistry, String, long)}
     * This will only bind statisctics of the Threadpool, and not time taks.
     */
    void bindMetricsOnly(MeterRegistry meterRegistry, ThreadPoolExecutor executorService, String executorServiceName,
            long tenantId);

    /**
     * unbind all metrics of the named executor service from the meter registry
     */
    void unbind(MeterRegistry meterRegistry, String executorServiceName, long tenantId);
}
