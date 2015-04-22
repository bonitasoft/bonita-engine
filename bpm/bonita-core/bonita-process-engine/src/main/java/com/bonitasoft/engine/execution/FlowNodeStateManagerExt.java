/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.work.WorkService;

/**
 * SP implementation of the activity state manager.
 *
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class FlowNodeStateManagerExt extends FlowNodeStateManagerImpl {

    private final BreakpointService breakpointService;

    public FlowNodeStateManagerExt(final ProcessDefinitionService processDefinitionService, final ProcessInstanceService processInstanceService,
            final ActivityInstanceService activityInstanceService, final ConnectorInstanceService connectorInstanceService,
            final ExpressionResolverService expressionResolverService, final DataInstanceService dataInstanceService,
            final EventInstanceService eventInstanceService, final OperationService operationService,
            final BPMInstancesCreator bpmInstancesCreator, final ContainerRegistry containerRegistry, final ArchiveService archiveService,
            final TechnicalLoggerService logger, final DocumentService documentService, final SCommentService commentService,
            final EventsHandler eventsHandler, final UserFilterService userFilterService, final ActorMappingService actorMappingService,
            final WorkService workService, final IdentityService identityService, final BreakpointService breakpointService,
            final RefBusinessDataService refBusinessDataService, final ParentContainerResolver parentContainerResolver,
            final WaitingEventsInterrupter waitingEventsInterrupter, ClassLoaderService classLoaderService) {
        super(processDefinitionService, processInstanceService, activityInstanceService, connectorInstanceService, expressionResolverService,
                dataInstanceService, operationService, bpmInstancesCreator, containerRegistry, archiveService, logger, documentService, commentService,
                new StateBehaviors(bpmInstancesCreator, eventsHandler, activityInstanceService, userFilterService, classLoaderService, actorMappingService,
                        connectorInstanceService, expressionResolverService, processDefinitionService, dataInstanceService, operationService, workService,
                        containerRegistry, eventInstanceService, commentService, identityService, processInstanceService, parentContainerResolver,
                        waitingEventsInterrupter, logger, refBusinessDataService), waitingEventsInterrupter, classLoaderService);
        this.breakpointService = breakpointService;
    }

    @Override
    public FlowNodeState getNextNormalState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final int currentStateId)
            throws SActivityExecutionException {
        FlowNodeState flowNodeState = super.getNextNormalState(processDefinition, flowNodeInstance, currentStateId);
        flowNodeState = handleBreakPoints(processDefinition, flowNodeInstance, flowNodeState, getState(currentStateId), flowNodeState.getId());
        return flowNodeState;
    }

    private FlowNodeState handleBreakPoints(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final FlowNodeState flowNodeState, final FlowNodeState current, final int nextState) throws SActivityExecutionException {
        try {
            if (!current.isInterrupting() && breakpointService.isBreakpointActive()) {
                final SBreakpoint breakPointFor = breakpointService.getBreakPointFor(processDefinition.getId(), flowNodeInstance.getRootContainerId(),
                        flowNodeInstance.getName(), nextState);
                if (breakPointFor != null) {
                    final int interruptedStateId = breakPointFor.getInterruptedStateId();
                    return getState(interruptedStateId);
                }
            }
        } catch (final SBonitaReadException e) {
            throw new SActivityExecutionException(e);
        }
        return flowNodeState;
    }

}
