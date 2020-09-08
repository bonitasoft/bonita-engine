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

import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.ABORTING;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ExecutingMultiInstanceActivityStateImpl implements FlowNodeState {

    private final ExpressionResolverService expressionResolverService;

    private final ContainerRegistry containerRegistry;

    private final ActivityInstanceService activityInstanceService;

    private final StateBehaviors stateBehaviors;

    public ExecutingMultiInstanceActivityStateImpl(final ExpressionResolverService expressionResolverService,
            final ContainerRegistry containerRegistry,
            final ActivityInstanceService activityInstanceService, final StateBehaviors stateBehaviors) {
        this.expressionResolverService = expressionResolverService;
        this.containerRegistry = containerRegistry;
        this.activityInstanceService = activityInstanceService;
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public int getId() {
        return 28;
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
        return "executing";
    }

    @Override
    public boolean notifyChildFlowNodeHasFinished(final SProcessDefinition processDefinition,
            final SFlowNodeInstance parentInstance,
            final SFlowNodeInstance childInstance)
            throws SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SActivityDefinition activityDefinition = (SActivityDefinition) processContainer
                .getFlowNode(parentInstance.getFlowNodeDefinitionId());
        final SMultiInstanceLoopCharacteristics loopCharacteristics = (SMultiInstanceLoopCharacteristics) activityDefinition
                .getLoopCharacteristics();

        try {
            if (parentInstance.getStateCategory() != SStateCategory.NORMAL) {
                // if is not a normal state (aborting / canceling), return true to change state from executing to aborting / cancelling (ChildReachstate),
                // without create a new child task
                return true;
            }

            final SMultiInstanceActivityInstance parentMultiInstance = (SMultiInstanceActivityInstance) parentInstance;
            if (childInstance.isAborting() || childInstance.isCanceling()) {
                // TODO add synchronization
                activityInstanceService.addMultiInstanceNumberOfTerminatedActivities(parentMultiInstance, 1);
            } else {
                // TODO add synchronization
                activityInstanceService.addMultiInstanceNumberOfCompletedActivities(parentMultiInstance, 1);
                // check the completionCondition
                final SExpression completionCondition = loopCharacteristics.getCompletionCondition();
                final Map<String, Object> input = new HashMap<>(1);
                input.put(ExpressionConstants.NUMBER_OF_ACTIVE_INSTANCES.getEngineConstantName(),
                        parentMultiInstance.getNumberOfActiveInstances());
                input.put(ExpressionConstants.NUMBER_OF_TERMINATED_INSTANCES.getEngineConstantName(),
                        parentMultiInstance.getNumberOfTerminatedInstances());
                input.put(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName(),
                        parentMultiInstance.getNumberOfCompletedInstances());
                final int numberOfInstances = parentMultiInstance.getNumberOfInstances();
                input.put(ExpressionConstants.NUMBER_OF_INSTANCES.getEngineConstantName(), numberOfInstances);
                final SExpressionContext sExpressionContext = new SExpressionContext(parentMultiInstance.getId(),
                        DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId(), input);
                sExpressionContext.setProcessDefinitionId(parentMultiInstance.getProcessDefinitionId());
                if (completionCondition != null) {
                    final boolean complete = (Boolean) expressionResolverService.evaluate(completionCondition,
                            sExpressionContext);
                    if (complete) {
                        stateBehaviors.interruptSubActivities(parentMultiInstance, ABORTING);
                        if (parentMultiInstance.isSequential()) {
                            return true;
                        }
                    }
                }
            }

            final int numberOfActiveInstances = parentMultiInstance.getNumberOfActiveInstances();
            final int numberOfCompletedInstances = parentMultiInstance.getNumberOfCompletedInstances();
            final int numberOfTerminatedInstances = parentMultiInstance.getNumberOfTerminatedInstances();
            final int numberOfInstances = parentMultiInstance.getNumberOfInstances();
            if (parentMultiInstance.isSequential()) {
                // only instantiate when we are in sequence
                List<SFlowNodeInstance> createInnerInstances = null;
                if (stateBehaviors.shouldCreateANewInstance(loopCharacteristics, numberOfInstances,
                        parentMultiInstance)) {
                    createInnerInstances = stateBehaviors.createInnerInstances(processDefinition.getId(),
                            activityDefinition, parentMultiInstance, 1);
                    for (final SFlowNodeInstance sFlowNodeInstance : createInnerInstances) {
                        containerRegistry.executeFlowNode(sFlowNodeInstance);
                    }
                }
                return numberOfActiveInstances == 0
                        && (createInnerInstances == null || createInnerInstances.size() == 0);
            }
            return numberOfActiveInstances == 0
                    || numberOfInstances == numberOfCompletedInstances + numberOfTerminatedInstances;
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        final int numberOfActiveInstances = ((SMultiInstanceActivityInstance) flowNodeInstance)
                .getNumberOfActiveInstances();
        if (numberOfActiveInstances > 0) {
            stateBehaviors.executeChildrenActivities(flowNodeInstance);
        }
        return numberOfActiveInstances > 0;
    }

    @Override
    public final StateCode execute(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) {
        return StateCode.DONE;
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
