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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
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
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

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

    public ExecutingMultiInstanceActivityStateImpl(final ExpressionResolverService expressionResolverService, final ContainerRegistry containerRegistry,
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
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final SFlowNodeInstance childInstance)
            throws SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SActivityDefinition activityDefinition = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        final SMultiInstanceLoopCharacteristics loopCharacteristics = (SMultiInstanceLoopCharacteristics) activityDefinition.getLoopCharacteristics();

        try {
            final SMultiInstanceActivityInstance miActivity = (SMultiInstanceActivityInstance) flowNodeInstance;
            if (miActivity.getStateCategory() != SStateCategory.NORMAL) {
                // if is not a normal state (aborting / canceling), return true to change state from executing to aborting / cancelling (ChildReadstate),
                // without create a new child task
                return true;
            }

            if (childInstance.isAborting() || childInstance.isCanceling()) {
                // TODO add synchronization
                activityInstanceService.addMultiInstanceNumberOfTerminatedActivities(miActivity, 1);
            } else {
                // TODO add synchronization
                activityInstanceService.addMultiInstanceNumberOfCompletedActivities(miActivity, 1);
                // check the completionCondition
                final SExpression completionCondition = loopCharacteristics.getCompletionCondition();
                final Map<String, Object> input = new HashMap<String, Object>(1);
                input.put(ExpressionConstants.NUMBER_OF_ACTIVE_INSTANCES.getEngineConstantName(), miActivity.getNumberOfActiveInstances());
                input.put(ExpressionConstants.NUMBER_OF_TERMINATED_INSTANCES.getEngineConstantName(), miActivity.getNumberOfTerminatedInstances());
                input.put(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName(), miActivity.getNumberOfCompletedInstances());
                final int numberOfInstances = miActivity.getNumberOfInstances();
                input.put(ExpressionConstants.NUMBER_OF_INSTANCES.getEngineConstantName(), numberOfInstances);
                final SExpressionContext sExpressionContext = new SExpressionContext(miActivity.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), input);
                sExpressionContext.setProcessDefinitionId(miActivity.getProcessDefinitionId());
                if (completionCondition != null) {
                    final boolean complete = (Boolean) expressionResolverService.evaluate(completionCondition, sExpressionContext);
                    if (complete) {
                        abortNonCompletedChildren(miActivity);
                        if (miActivity.isSequential()) {
                            return true;
                        }
                    }
                }
            }

            final int numberOfActiveInstances = miActivity.getNumberOfActiveInstances();
            final int numberOfCompletedInstances = miActivity.getNumberOfCompletedInstances();
            final int numberOfTerminatedInstances = miActivity.getNumberOfTerminatedInstances();
            final int numberOfInstances = miActivity.getNumberOfInstances();
            if (miActivity.isSequential()) {
                // only instantiate when we are in sequence
                List<SFlowNodeInstance> createInnerInstances = null;
                if (stateBehaviors.shouldCreateANewInstance(loopCharacteristics, numberOfInstances, miActivity)) {
                    createInnerInstances = stateBehaviors.createInnerInstances(processDefinition.getId(), activityDefinition, miActivity, 1);
                    for (final SFlowNodeInstance sFlowNodeInstance : createInnerInstances) {
                        containerRegistry.executeFlowNode(processDefinition.getId(), sFlowNodeInstance.getLogicalGroup(3), sFlowNodeInstance.getId(), null,
                                null);
                    }
                }
                return numberOfActiveInstances == 0 && (createInnerInstances == null || createInnerInstances.size() == 0);
            }
            return numberOfActiveInstances == 0 || numberOfInstances == numberOfCompletedInstances + numberOfTerminatedInstances;
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    private boolean abortNonCompletedChildren(final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        final int numberOfResults = 100;
        long count = 0;
        List<SActivityInstance> children;
        boolean hasChildren = false;
        final SUserTaskInstanceBuilderFactory keyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        do {
            final OrderByOption orderByOption = new OrderByOption(SActivityInstance.class, keyProvider.getNameKey(), OrderByType.ASC);
            final List<FilterOption> filters = new ArrayList<FilterOption>(2);
            filters.add(new FilterOption(SActivityInstance.class, keyProvider.getParentActivityInstanceKey(), flowNodeInstance.getId()));
            filters.add(new FilterOption(SActivityInstance.class, keyProvider.getTerminalKey(), false));
            filters.add(new FilterOption(SActivityInstance.class, keyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));
            final QueryOptions queryOptions = new QueryOptions(0, numberOfResults, Collections.singletonList(orderByOption), filters, null);
            final QueryOptions countOptions = new QueryOptions(0, numberOfResults, null, filters, null);
            children = activityInstanceService.searchActivityInstances(SActivityInstance.class, queryOptions);
            count = activityInstanceService.getNumberOfActivityInstances(SActivityInstance.class, countOptions);
            if (count > 0) {
                hasChildren = true;
            }
            for (final SActivityInstance child : children) {
                activityInstanceService.setStateCategory(child, SStateCategory.ABORTING);
                if (child.isStable()) {
                    containerRegistry.executeFlowNode(flowNodeInstance.getProcessDefinitionId(), child.getLogicalGroup(3), child.getId(), null, null);
                }
            }

        } while (count > children.size());
        return hasChildren;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        final int numberOfActiveInstances = ((SMultiInstanceActivityInstance) flowNodeInstance).getNumberOfActiveInstances();
        if (numberOfActiveInstances > 0) {
            stateBehaviors.executeChildrenActivities(flowNodeInstance);
        }
        return numberOfActiveInstances > 0;
    }

    @Override
    public final StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
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
