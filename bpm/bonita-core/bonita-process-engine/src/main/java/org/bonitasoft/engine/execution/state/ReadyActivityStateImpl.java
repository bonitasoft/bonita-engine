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
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ReadyActivityStateImpl extends FlowNodeStateWithConnectors {

    private final StateBehaviors stateBehaviors;

    public ReadyActivityStateImpl(final StateBehaviors stateBehaviors) {
        super(stateBehaviors, false, true);
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    protected void beforeOnEnter(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        if (((SHumanTaskInstance) flowNodeInstance).getAssigneeId() <= 0) {
            throw new SActivityStateExecutionException("The activity is not yet assigned, unable to execute it");
        }
    }

    @Override
    protected void onEnterToOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        stateBehaviors.executeOperations(processDefinition, (SActivityInstance) flowNodeInstance);
    }

    @Override
    protected void afterOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        stateBehaviors.mapDataOutputOfMultiInstance(processDefinition, flowNodeInstance);
        stateBehaviors.updateDisplayDescriptionAfterCompletion(processDefinition, flowNodeInstance);
    }

    @Override
    public int getId() {
        return 4;
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
        return false;
    }

    @Override
    public String getName() {
        return "ready";
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
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }
}
