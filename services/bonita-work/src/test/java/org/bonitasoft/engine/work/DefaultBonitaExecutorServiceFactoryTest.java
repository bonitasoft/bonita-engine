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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.commons.time.DefaultEngineClock;
import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMetricsProvider;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultBonitaExecutorServiceFactoryTest {

    @Mock
    private WorkFactory workFactory;
    @Mock
    private WorkExecutionCallback workExecutionCallback;

    @Test
    public void threadNameInExecutorService_should_contain_tenantId() {
        long tenantId = 999;
        DefaultBonitaExecutorServiceFactory defaultBonitaExecutorServiceFactory = new DefaultBonitaExecutorServiceFactory(
                tenantId,
                new SimpleMeterRegistry(),
                new DefaultEngineClock(),
                workFactory,
                mock(WorkExecutionAuditor.class),
                new DefaultExecutorServiceMetricsProvider(),
                new WorkSingleThreadPoolExecutorFactory(tenantId, 10));

        BonitaExecutorService createExecutorService = defaultBonitaExecutorServiceFactory
                .createExecutorService(workExecutionCallback);
        Runnable r = () -> {
        };

        String name = createExecutorService.getExecutor().getThreadFactory().newThread(r).getName();
        assertThat(name).as("thread name should contains the tenantId").contains(Long.toString(tenantId));
    }

    @Test
    public void createExecutorService_should_register_ExecutorServiceMetrics() {
        // given:
        long tenantId = 97L;
        final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DefaultBonitaExecutorServiceFactory defaultBonitaExecutorServiceFactory = new DefaultBonitaExecutorServiceFactory(
                tenantId,
                meterRegistry,
                new DefaultEngineClock(),
                workFactory,
                mock(WorkExecutionAuditor.class),
                new DefaultExecutorServiceMetricsProvider(),
                new WorkSingleThreadPoolExecutorFactory(tenantId, 10));

        // when:
        defaultBonitaExecutorServiceFactory.createExecutorService(workExecutionCallback);

        // then:
        assertThat(
                meterRegistry.find("executor.pool.size")
                        .tag("name", "bonita-work-executor")
                        .tag("tenant", String.valueOf(tenantId))
                        .gauge())
                .isNotNull();
    }

    @Test
    public void should_not_have_metrics_when_unbind_is_called() {
        // given:
        long tenantId = 97L;
        final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DefaultBonitaExecutorServiceFactory defaultBonitaExecutorServiceFactory = new DefaultBonitaExecutorServiceFactory(
                tenantId,
                meterRegistry,
                new DefaultEngineClock(),
                workFactory,
                mock(WorkExecutionAuditor.class),
                new DefaultExecutorServiceMetricsProvider(),
                new WorkSingleThreadPoolExecutorFactory(tenantId, 10));

        // when:
        defaultBonitaExecutorServiceFactory.createExecutorService(workExecutionCallback);
        defaultBonitaExecutorServiceFactory.unbind();

        // then:
        assertThat(
                meterRegistry.find("executor.pool.size")
                        .tag("name", "bonita-work-executor")
                        .tag("tenant", String.valueOf(tenantId))
                        .gauge())
                .isNull();
    }
}
