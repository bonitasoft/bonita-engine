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
package org.bonitasoft.engine.execution.work.failurewrapping;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class MessageInstanceContextWorkTest extends AbstractContextWorkTest {

    @Mock
    private SMessageInstance messageInstance;

    @Mock
    private SWaitingMessageEvent waitingMessageEvent;

    private final static String MESSAGE_INSTANCE_NAME = "name";

    private final static String MESSAGE_INSTANCE_TARGET_PROCESS_NAME = "process";

    private final static String MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME = "flowNode name";

    private final static SBPMEventType WAITING_MESSAGE_EVENT_TYPE = SBPMEventType.INTERMEDIATE_THROW_EVENT;

    @Override
    @Before
    public void before() throws SBonitaException {
        txBonitawork = spy(new MessageInstanceContextWork(wrappedWork, messageInstance, waitingMessageEvent));
        doReturn(MESSAGE_INSTANCE_NAME).when(messageInstance).getMessageName();
        doReturn(MESSAGE_INSTANCE_TARGET_PROCESS_NAME).when(messageInstance).getTargetProcess();
        doReturn(MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME).when(messageInstance).getTargetFlowNode();
        doReturn(WAITING_MESSAGE_EVENT_TYPE).when(waitingMessageEvent).getEventType();
        super.before();
    }

    @Test
    public void handleFailure() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("MESSAGE_INSTANCE_NAME=" + MESSAGE_INSTANCE_NAME));
        assertTrue(e.getMessage().contains("MESSAGE_INSTANCE_TARGET_PROCESS_NAME=" + MESSAGE_INSTANCE_TARGET_PROCESS_NAME));
        assertTrue(e.getMessage().contains("MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME=" + MESSAGE_INSTANCE_TARGET_FLOW_NODE_NAME));
        assertTrue(e.getMessage().contains("WAITING_MESSAGE_INSTANCE_TYPE=" + WAITING_MESSAGE_EVENT_TYPE.name()));
        verify(wrappedWork).handleFailure(e, context);
    }
}
