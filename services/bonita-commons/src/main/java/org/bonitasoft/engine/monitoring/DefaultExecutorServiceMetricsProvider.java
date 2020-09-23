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

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

/**
 * @author Emmanuel Duchastenier
 */
public class DefaultExecutorServiceMetricsProvider implements ExecutorServiceMetricsProvider {

    @Override
    public ExecutorService bind(MeterRegistry meterRegistry, ThreadPoolExecutor executorService,
            String executorServiceName, long tenantId) {
        return ExecutorServiceMetrics.monitor(meterRegistry, executorService, executorServiceName,
                Tags.of("tenant", String.valueOf(tenantId)));
    }

    @Override
    public void bindMetricsOnly(MeterRegistry meterRegistry, ThreadPoolExecutor executorService,
            String executorServiceName, long tenantId) {
        new ExecutorServiceMetrics(executorService, executorServiceName, Tags.of("tenant", String.valueOf(tenantId)))
                .bindTo(meterRegistry);
    }

    @Override
    public void unbind(MeterRegistry meterRegistry, String executorServiceName, long tenantId) {
        Tags tags = Tags.of("name", executorServiceName, "tenant", String.valueOf(tenantId));
        //right now, there is no unbind method on the MeterBinder, manually unbind them
        Optional.ofNullable(meterRegistry.find("executor").tags(tags).timer()).ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.active").tags(tags).gauge()).ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.pool.size").tags(tags).gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.pool.max").tags(tags).gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.pool.core").tags(tags).gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.queue.remaining").tags(tags).gauge())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.queued").tags(tags).gauge()).ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.completed").tags(tags).functionCounter())
                .ifPresent(meterRegistry::remove);
        Optional.ofNullable(meterRegistry.find("executor.idle").tags(tags).timer()).ifPresent(meterRegistry::remove);
    }
}
