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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SStandardLoopCharacteristics;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SLoopActivityInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.springframework.stereotype.Component;

@Component
public class ExecutingLoopActivityState implements FlowNodeState {

    private final ExpressionResolverService expressionResolverService;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final ContainerRegistry containerRegistry;

    private final ActivityInstanceService activityInstanceService;

    public ExecutingLoopActivityState(final ExpressionResolverService expressionResolverService,
            final BPMInstancesCreator bpmInstancesCreator,
            final ContainerRegistry containerRegistry, final ActivityInstanceService activityInstanceService) {
        this.expressionResolverService = expressionResolverService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.containerRegistry = containerRegistry;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
        return StateCode.DONE;
    }

    @Override
    public int getId() {
        return 24;
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
        try {
            final SLoopActivityInstance loopActivity = (SLoopActivityInstance) activityInstanceService
                    .getFlowNodeInstance(parentInstance.getId());// get it
            if (loopActivity.getStateCategory() != SStateCategory.NORMAL) {
                // if is not a normal state (aborting / canceling), return true to change state from executing to aborting / cancelling (ChildReadstate),
                // without create a new child task
                return true;
            }
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SActivityDefinition activity = (SActivityDefinition) processContainer
                    .getFlowNode(parentInstance.getFlowNodeDefinitionId());
            final SStandardLoopCharacteristics loopCharacteristics = (SStandardLoopCharacteristics) activity
                    .getLoopCharacteristics();
            boolean loop = false;
            final int loopCounter = loopActivity.getLoopCounter();
            if (loopActivity.getLoopMax() > 0 && loopCounter >= loopActivity.getLoopMax()) {
                return true;
            }

            final SStandardLoopCharacteristics standardLoop = loopCharacteristics;
            final Map<String, Object> input = new HashMap<>(1);
            input.put(ExpressionConstants.LOOP_COUNTER.getEngineConstantName(), loopActivity.getLoopCounter());
            final SExpressionContext sExpressionContext = new SExpressionContext(loopActivity.getId(),
                    DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                    loopActivity.getProcessDefinitionId(), input);
            loop = (Boolean) expressionResolverService.evaluate(standardLoop.getLoopCondition(), sExpressionContext);
            if (loop) {
                final SLoopActivityInstanceBuilderFactory keyProvider = BuilderFactory
                        .get(SLoopActivityInstanceBuilderFactory.class);
                final long rootProcessInstanceId = parentInstance
                        .getLogicalGroup(keyProvider.getRootProcessInstanceIndex());
                final long parentProcessInstanceId = parentInstance
                        .getLogicalGroup(keyProvider.getParentProcessInstanceIndex());
                final SFlowNodeInstance child = bpmInstancesCreator.createFlowNodeInstance(processDefinition.getId(),
                        parentInstance.getRootContainerId(),
                        parentInstance.getId(), SFlowElementsContainerType.FLOWNODE, activity, rootProcessInstanceId,
                        parentProcessInstanceId, true,
                        loopCounter + 1, SStateCategory.NORMAL, -1);
                activityInstanceService.incrementLoopCounter(loopActivity);
                activityInstanceService.setTokenCount(loopActivity, loopActivity.getTokenCount() + 1);
                containerRegistry.executeFlowNode(child);
            }
            return !loop;
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        List<SActivityInstance> childrenOfAnActivity;
        try {
            childrenOfAnActivity = activityInstanceService.getChildrenOfAnActivity(flowNodeInstance.getId(), 0, 1);
            if (!childrenOfAnActivity.isEmpty()) {
                containerRegistry.executeFlowNode(childrenOfAnActivity.get(0));
            }
            return !childrenOfAnActivity.isEmpty();
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
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
