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

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class CompletedActivityStateImpl implements FlowNodeState {

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance activityInstance) {
        return StateCode.DONE;
        // this method is never called, use "completing" state
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public boolean isInterrupting() {
        return false;
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public String getName() {
        return "completed";
    }

    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
        return false;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return flowNodeInstance instanceof SHumanTaskInstance;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return " User " + flowNodeInstance.getExecutedBy() + " has " + getName() + " task " + flowNodeInstance.getName();
    }

}
