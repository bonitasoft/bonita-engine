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
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlowNodeDefinitionAndInstanceContextWorkTest extends AbstractContextWorkTest {

    private static final long FLOW_NODE_DEFINITION_ID = 2;

    private static final long FLOW_NODE_INSTANCE_ID = 3;

    private static final String FLOW_NODE_NAME = "name";

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Override
    @Before
    public void before() throws SBonitaException {
        when(tenantAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);

        txBonitawork = spy(new FlowNodeDefinitionAndInstanceContextWork(wrappedWork, FLOW_NODE_INSTANCE_ID));

        doReturn(flowNodeInstance).when(activityInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);
        doReturn(FLOW_NODE_DEFINITION_ID).when(flowNodeInstance).getFlowNodeDefinitionId();
        doReturn(FLOW_NODE_NAME).when(flowNodeInstance).getName();
        super.before();
    }

    @Test
    public void handleFailure() throws Throwable {
        final Map<String, Object> context = Collections.<String, Object> singletonMap("tenantAccessor", tenantAccessor);
        final SBonitaException e = new SBonitaException() {

            private static final long serialVersionUID = -6748168976371554636L;
        };

        txBonitawork.handleFailure(e, context);

        assertTrue(e.getMessage().contains("FLOW_NODE_DEFINITION_ID=" + FLOW_NODE_DEFINITION_ID));
        assertTrue(e.getMessage().contains("FLOW_NODE_NAME=" + FLOW_NODE_NAME));
        assertTrue(e.getMessage().contains("FLOW_NODE_INSTANCE_ID=" + FLOW_NODE_INSTANCE_ID));
        verify(wrappedWork).handleFailure(e, context);
    }
}
