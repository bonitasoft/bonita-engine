/**
 * Copyright (C) 2015-2019 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.concurrent.ThreadPoolExecutor;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.commons.time.DefaultEngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
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
    public void ThreadNameInExecutorServiceShouldContainsTenantId() {
        long tenantId = 999;
        DefaultBonitaExecutorServiceFactory defaultBonitaExecutorServiceFactory = new DefaultBonitaExecutorServiceFactory(
                new TechnicalLoggerSLF4JImpl(12L), workFactory, tenantId, 1,
                20, 15, 10, new DefaultEngineClock(), mock(WorkExecutionAuditor.class), new SimpleMeterRegistry());

        BonitaExecutorService createExecutorService = defaultBonitaExecutorServiceFactory.createExecutorService(workExecutionCallback);
        Runnable r = () -> {
        };

        String name = ((ThreadPoolExecutor) createExecutorService).getThreadFactory().newThread(r).getName();
        assertThat(name).as("thread name should contains the tenantId").contains(Long.toString(tenantId));
    }
}
