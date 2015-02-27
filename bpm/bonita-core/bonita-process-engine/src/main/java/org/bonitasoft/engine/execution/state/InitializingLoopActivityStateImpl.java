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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SStandardLoopCharacteristics;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
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
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class InitializingLoopActivityStateImpl implements FlowNodeState {

    private final ExpressionResolverService expressionResolverService;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final ActivityInstanceService activityInstanceService;

    private final StateBehaviors stateBehaviors;

    public InitializingLoopActivityStateImpl(final ExpressionResolverService expressionResolverService, final BPMInstancesCreator bpmInstancesCreator,
            final ActivityInstanceService activityInstanceService, final StateBehaviors stateBehaviors) {
        this.expressionResolverService = expressionResolverService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.activityInstanceService = activityInstanceService;
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        stateBehaviors.createAttachedBoundaryEvents(processDefinition, (SActivityInstance) flowNodeInstance);
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final Long processDefinitionId = processDefinition.getId();
        final SActivityDefinition activity = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        try {
            final SLoopActivityInstance loopActivity = (SLoopActivityInstance) activityInstanceService.getFlowNodeInstance(flowNodeInstance.getId());
            final SLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
            if (loopCharacteristics instanceof SStandardLoopCharacteristics) {
                final SStandardLoopCharacteristics standardLoop = (SStandardLoopCharacteristics) loopCharacteristics;
                final SExpression loopMax = ((SStandardLoopCharacteristics) loopCharacteristics).getLoopMax();
                Integer intLoopMax;

                if (loopMax != null) {
                    intLoopMax = (Integer) expressionResolverService.evaluate(loopMax, new SExpressionContext(loopActivity.getId(),
                            DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                            processDefinitionId));
                    activityInstanceService.setLoopMax(loopActivity, intLoopMax);
                }
                final boolean loop = !standardLoop.isTestBefore() || evaluateLoop(standardLoop, loopActivity);
                if (loop) {
                    final SLoopActivityInstanceBuilderFactory keyProvider = BuilderFactory.get(SLoopActivityInstanceBuilderFactory.class);
                    final long rootProcessInstanceId = flowNodeInstance.getLogicalGroup(keyProvider.getRootProcessInstanceIndex());
                    final long parentProcessInstanceId = flowNodeInstance.getLogicalGroup(keyProvider.getParentProcessInstanceIndex());
                    bpmInstancesCreator.createFlowNodeInstance(processDefinitionId, flowNodeInstance.getRootContainerId(), flowNodeInstance.getId(),
                            SFlowElementsContainerType.FLOWNODE, activity, rootProcessInstanceId, parentProcessInstanceId, true, 1, SStateCategory.NORMAL, -1
                    );
                    activityInstanceService.incrementLoopCounter(loopActivity);
                    activityInstanceService.setTokenCount(loopActivity, loopActivity.getTokenCount() + 1);
                }
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
        return StateCode.DONE;
    }

    private boolean evaluateLoop(final SStandardLoopCharacteristics standardLoop, final SLoopActivityInstance loopActivity)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final Map<String, Object> input = new HashMap<String, Object>(1);
        input.put(ExpressionConstants.LOOP_COUNTER.getEngineConstantName(), loopActivity.getLoopCounter());
        final SExpressionContext sExpressionContext = new SExpressionContext(loopActivity.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), input);
        return ((Boolean) expressionResolverService.evaluate(standardLoop.getLoopCondition(), sExpressionContext)).booleanValue();
    }

    @Override
    public int getId() {
        return 23;
    }

    @Override
    public boolean isInterrupting() {
        return false;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String getName() {
        return "initializing";
    }

    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
        return true;
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
