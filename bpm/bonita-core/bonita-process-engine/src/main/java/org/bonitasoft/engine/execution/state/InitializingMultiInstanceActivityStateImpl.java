/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
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
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class InitializingMultiInstanceActivityStateImpl implements FlowNodeState {

    private final ExpressionResolverService expressionResolverService;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final ActivityInstanceService activityInstanceService;

    private final DataInstanceService dataInstanceService;

    private final StateBehaviors stateBehaviors;

    public InitializingMultiInstanceActivityStateImpl(final ExpressionResolverService expressionResolverService, final BPMInstancesCreator bpmInstancesCreator,
            final ActivityInstanceService activityInstanceService, final DataInstanceService dataInstanceService,
            final StateBehaviors stateBehaviors) {
        this.expressionResolverService = expressionResolverService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.activityInstanceService = activityInstanceService;
        this.dataInstanceService = dataInstanceService;
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        try {
            stateBehaviors.createAttachedBoundaryEvents(processDefinition, (SActivityInstance) flowNodeInstance);
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SActivityDefinition activity = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            final SMultiInstanceActivityInstance multiInstanceActivity = (SMultiInstanceActivityInstance) activityInstanceService
                    .getFlowNodeInstance(flowNodeInstance.getId());
            final SLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
            bpmInstancesCreator.addChildDataContainer(flowNodeInstance);
            if (loopCharacteristics instanceof SMultiInstanceLoopCharacteristics) {
                final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                final SExpression loopCardinality = miLoop.getLoopCardinality();
                int intLoopCardinality;
                int numberOfInstanceMax = -1;
                if (loopCardinality != null) {
                    intLoopCardinality = (Integer) expressionResolverService.evaluate(loopCardinality, new SExpressionContext(multiInstanceActivity.getId(),
                            DataInstanceContainer.ACTIVITY_INSTANCE.name(), processDefinition.getId()));
                    activityInstanceService.setLoopCardinality(flowNodeInstance, intLoopCardinality);
                    numberOfInstanceMax = intLoopCardinality;

                } else if (miLoop.getLoopDataInputRef() != null) {
                    final SDataInstance loopDataInput = dataInstanceService.getDataInstance(miLoop.getLoopDataInputRef(), flowNodeInstance.getId(),
                            DataInstanceContainer.ACTIVITY_INSTANCE.name());
                    if (loopDataInput != null) {
                        final Serializable value = loopDataInput.getValue();
                        if (value instanceof List) {
                            final List<?> loopDataInputCollection = (List<?>) value;
                            numberOfInstanceMax = loopDataInputCollection.size();
                            final String loopDataOutputRef = miLoop.getLoopDataOutputRef();
                            if (loopDataOutputRef != null) {
                                final SDataInstance loopDataOutput = dataInstanceService.getDataInstance(loopDataOutputRef, flowNodeInstance.getId(),
                                        DataInstanceContainer.ACTIVITY_INSTANCE.name());
                                if (loopDataOutput != null) {
                                    final Serializable outValue = loopDataOutput.getValue();
                                    if (outValue instanceof List) {
                                        final List<?> loopDataOutputCollection = (List<?>) outValue;
                                        if (loopDataOutputCollection.size() < numberOfInstanceMax) {
                                            // output data is too small
                                            final ArrayList<Object> newOutputList = new ArrayList<Object>(numberOfInstanceMax);
                                            newOutputList.addAll(loopDataOutputCollection);
                                            for (int i = loopDataOutputCollection.size(); i < numberOfInstanceMax; i++) {
                                                newOutputList.add(null);
                                            }
                                            final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
                                            final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
                                            updateDescriptor.addField(fact.getValueKey(), newOutputList);
                                            dataInstanceService.updateDataInstance(loopDataOutput, updateDescriptor);
                                        }
                                    } else if (outValue == null) {
                                        final ArrayList<Object> newOutputList = new ArrayList<Object>(numberOfInstanceMax);
                                        for (int i = 0; i < numberOfInstanceMax; i++) {
                                            newOutputList.add(null);
                                        }
                                        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
                                        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
                                        updateDescriptor.addField(fact.getValueKey(), newOutputList);
                                        dataInstanceService.updateDataInstance(loopDataOutput, updateDescriptor);
                                    } else {
                                        throw new SActivityStateExecutionException("The multi instance on activity " + flowNodeInstance.getName()
                                                + " of process " + processDefinition.getName() + " " + processDefinition.getVersion()
                                                + " have a loop data output which is not a java.util.List");
                                    }
                                }
                            }
                        } else {
                            throw new SActivityStateExecutionException("The multi instance on activity " + flowNodeInstance.getName() + " of process "
                                    + processDefinition.getName() + " " + processDefinition.getVersion()
                                    + " have a loop data input which is not a java.util.List");
                        }
                    }
                }
                if (numberOfInstanceMax < 0) {
                    throw new SActivityStateExecutionException("The multi instance on activity " + flowNodeInstance.getName() + " of process "
                            + processDefinition.getName() + " " + processDefinition.getVersion() + " did not have loop cardinality nor loop data input ref set");
                }
                createInnerInstances(bpmInstancesCreator, activityInstanceService, processDefinition.getId(), activity, flowNodeInstance,
                        miLoop.isSequential() ? 1 : numberOfInstanceMax);
            }
        } catch (final SActivityStateExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
        return StateCode.DONE;
    }

    static List<SFlowNodeInstance> createInnerInstances(final BPMInstancesCreator bpmInstancesCreator, final ActivityInstanceService activityInstanceService,
            final long processDefinitionId, final SActivityDefinition activity, final SFlowNodeInstance flowNodeInstance,
            final int numberOfInstanceToCreate) throws SBonitaException {
        final SMultiInstanceActivityInstanceBuilderFactory keyProvider = BuilderFactory.get(SMultiInstanceActivityInstanceBuilderFactory.class);
        final long rootProcessInstanceId = flowNodeInstance.getLogicalGroup(keyProvider.getRootProcessInstanceIndex());
        final long parentProcessInstanceId = flowNodeInstance.getLogicalGroup(keyProvider.getParentProcessInstanceIndex());
        int nbOfcreatedInstances = 0;
        final int nbOfInstances = ((SMultiInstanceActivityInstance) flowNodeInstance).getNumberOfInstances();
        final ArrayList<SFlowNodeInstance> createdInstances = new ArrayList<SFlowNodeInstance>();
        for (int i = nbOfInstances; i < nbOfInstances + numberOfInstanceToCreate; i++) {
            createdInstances.add(bpmInstancesCreator.createFlowNodeInstance(processDefinitionId, flowNodeInstance.getRootContainerId(),
                    flowNodeInstance.getId(), SFlowElementsContainerType.FLOWNODE, activity, rootProcessInstanceId, parentProcessInstanceId, true, i,
                    SStateCategory.NORMAL, -1, null));
            nbOfcreatedInstances++;
        }
        final SMultiInstanceActivityInstance multiInstanceActivityInstance = (SMultiInstanceActivityInstance) flowNodeInstance;
        activityInstanceService.addMultiInstanceNumberOfActiveActivities(multiInstanceActivityInstance, nbOfcreatedInstances);
        final int tokenCount = multiInstanceActivityInstance.getTokenCount() + nbOfcreatedInstances;
        activityInstanceService.setTokenCount(multiInstanceActivityInstance, tokenCount);
        return createdInstances;
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

    @SuppressWarnings("unused")
    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
        return true;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }
}
