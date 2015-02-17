/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkService;

import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SFlowNodeSimpleRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;


public class StateBehaviorsExt extends StateBehaviors {

    private final RefBusinessDataService refBusinessDataService;

    public StateBehaviorsExt(final BPMInstancesCreator bpmInstancesCreator, final EventsHandler eventsHandler,
            final ActivityInstanceService activityInstanceService, final UserFilterService userFilterService, final ClassLoaderService classLoaderService,
            final ActorMappingService actorMappingService, final ConnectorInstanceService connectorInstanceService,
            final ExpressionResolverService expressionResolverService, final ProcessDefinitionService processDefinitionService,
            final DataInstanceService dataInstanceService, final OperationService operationService, final WorkService workService,
            final ContainerRegistry containerRegistry, final EventInstanceService eventInstanceSevice, final SchedulerService schedulerService,
            final SCommentService commentService, final IdentityService identityService, final TechnicalLoggerService logger, final ProcessInstanceService processInstanceService,
            final RefBusinessDataService refBusinessDataService, final ParentContainerResolver parentContainerResolver) {
        super(bpmInstancesCreator, eventsHandler, activityInstanceService, userFilterService, classLoaderService, actorMappingService,
                connectorInstanceService, expressionResolverService, processDefinitionService, dataInstanceService, operationService, workService,
                containerRegistry, eventInstanceSevice, schedulerService, commentService, identityService, logger, processInstanceService, parentContainerResolver);
        this.refBusinessDataService = refBusinessDataService;
    }

    @Override
    public int getNumberOfInstancesToCreateFromInputRef(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SMultiInstanceLoopCharacteristics miLoop, final int numberOfInstanceMax) throws SDataInstanceException, SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SBusinessDataDefinition businessData = processContainer.getBusinessDataDefinition(miLoop.getLoopDataInputRef());
        if (businessData == null) {
            return super.getNumberOfInstancesToCreateFromInputRef(processDefinition, flowNodeInstance, miLoop, numberOfInstanceMax);
        }
        try {
            return refBusinessDataService.getNumberOfDataOfMultiRefBusinessData(businessData.getName(), flowNodeInstance.getParentProcessInstanceId());
        } catch (final SBonitaReadException sbre) {
            throw new SActivityStateExecutionException(sbre);
        }
    }

    @Override
    public void updateOutputData(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SMultiInstanceLoopCharacteristics miLoop, final int numberOfInstanceMax) throws SDataInstanceException, SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SBusinessDataDefinition businessData = processContainer.getBusinessDataDefinition(miLoop.getLoopDataOutputRef());
        if (businessData == null) {
            super.updateOutputData(processDefinition, flowNodeInstance, miLoop, numberOfInstanceMax);
        }
    }

    @Override
    public boolean shouldCreateANewInstance(final SMultiInstanceLoopCharacteristics loopCharacteristics, final int numberOfInstances,
            final SMultiInstanceActivityInstance miActivityInstance) throws SDataInstanceException {
        if (loopCharacteristics.getLoopCardinality() != null) {
            return miActivityInstance.getLoopCardinality() > numberOfInstances;
        }
        List<?> possibleValues = null;
        try {
            //FIXME find if a business data is used if instead of try catch
            final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) refBusinessDataService.getRefBusinessDataInstance(
                    loopCharacteristics.getLoopDataInputRef(), miActivityInstance.getParentProcessInstanceId());
            possibleValues = multiRef.getDataIds();
        } catch (final SBonitaException sbe) {
            final SDataInstance dataInstance = getDataInstanceService().getDataInstance(loopCharacteristics.getLoopDataInputRef(),
                    miActivityInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), parentContainerResolver);
            possibleValues = (List<?>) dataInstance.getValue();
        }
        if (possibleValues != null) {
            return numberOfInstances < possibleValues.size();
        }
        return false;
    }

    @Override
    public void mapDataOutputOfMultiInstance(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        if (flowNodeInstance instanceof SActivityInstance && !SFlowNodeType.MULTI_INSTANCE_ACTIVITY.equals(flowNodeInstance.getType())) {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SActivityDefinition activityDefinition = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (activityDefinition != null) {// can be null if the activity was added in runtime
                try {
                    final SLoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
                    if (loopCharacteristics instanceof SMultiInstanceLoopCharacteristics
                            && ((SMultiInstanceLoopCharacteristics) loopCharacteristics).getDataOutputItemRef() != null) {
                        final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                        final SBusinessDataDefinition businessData = processContainer.getBusinessDataDefinition(miLoop.getLoopDataOutputRef());
                        if (businessData == null) {
                            super.mapDataOutputOfMultiInstance(flowNodeInstance, miLoop);
                        } else {
                            final SRefBusinessDataInstance outputMIRef = refBusinessDataService.getFlowNodeRefBusinessDataInstance(
                                    miLoop.getDataOutputItemRef(), flowNodeInstance.getId());
                            final SRefBusinessDataInstance outputMILoopRef = refBusinessDataService.getRefBusinessDataInstance(
                                    miLoop.getLoopDataOutputRef(), flowNodeInstance.getParentProcessInstanceId());
                            final SMultiRefBusinessDataInstance multiRefBusinessDataInstance = (SMultiRefBusinessDataInstance) outputMILoopRef;
                            List<Long> dataIds = multiRefBusinessDataInstance.getDataIds();
                            if (dataIds == null) {
                                dataIds = new ArrayList<Long>();
                            }
                            final Long dataId = ((SFlowNodeSimpleRefBusinessDataInstance) outputMIRef).getDataId();
                            dataIds.add(dataId);
                            refBusinessDataService.updateRefBusinessDataInstance(multiRefBusinessDataInstance, dataIds);
                        }
                    }
                } catch (final SBonitaException sbe) {
                    throw new SActivityStateExecutionException(sbe);
                }
            }
        }
    }

}
