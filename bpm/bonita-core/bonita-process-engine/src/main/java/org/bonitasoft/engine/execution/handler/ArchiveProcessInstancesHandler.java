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
package org.bonitasoft.engine.execution.handler;

import java.util.UUID;

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ArchiveProcessInstancesHandler implements SProcessInstanceHandler<SUpdateEvent> {

    private static final long serialVersionUID = 1L;

    private final long tenantId;

    private final String identifier;

    public ArchiveProcessInstancesHandler(final long tenantId) {
        this(tenantId,  UUID.randomUUID().toString());
    }

    public ArchiveProcessInstancesHandler(final long tenantId, final String identifier) {
        this.tenantId = tenantId;
        this.identifier = identifier;
    }

    @Override
    public void execute(final SUpdateEvent event) throws SHandlerExecutionException {
        final SProcessInstance processInstance = (SProcessInstance) event.getObject();
        try {
            TenantServiceAccessor tenantServiceAccessor;
            tenantServiceAccessor = getTenantServiceAccessor();
            final ArchiveService archiveService = tenantServiceAccessor.getArchiveService();
            final ProcessInstanceService processInstanceService = tenantServiceAccessor.getProcessInstanceService();
            final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
            final DataInstanceService dataInstanceService = tenantServiceAccessor.getDataInstanceService();
            final DocumentService documentService = tenantServiceAccessor.getDocumentService();
            final SCommentService commentService = tenantServiceAccessor.getCommentService();
            final ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
            final ConnectorInstanceService connectorInstanceService = tenantServiceAccessor.getConnectorInstanceService();
            ClassLoaderService classLoaderService = tenantServiceAccessor.getClassLoaderService();

            ProcessArchiver.archiveProcessInstance(processInstance, archiveService, processInstanceService, dataInstanceService, documentService,
                    logger, commentService, processDefinitionService, connectorInstanceService, classLoaderService);
        } catch (final SArchivingException e) {
            throw new SHandlerExecutionException(e);
        } catch (SBonitaException e) {
            throw new SHandlerExecutionException(e);
        }
    }

    /**
     * @return tenantServiceAccessor
     * @throws SHandlerExecutionException
     */
    private TenantServiceAccessor getTenantServiceAccessor() throws SHandlerExecutionException {
        try {
            ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            return serviceAccessorFactory.createTenantServiceAccessor(tenantId);
        } catch (Exception e) {
            throw new SHandlerExecutionException(e.getMessage(), null);
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
    public String getIdentifier() {
        return identifier;
    }

}
