/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.work.failurewrapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingSignalEventImpl;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class TriggerSignalWorkTest {

    public static final long UNREADABLE_SIGNAL_ID = 58923749333L;
    private TriggerSignalWork triggerSignalWork;
    private String SIGNAL_NAME = "theSignal";
    private long SIGNAL_ID = 5643261L;
    private Map<String, Object> context;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private EventsHandler eventsHandler;
    @Mock
    private EventInstanceService eventInstanceService;
    private SWaitingSignalEventImpl waitingSignalEvent;

    @Before
    public void before() throws Exception {
        context = new HashMap<>();
        context.put("tenantAccessor", tenantServiceAccessor);
        doReturn(eventInstanceService).when(tenantServiceAccessor).getEventInstanceService();
        doReturn(eventsHandler).when(tenantServiceAccessor).getEventsHandler();
        waitingSignalEvent = new SWaitingSignalEventImpl(SBPMEventType.EVENT_SUB_PROCESS, 654223L, "proc", 54362L, "flownode", SIGNAL_NAME);
        doThrow(SEventTriggerInstanceNotFoundException.class).when(eventInstanceService).getWaitingSignalEvent(anyLong());
        doReturn(waitingSignalEvent).when(eventInstanceService).getWaitingSignalEvent(SIGNAL_ID);
    }

    @Test
    public void should_trigger_catch_signal() throws Exception {
        triggerSignalWork = new TriggerSignalWork(SIGNAL_ID, SIGNAL_NAME);
        //when
        triggerSignalWork.work(context);
        //then
        verify(eventsHandler).triggerCatchEvent(waitingSignalEvent, null);
    }

    @Test(expected = SEventTriggerInstanceNotFoundException.class)
    public void should_throw_exception_when_signal_not_found() throws Exception {
        triggerSignalWork = new TriggerSignalWork(5348934579L, SIGNAL_NAME);
        //when
        triggerSignalWork.work(context);
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public void should_throw_exception_when_we_are_unable_to_read_the_signal() throws Exception {
        doThrow(SEventTriggerInstanceReadException.class).when(eventInstanceService).getWaitingSignalEvent(UNREADABLE_SIGNAL_ID);
        triggerSignalWork = new TriggerSignalWork(UNREADABLE_SIGNAL_ID, SIGNAL_NAME);
        //when
        triggerSignalWork.work(context);
    }

    @Test
    public void should_give_signal_name_in_recovery_procedure() throws Exception {
        //given
        triggerSignalWork = new TriggerSignalWork(SIGNAL_ID, SIGNAL_NAME);
        //when
        String recoveryProcedure = triggerSignalWork.getRecoveryProcedure();
        //then
        assertThat(recoveryProcedure).contains(SIGNAL_NAME);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void should_throw_exception_when_handling_failure() throws Exception {
        //given
        triggerSignalWork = new TriggerSignalWork(SIGNAL_ID, SIGNAL_NAME);
        //when
        triggerSignalWork.handleFailure(new Exception("unexpected"), context);
    }

}
