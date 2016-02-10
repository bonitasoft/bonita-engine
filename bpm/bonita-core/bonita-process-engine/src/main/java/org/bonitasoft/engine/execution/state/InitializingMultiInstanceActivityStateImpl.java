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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class InitializingMultiInstanceActivityStateImpl implements FlowNodeState {

    private final ExpressionResolverService expressionResolverService;

    private final ActivityInstanceService activityInstanceService;

    private final StateBehaviors stateBehaviors;

    public InitializingMultiInstanceActivityStateImpl(final ExpressionResolverService expressionResolverService,
            final ActivityInstanceService activityInstanceService, final StateBehaviors stateBehaviors) {
        this.expressionResolverService = expressionResolverService;
        this.activityInstanceService = activityInstanceService;
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        try {
            final SMultiInstanceActivityInstance multiInstanceActivityInstance = (SMultiInstanceActivityInstance) flowNodeInstance;
            stateBehaviors.createAttachedBoundaryEvents(processDefinition, multiInstanceActivityInstance);
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SActivityDefinition activity = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            final SLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
            if (loopCharacteristics instanceof SMultiInstanceLoopCharacteristics) {
                final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                final SExpression loopCardinality = miLoop.getLoopCardinality();
                int numberOfInstanceMax = -1;
                if (loopCardinality != null) {
                    numberOfInstanceMax = (Integer) expressionResolverService.evaluate(loopCardinality,
                            new SExpressionContext(multiInstanceActivityInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), processDefinition
                                    .getId()));
                    activityInstanceService.setLoopCardinality(multiInstanceActivityInstance, numberOfInstanceMax);
                } else if (miLoop.getLoopDataInputRef() != null) {
                    numberOfInstanceMax = stateBehaviors.getNumberOfInstancesToCreateFromInputRef(processDefinition, multiInstanceActivityInstance, miLoop,
                            numberOfInstanceMax);
                    stateBehaviors.updateOutputData(processDefinition, multiInstanceActivityInstance, miLoop, numberOfInstanceMax);
                }
                if (numberOfInstanceMax < 0) {
                    throw new SActivityStateExecutionException("The multi instance on activity " + flowNodeInstance.getName() + " of process "
                            + processDefinition.getName() + " " + processDefinition.getVersion() + " did not have loop cardinality nor loop data input ref set");
                }
                stateBehaviors.createInnerInstances(processDefinition.getId(), activity, multiInstanceActivityInstance, miLoop.isSequential() ? 1
                        : numberOfInstanceMax);
            }
        } catch (final SActivityStateExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
        return StateCode.DONE;
    }

    @Override
    public int getId() {
        return 27;
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
