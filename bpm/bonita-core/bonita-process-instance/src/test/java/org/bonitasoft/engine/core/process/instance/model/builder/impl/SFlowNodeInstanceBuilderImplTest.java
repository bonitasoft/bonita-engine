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

package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SAutomaticTaskInstanceImpl;
import org.junit.Test;

public class SFlowNodeInstanceBuilderImplTest {

    @Test
    public void setState_should_set_all_fields_related_to_state() throws Exception {
        //given
        SAutomaticTaskInstanceBuilderImpl builder = new SAutomaticTaskInstanceBuilderImpl(new SAutomaticTaskInstanceImpl());
        int stateId = 100;
        String stateName = "mockState";
        FlowNodeState state = mock(FlowNodeState.class);
        given(state.getId()).willReturn(stateId);
        given(state.getName()).willReturn(stateName);
        given(state.isTerminal()).willReturn(true);
        given(state.isStable()).willReturn(true);

        //when
        builder.setState(state);

        //then
        SAutomaticTaskInstance taskInstance = builder.done();
        assertThat(taskInstance.getStateId()).isEqualTo(stateId);
        assertThat(taskInstance.getStateName()).isEqualTo(stateName);
        assertThat(taskInstance.isStable()).isTrue();
        assertThat(taskInstance.isTerminal()).isTrue();
    }

}