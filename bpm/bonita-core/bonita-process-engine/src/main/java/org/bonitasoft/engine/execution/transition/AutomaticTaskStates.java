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

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.execution.state.AbortedFlowNodeState;
import org.bonitasoft.engine.execution.state.AbortingActivityWithBoundaryState;
import org.bonitasoft.engine.execution.state.AbortingBoundaryEventsOnCompletingActivityState;
import org.bonitasoft.engine.execution.state.AbortingFlowNodeContainerState;
import org.bonitasoft.engine.execution.state.CancelledFlowNodeState;
import org.bonitasoft.engine.execution.state.CancellingActivityWithBoundaryState;
import org.bonitasoft.engine.execution.state.CancellingFlowNodeContainerChildrenState;
import org.bonitasoft.engine.execution.state.CompletedActivityState;
import org.bonitasoft.engine.execution.state.ExecutingAutomaticActivityState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AutomaticTaskStates extends FlowNodeStateSequences {

    public SFlowNodeType getFlowNodeType() {
        return SFlowNodeType.AUTOMATIC_TASK;
    }

    public AutomaticTaskStates(
            AbortingBoundaryEventsOnCompletingActivityState abortingBoundaryEventsOnCompletingActivityState,
            CompletedActivityState completed,
            AbortingActivityWithBoundaryState abortingActivityWithBoundary,
            AbortedFlowNodeState aborted,
            @Qualifier("cancellingActivityWithBoundaryState") CancellingActivityWithBoundaryState cancellingActivityWithBoundary,
            CancelledFlowNodeState cancelled,
            ExecutingAutomaticActivityState executingAutomaticActivity,
            CancellingFlowNodeContainerChildrenState cancellingContainer,
            @Qualifier("abortingFlowNodeContainerState") AbortingFlowNodeContainerState abortingContainer) {

        defineNormalSequence(executingAutomaticActivity, abortingBoundaryEventsOnCompletingActivityState,
                completed);
        defineAbortSequence(abortingActivityWithBoundary, abortingContainer, aborted);
        defineCancelSequence(cancellingActivityWithBoundary, cancellingContainer, cancelled);
    }

}
