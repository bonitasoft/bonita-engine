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

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.TransactionContainedProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class EndingCallActivityExceptionStateImpl implements FlowNodeState {

    private final ActivityInstanceService activityInstanceService;

    private final ProcessInstanceService processInstanceService;

    private final ContainerRegistry containerRegistry;

    private final ArchiveService archiveService;

    private final SCommentService commentService;

    private final DataInstanceService dataInstanceService;

    private final DocumentService documentService;

    private final TechnicalLoggerService logger;

    private final ProcessDefinitionService processDefinitionService;

    private final ConnectorInstanceService connectorInstanceService;
    private ClassLoaderService classLoaderService;

    public EndingCallActivityExceptionStateImpl(final ActivityInstanceService activityInstanceService,
            final ProcessInstanceService processInstanceService, final ContainerRegistry containerRegistry, final ArchiveService archiveService,
            final SCommentService commentService, final DataInstanceService dataInstanceService,
            final DocumentService documentService, final TechnicalLoggerService logger, final ProcessDefinitionService processDefinitionService,
                                                final ConnectorInstanceService connectorInstanceService, ClassLoaderService classLoaderService) {
        super();
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.containerRegistry = containerRegistry;
        this.archiveService = archiveService;
        this.commentService = commentService;
        this.dataInstanceService = dataInstanceService;
        this.documentService = documentService;
        this.logger = logger;
        this.processDefinitionService = processDefinitionService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        try {
            final SCallActivityInstance callActivity = (SCallActivityInstance) flowNodeInstance;
            final boolean hasActiveChild = callActivity.getTokenCount() > 0;
            if (hasActiveChild) {
                final SProcessInstance targetProcessInstance = processInstanceService.getChildOfActivity(flowNodeInstance.getId());
                final TransactionContainedProcessInstanceInterruptor processInstanceInterruptor = new TransactionContainedProcessInstanceInterruptor(
                        processInstanceService, activityInstanceService, containerRegistry, logger);
                processInstanceInterruptor.interruptProcessInstance(targetProcessInstance.getId(), getStateCategory(), -1);
            } else {
                archiveChildProcessInstance(flowNodeInstance);
            }
            return hasActiveChild;
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance instance) throws SActivityStateExecutionException {
        // archive process target process instance
        try {
            archiveChildProcessInstance(instance);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("Unable to found the process instance called by call activity with id " + instance.getId(), e);
        }
        return StateCode.DONE;
    }

    protected void archiveChildProcessInstance(final SFlowNodeInstance instance) throws SProcessInstanceNotFoundException, SArchivingException,
            SBonitaReadException {
        final SProcessInstance childProcInst = processInstanceService.getChildOfActivity(instance.getId());
        ProcessArchiver.archiveProcessInstance(childProcInst, archiveService, processInstanceService, dataInstanceService, documentService, logger,
                commentService, processDefinitionService, connectorInstanceService, classLoaderService);
    }

    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
        return true;
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

}
