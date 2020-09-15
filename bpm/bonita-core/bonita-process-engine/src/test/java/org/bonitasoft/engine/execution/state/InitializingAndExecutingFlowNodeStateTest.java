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
package org.bonitasoft.engine.execution.state;

import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitializingAndExecutingFlowNodeStateTest {

    @Mock
    private StateBehaviors stateBehaviors;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Mock
    private SProcessDefinition processDefinition;

    @InjectMocks
    private InitializingAndExecutingFlowNodeState initializingAndExecutingFlowNodeState;

    @Test
    public void testBeforeOnEnter() throws Exception {

        // when
        initializingAndExecutingFlowNodeState.beforeOnEnter(processDefinition, flowNodeInstance);

        // then
        verify(stateBehaviors).createData(processDefinition, flowNodeInstance);
        verify(stateBehaviors).mapActors(flowNodeInstance, processDefinition.getProcessContainer());
    }

    @Test
    public void testOnEnterToOnFinish() throws Exception {

        // when
        initializingAndExecutingFlowNodeState.onEnterToOnFinish(processDefinition, flowNodeInstance);

        // then
        verify(stateBehaviors).updateDisplayNameAndDescription(processDefinition, flowNodeInstance);
        verify(stateBehaviors).handleCallActivity(processDefinition, flowNodeInstance);
    }

    @Test
    public void testAfterOnFinish() throws Exception {

        // when
        initializingAndExecutingFlowNodeState.afterOnFinish(processDefinition, flowNodeInstance);

        // then
        verify(stateBehaviors).updateDisplayDescriptionAfterCompletion(processDefinition, flowNodeInstance);
    }

}
