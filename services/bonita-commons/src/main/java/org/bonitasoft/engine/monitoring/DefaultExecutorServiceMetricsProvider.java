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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

/**
 * @author Emmanuel Duchastenier
 */
public class DefaultExecutorServiceMetricsProvider implements ExecutorServiceMetricsProvider {

    @Override
    public ExecutorService bind(MeterRegistry meterRegistry, ThreadPoolExecutor executorService,
            String executorServiceName) {
        return ExecutorServiceMetrics.monitor(meterRegistry, executorService, executorServiceName,
                List.of());
    }

    @Override
    public void bindMetricsOnly(MeterRegistry meterRegistry, ThreadPoolExecutor executorService,
            String executorServiceName) {
        new ExecutorServiceMetrics(executorService, executorServiceName, List.of()).bindTo(meterRegistry);
    }

    @Override
    public void unbind(MeterRegistry meterRegistry, String executorServiceName) {
        //right now, there is no unbind method on the MeterBinder, manually unbind them
        Optional.ofNullable(meterRegistry.find("executor").timer()).ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.active").gauge()).ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.pool.size").gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.pool.max").gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.pool.core").gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.queue.remaining").gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.queued").gauge()).ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.completed").functionCounter())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.idle").timer()).ifPresent(meterRegistry::remove);
    }
}
