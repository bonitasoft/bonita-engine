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
package org.bonitasoft.engine.execution.state;

import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public interface FlowNodeStateManager {

    FlowNodeState getNextNormalState(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance, int currentStateId)
            throws SActivityExecutionException;

    FlowNodeState getFailedState();// TODO get the old state?

    FlowNodeState getState(int stateId);

    Set<String> getSupportedState(FlowNodeType nodeType);

    void setProcessExecutor(ProcessExecutor processExecutor);

    StateBehaviors getStateBehaviors();

    public FlowNodeState getFirstState(SFlowNodeType nodeType);

}
