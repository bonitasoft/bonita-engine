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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

public class DefaultExecutorServiceMetricsProviderTest {

    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 100, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(10));
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private DefaultExecutorServiceMetricsProvider defaultExecutorServiceMetricsProvider = new DefaultExecutorServiceMetricsProvider();

    @Test
    public void should_register_metrics_when_binding_the_threadpool() {
        defaultExecutorServiceMetricsProvider.bind(meterRegistry, executorService, "my-executor", 14L);

        assertThat(meterRegistry.getMeters()).hasSize(9);
    }

    @Test
    public void should_have_no_more_metrics_when_we_unbind_the_executor() {
        defaultExecutorServiceMetricsProvider.bind(meterRegistry, executorService, "my-executor", 14L);

        defaultExecutorServiceMetricsProvider.unbind(meterRegistry, "my-executor", 14L);

        assertThat(meterRegistry.getMeters()).hasSize(0);
    }

}
