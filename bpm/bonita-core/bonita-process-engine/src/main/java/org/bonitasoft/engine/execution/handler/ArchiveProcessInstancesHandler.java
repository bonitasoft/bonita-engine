/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.handler;

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class ArchiveProcessInstancesHandler implements SProcessInstanceHandler<SUpdateEvent> {

    private final ArchiveService archiveService;

    private ProcessInstanceService processInstanceService;

    private final BPMInstanceBuilders instancesBuilders;

    private final TechnicalLoggerService logger;

    private final DataInstanceService dataInstanceService;

    private final SDataInstanceBuilders sDataInstanceBuilders;

    private final DocumentMappingService documentMappingService;

    private final SCommentService commentService;

    private final SACommentBuilder saCommentBuilder;

    private final ProcessDefinitionService processDefinitionService;

    private final ConnectorInstanceService connectorInstanceService;

    public ArchiveProcessInstancesHandler(final ArchiveService archiveService, final BPMInstanceBuilders instancesBuilders,
            final TechnicalLoggerService logger, final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders,
            final DocumentMappingService documentMappingService, final SCommentService commentService, final SACommentBuilder saCommentBuilder,
            final ProcessDefinitionService processDefinitionService, final ConnectorInstanceService connectorInstanceService) {
        this.archiveService = archiveService;
        this.instancesBuilders = instancesBuilders;
        this.logger = logger;
        this.dataInstanceService = dataInstanceService;
        this.sDataInstanceBuilders = sDataInstanceBuilders;
        this.documentMappingService = documentMappingService;
        this.commentService = commentService;
        this.saCommentBuilder = saCommentBuilder;
        this.processDefinitionService = processDefinitionService;
        this.connectorInstanceService = connectorInstanceService;
    }

    @Override
    public void execute(final SUpdateEvent event) throws SHandlerExecutionException {
        final SProcessInstance processInstance = (SProcessInstance) event.getObject();
        try {
            ProcessArchiver.archiveProcessInstance(processInstance, archiveService, processInstanceService, dataInstanceService, documentMappingService,
                    logger, instancesBuilders, sDataInstanceBuilders, commentService, saCommentBuilder, processDefinitionService, connectorInstanceService);
        } catch (final SArchivingException e) {
            throw new SHandlerExecutionException(e);
        }
    }

    @Override
    public boolean isInterested(final SUpdateEvent event) {
        boolean isInterested = ProcessInstanceService.PROCESSINSTANCE_STATE_UPDATED.equals(event.getType()) && event.getObject() instanceof SProcessInstance;
        if (isInterested) {
            final SProcessInstance processInstance = (SProcessInstance) event.getObject();
            // TODO add a method isInTerminalState in SProcessInstance
            final boolean isTerminal = ProcessInstanceState.COMPLETED.getId() == processInstance.getStateId()
                    || ProcessInstanceState.ABORTED.getId() == processInstance.getStateId()
                    || ProcessInstanceState.CANCELLED.getId() == processInstance.getStateId();
            // process instances called by an call activity are archive in the state CompletingCallActivity (wait data transfer from called process to caller).
            // Sub-process can be archived directly
            isInterested = isTerminal && (processInstance.getCallerId() <= 0 || SFlowNodeType.SUB_PROCESS.equals(processInstance.getCallerType()));
        }
        return isInterested;
    }

    @Override
    public void setProcessInstanceService(final ProcessInstanceService processInstanceService) {
        this.processInstanceService = processInstanceService;
    }

}
