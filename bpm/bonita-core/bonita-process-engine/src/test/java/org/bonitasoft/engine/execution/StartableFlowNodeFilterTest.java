/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Vincent Elcrin
 * Date: 17/12/13
 * Time: 17:27
 */
@RunWith(MockitoJUnitRunner.class)
public class StartableFlowNodeFilterTest {

    @Mock
    private SFlowNodeDefinition flownode;

    @Mock
    SSubProcessDefinition subprocess;

    @Mock
    private STransitionDefinition transition;

    private StartableFlowNodeFilter filter = new StartableFlowNodeFilter();

    @Before
    public void setUp() throws Exception {
        doReturn(SFlowNodeType.SUB_PROCESS).when(subprocess).getType();

        doReturn(Collections.emptyList()).when(flownode).getIncomingTransitions();
        doReturn(Collections.emptyList()).when(subprocess).getIncomingTransitions();
    }

    @Test
    public void select_should_return_false_if_flow_node_contains_incoming_transitions() throws Exception {
        doReturn(Arrays.asList(transition)).when(flownode).getIncomingTransitions();

        boolean result = filter.select(flownode);

        assertFalse(result);
    }

    @Test
    public void select_should_return_true_if_flow_node_doesnt_have_any_incoming_transition() throws Exception {
        assertTrue(filter.select(flownode));
    }

    @Test
    public void select_should_return_false_if_flow_node_is_an_event_sub_process() throws Exception {
        doReturn(true).when(subprocess).isTriggeredByEvent();

        assertFalse(filter.select(subprocess));
    }

}
