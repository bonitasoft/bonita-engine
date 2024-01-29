/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.transition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.*;
import static org.bonitasoft.engine.execution.TestFlowNodeState.*;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.junit.Before;
import org.junit.Test;

public class FlowNodeStateSequencesTest {

    public static final int NORMAL_STATE_1 = 1;
    public static final int NORMAL_STATE_2 = 2;
    public static final int CANCEL_STATE_1 = 3;
    public static final int CANCEL_STATE_2 = 4;
    public static final int ABORT_STATE_1 = 5;
    public static final int ABORT_STATE_2 = 6;
    private final FlowNodeStateSequences flowNodeStateSequences = new FlowNodeStateSequences() {

        @Override
        public SFlowNodeType getFlowNodeType() {
            return SFlowNodeType.AUTOMATIC_TASK;
        }
    };

    @Before
    public void before() {
        flowNodeStateSequences.defineNormalSequence(normalState(NORMAL_STATE_1, NORMAL),
                terminalState(NORMAL_STATE_2, NORMAL));
        flowNodeStateSequences.defineCancelSequence(normalState(CANCEL_STATE_1, CANCELLING),
                terminalState(CANCEL_STATE_2, CANCELLING));
        flowNodeStateSequences.defineAbortSequence(normalState(ABORT_STATE_1, ABORTING),
                terminalState(ABORT_STATE_2, ABORTING));
    }

    @Test
    public void should_give_the_first_state_of_normal_branch() {
        assertThat(flowNodeStateSequences.getFirstState(NORMAL).getId()).isEqualTo(NORMAL_STATE_1);
    }

    @Test
    public void should_give_the_first_state_of_abort_branch() {
        assertThat(flowNodeStateSequences.getFirstState(CANCELLING).getId()).isEqualTo(CANCEL_STATE_1);
    }

    @Test
    public void should_give_the_first_state_of_cancel_branch() {
        assertThat(flowNodeStateSequences.getFirstState(ABORTING).getId()).isEqualTo(ABORT_STATE_1);
    }

    @Test
    public void should_give_the_next_of_normal_branch() {
        assertThat(flowNodeStateSequences.getStateAfter(NORMAL, NORMAL_STATE_1).getId())
                .isEqualTo(NORMAL_STATE_2);
    }

    @Test
    public void should_give_the_next_of_abort_branch() {
        assertThat(flowNodeStateSequences.getStateAfter(CANCELLING, CANCEL_STATE_1).getId())
                .isEqualTo(CANCEL_STATE_2);
    }

    @Test
    public void should_give_the_next_of_cancel_branch() {
        assertThat(flowNodeStateSequences.getStateAfter(ABORTING, ABORT_STATE_1).getId())
                .isEqualTo(ABORT_STATE_2);
    }

    @Test
    public void should_give_null_after_the_last_state() {
        assertThat(flowNodeStateSequences.getStateAfter(NORMAL, NORMAL_STATE_2)).isNull();
    }

    @Test
    public void should_give_null_when_state_does_not_exists() {
        assertThat(flowNodeStateSequences.getStateAfter(NORMAL, 9999)).isNull();
    }

    @Test
    public void should_give_null_if_state_does_not_exists_in_current_state_category() {
        assertThat(flowNodeStateSequences.getStateAfter(ABORTING, NORMAL_STATE_1)).isNull();

    }

}
