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
package org.bonitasoft.engine.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceRestartHandlerTest {

    @Mock
    private SchedulerService schedulerService;
    @Mock
    private UserTransactionService userTransactionService;

    private SchedulerServiceRestartHandler handler;

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void before() throws Exception {
        handler = new SchedulerServiceRestartHandler(schedulerService, userTransactionService);
        when(userTransactionService.executeInTransaction(any()))
                .thenAnswer(invocation -> ((Callable) invocation.getArgument(0)).call());

    }

    @Test
    public void should_call_rescheduleErroneousTriggers_in_transaction() throws Exception {
        handler.execute();

        verify(schedulerService).rescheduleErroneousTriggers();
        verify(userTransactionService).executeInTransaction(any());
    }

    @Test
    public void should_log_when_rescheduleErroneousTriggers_fails() throws SBonitaException {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        systemOutRule.clearLog();
        handler.execute();

        assertThat(systemOutRule.getLog()).contains(
                "Unable to reschedule all erroneous triggers, call PlatformAPI.rescheduleErroneousTriggers to retry.");
    }

}
