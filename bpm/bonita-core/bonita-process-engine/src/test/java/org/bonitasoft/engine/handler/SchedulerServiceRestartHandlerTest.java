/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceRestartHandlerTest {

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @InjectMocks
    private SchedulerServiceRestartHandler handler;

    @Test
    public void executeHandleCallsSchedulerServiceToRescheduleErroneousTriggers() throws SBonitaException {
        handler.execute();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test
    public void executeHandleShouldLogItsJob() throws SBonitaException {
        handler.execute();

        verify(technicalLoggerService).log(eq(SchedulerServiceRestartHandler.class), any(TechnicalLogSeverity.class), anyString());
    }

    @Test(expected = SBonitaException.class)
    public void executeHandleThrowsExceptionIfSchedulerServiceThrowsException() throws SBonitaException {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        handler.execute();
    }

}
