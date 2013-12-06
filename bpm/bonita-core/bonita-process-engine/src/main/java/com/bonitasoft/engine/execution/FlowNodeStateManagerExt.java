/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkService;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;

/**
 * SP implementation of the activity state manager.
 * 
 * @author Celine Souchet
 */
public class FlowNodeStateManagerExt extends FlowNodeStateManagerImpl {

    private final BreakpointService breakpointService;

    public FlowNodeStateManagerExt(final ProcessDefinitionService processDefinitionService, final ProcessInstanceService processInstanceService,
            final ActivityInstanceService activityInstanceService, final ConnectorInstanceService connectorInstanceService,
            final ClassLoaderService classLoaderService, final ExpressionResolverService expressionResolverService, final SchedulerService schedulerService,
            final DataInstanceService dataInstanceService, final EventInstanceService eventInstanceService,
            final OperationService operationService, final BPMInstancesCreator bpmInstancesCreator,
            final ContainerRegistry containerRegistry, final ArchiveService archiveService, final TechnicalLoggerService logger,
            final DocumentMappingService documentMappingService, final SCommentService commentService,
            final BreakpointService breakpointService, final EventsHandler eventsHandler, final UserFilterService userFilterService,
            final ActorMappingService actorMappingService, final WorkService workService, final TokenService tokenService, final IdentityService identityService) {
        super(processDefinitionService, processInstanceService, activityInstanceService, connectorInstanceService, classLoaderService,
                expressionResolverService, schedulerService, dataInstanceService, eventInstanceService,
                operationService, bpmInstancesCreator, containerRegistry, archiveService, logger, documentMappingService, commentService,
                eventsHandler, userFilterService, actorMappingService, workService, tokenService, identityService);
        this.breakpointService = breakpointService;
    }

    @Override
    public FlowNodeState getNextNormalState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final int currentState)
            throws SActivityExecutionException {
        FlowNodeState flowNodeState;
        final Map<Integer, FlowNodeState> normalTransition = normalTransitions.get(flowNodeInstance.getType());
        final Map<Integer, FlowNodeState> flowNodeAbortTransitions = abortTransitions.get(flowNodeInstance.getType());
        final Map<Integer, FlowNodeState> flowNodeCancelTransitions = cancelTransitions.get(flowNodeInstance.getType());
        final FlowNodeState current = getState(currentState);
        int nextState;
        if (current.isInterrupting()) {
            final int previousStateId = flowNodeInstance.getPreviousStateId();
            nextState = states.get(previousStateId).getId();
        } else {
            nextState = currentState;
        }
        do {
            switch (flowNodeInstance.getStateCategory()) {
                case ABORTING:
                    flowNodeState = flowNodeAbortTransitions.get(nextState);
                    // next state should be aborting
                    if (flowNodeState == null) {
                        flowNodeState = flowNodeAbortTransitions.get(-1);
                    }

                    break;
                case CANCELLING:
                    flowNodeState = flowNodeCancelTransitions.get(nextState);
                    // next state should be canceling
                    if (flowNodeState == null) {
                        flowNodeState = flowNodeCancelTransitions.get(-1);
                    }
                    break;

                default:
                    flowNodeState = normalTransition.get(nextState);
                    break;
            }
            if (flowNodeState == null) {
                throw new SActivityExecutionException("no state found after " + states.get(currentState).getClass() + " for " + flowNodeInstance.getClass()
                        + " in state category " + flowNodeInstance.getStateCategory() + " activity id=" + flowNodeInstance.getId());
            }
            nextState = flowNodeState.getId();
        } while (!flowNodeState.shouldExecuteState(processDefinition, flowNodeInstance));
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
