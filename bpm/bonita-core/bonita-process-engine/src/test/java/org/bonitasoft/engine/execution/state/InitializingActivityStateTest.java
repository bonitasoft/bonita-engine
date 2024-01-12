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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializingActivityStateTest {

    @Mock
    private StateBehaviors stateBehaviors;

    @Mock
    private SProcessDefinition sProcessDefinition;

    @Mock
    private SFlowNodeInstance sFlowNodeInstance;

    @Test
    public void should_update_expected_duration() throws Exception {
        //given
        InitializingActivityState initializingActivityState = new InitializingActivityState(stateBehaviors);

        //when
        initializingActivityState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        //then
        verify(stateBehaviors).updateExpectedDuration(sProcessDefinition, sFlowNodeInstance);
    }

    @Test
    public void should_register_waiting_event_after_connectors() throws Exception {
        InitializingActivityState initializingActivityState = new InitializingActivityState(stateBehaviors);

        initializingActivityState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        verify(stateBehaviors).registerWaitingEvent(sProcessDefinition, sFlowNodeInstance);
    }
}
