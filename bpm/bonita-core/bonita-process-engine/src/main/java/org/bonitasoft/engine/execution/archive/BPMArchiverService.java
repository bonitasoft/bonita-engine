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
package org.bonitasoft.engine.execution.archive;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.model.*;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.*;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@Slf4j
public class BPMArchiverService {

    private final ArchiveService archiveService;
    private final ProcessInstanceService processInstanceService;
    private final DocumentService documentService;
    private final SCommentService commentService;
    private final ProcessDefinitionService processDefinitionService;
    private final ConnectorInstanceService connectorInstanceService;
    private final ClassLoaderService classLoaderService;
    private final RefBusinessDataService refBusinessDataService;
    private final ContractDataService contractDataService;
    private final DataInstanceService dataInstanceService;
    private final ActivityInstanceService activityInstanceService;

    private final int BATCH_SIZE = 100;

    public BPMArchiverService(ArchiveService archiveService,
            ProcessInstanceService processInstanceService,
            DocumentService documentService,
            SCommentService commentService,
            ProcessDefinitionService processDefinitionService,
            ConnectorInstanceService connectorInstanceService,
            ClassLoaderService classLoaderService,
            RefBusinessDataService refBusinessDataService,
            ContractDataService contractDataService,
            DataInstanceService dataInstanceService,
            ActivityInstanceService activityInstanceService) {
        this.archiveService = archiveService;
        this.processInstanceService = processInstanceService;
        this.documentService = documentService;
        this.commentService = commentService;
        this.processDefinitionService = processDefinitionService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        this.refBusinessDataService = refBusinessDataService;
        this.contractDataService = contractDataService;
        this.dataInstanceService = dataInstanceService;
        this.activityInstanceService = activityInstanceService;
    }

    public void archiveAndDeleteProcessInstance(final SProcessInstance processInstance) throws SArchivingException {

        //set the classloader to this process because we need it e.g. to archive data instance
        ClassLoader processClassLoader;
        try {
            processClassLoader = classLoaderService.getClassLoader(
                    identifier(ScopeType.PROCESS, processInstance.getProcessDefinitionId()));

        } catch (SClassLoaderException e) {
            throw new SArchivingException(e);
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(processClassLoader);
        try {

            final SAProcessInstance saProcessInstance = buildArchiveProcessInstance(processInstance);

            SProcessDefinition processDefinition;
            try {
                processDefinition = processDefinitionService
                        .getProcessDefinition(processInstance.getProcessDefinitionId());
            } catch (final SBonitaException e) {
                throw new SArchivingException(e);
            }

            final long archiveDate = saProcessInstance.getEndDate();

            // The archive of data instance is not done because it is done on creation + when updating.
            // Archive SComment
            archiveComments(processDefinition, processInstance, archiveDate);

            // archive document mappings
            archiveDocumentMappings(processDefinition, processInstance, archiveDate);

            archiveConnectorInstancesIfAny(processInstance, processDefinition, archiveDate);

            archiveRefBusinessDataInstances(processInstance.getId());

            // Archive
            archiveAndDeleteProcessInstanceObject(processDefinition, processInstance, saProcessInstance, archiveDate);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

    }

    protected void archiveConnectorInstancesIfAny(SProcessInstance processInstance,
            SProcessDefinition processDefinition, long archiveDate) throws SArchivingException {
        if (!processDefinition.getProcessContainer().getConnectors().isEmpty()) {
            archiveConnectors(archiveDate, processInstance.getId(), SConnectorInstance.PROCESS_TYPE);
        }
    }

    protected SAProcessInstance buildArchiveProcessInstance(SProcessInstance processInstance) {
        return BuilderFactory.get(SAProcessInstanceBuilderFactory.class).createNewInstance(processInstance).done();
    }

    private void archiveRefBusinessDataInstances(long processInstanceId) throws SArchivingException {
        try {
            List<SRefBusinessDataInstance> refBusinessDataInstances;
            int i = 0;
            do {
                refBusinessDataInstances = refBusinessDataService.getRefBusinessDataInstances(processInstanceId, i,
                        i + BATCH_SIZE);
                i += BATCH_SIZE;
                for (final SRefBusinessDataInstance sRefBusinessDataInstance : refBusinessDataInstances) {
                    refBusinessDataService.archiveRefBusinessDataInstance(sRefBusinessDataInstance);
                }
            } while (refBusinessDataInstances.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            throw new SArchivingException("Unable to archive RefBusinessDataInstance", e);
        }
    }

    private void archiveConnectors(final long archiveDate,
            final long containerId,
            final String containerType) throws SArchivingException {
        try {
            List<SConnectorInstance> connectorInstances;
            int i = 0;
            do {
                connectorInstances = connectorInstanceService.getConnectorInstances(containerId, containerType, i,
                        i + BATCH_SIZE, "id", OrderByType.ASC);
                i += BATCH_SIZE;
                for (final SConnectorInstance sConnectorInstance : connectorInstances) {
                    connectorInstanceService.archiveConnectorInstance(sConnectorInstance, archiveDate);
                }
            } while (connectorInstances.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            throw new SArchivingException("Unable to archive the container instance with id " + containerId, e);
        }
    }

    private void archiveAndDeleteProcessInstanceObject(final SProcessDefinition processDefinition,
            final SProcessInstance processInstance,
            final SAProcessInstance saProcessInstance, final long archiveDate)
            throws SArchivingException {
        try {
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saProcessInstance);
            archiveService.recordInsert(archiveDate, insertRecord);

            if (log.isDebugEnabled()) {
                log.debug("Archiving process instance with id = <{}> and state {}", processInstance.getId(),
                        processInstance.getStateId());
            }
            try {
                processInstanceService.deleteProcessInstance(processInstance.getId());
            } catch (final SBonitaException e) {
                throw new SArchivingException("Unable to delete the process instance during the archiving.", e);
            }
        } catch (final SRecorderException e) {
            setExceptionContext(processDefinition, processInstance, e);
            throw new SArchivingException("Unable to archive the process instance.", e);
        }
    }

    private void archiveDocumentMappings(final SProcessDefinition processDefinition,
            final SProcessInstance processInstance,
            final long archiveDate) throws SArchivingException {
        try {
            List<SMappedDocument> mappedDocuments;
            int startIndex = 0;
            do {
                mappedDocuments = documentService.getDocumentsOfProcessInstance(processInstance.getId(), startIndex,
                        BATCH_SIZE, null, null);
                for (final SMappedDocument mappedDocument : mappedDocuments) {
                    documentService.archive(mappedDocument, archiveDate);
                }
                startIndex += BATCH_SIZE;
            } while (mappedDocuments.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            setExceptionContext(processDefinition, processInstance, e);
            throw new SArchivingException("Unable to archive the process instance.", e);
        }
    }

    private void archiveComments(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final long archiveDate) throws SArchivingException {
        try {
            List<SComment> sComments;
            int startIndex = 0;
            do {
                sComments = commentService
                        .getComments(processInstance.getId(),
                                new QueryOptions(startIndex, BATCH_SIZE, SComment.class, "id", OrderByType.ASC));
                for (final SComment sComment : sComments) {
                    commentService.archive(archiveDate, sComment);
                }
                startIndex += BATCH_SIZE;
            } while (!sComments.isEmpty());
        } catch (final SBonitaException e) {
            setExceptionContext(processDefinition, processInstance, e);
            throw new SArchivingException("Unable to archive the process instance comments.", e);
        }
    }

    public void archiveAndDeleteFlowNodeInstance(final SFlowNodeInstance flowNodeInstance,
            final long processDefinitionId) throws SArchivingException {
        try {
            final SProcessDefinition processDefinition = processDefinitionService
                    .getProcessDefinition(processDefinitionId);
            // Remove data instance + data visibility mapping
            archiveAndDeleteFlownodeInstance(flowNodeInstance, processDefinition, System.currentTimeMillis());
        } catch (final SArchivingException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SArchivingException(e);
        }

    }

    public void archiveFlowNodeInstance(final SFlowNodeInstance flowNodeInstance) throws SArchivingException {
        archiveFlowNodeInstance(flowNodeInstance, System.currentTimeMillis());
    }

    private void archiveAndDeleteFlownodeInstance(SFlowNodeInstance flowNodeInstance,
            SProcessDefinition processDefinition, long archiveDate) throws SDataInstanceException, SArchivingException,
            SFlowNodeNotFoundException, SFlowNodeReadException, SProcessInstanceModificationException {
        if (flowNodeInstance instanceof SActivityInstance) {
            final SActivityDefinition activityDef = (SActivityDefinition) processDefinition
                    .getProcessContainer().getFlowNode(
                            flowNodeInstance.getFlowNodeDefinitionId());
            // only do search for data instances with there are data definitions. Can be null if it's a manual data add at runtime
            if (activityDef != null && !activityDef.getSDataDefinitions().isEmpty()) {
                /*
                 * Delete data instances defined at activity level:
                 * We do not archive because it's done after update not before update
                 */
                deleteLocalDataInstancesFromActivityInstance(flowNodeInstance);
            }

            if (activityDef != null && !activityDef.getConnectors().isEmpty()) {
                archiveConnectors(archiveDate, flowNodeInstance.getId(),
                        SConnectorInstance.FLOWNODE_TYPE);
            }
        }
        if (flowNodeInstance instanceof SUserTaskInstance) {
            archiveContractData(archiveDate, flowNodeInstance.getId());
        }

        // then archive the flow node instance:
        archiveFlowNodeInstance(flowNodeInstance, archiveDate);

        // Reconnect the persisted object before deleting it:
        final SFlowNodeInstance flowNodeInstance2 = activityInstanceService
                .getFlowNodeInstance(flowNodeInstance.getId());
        processInstanceService.deleteFlowNodeInstance(flowNodeInstance2, processDefinition);
    }

    private void setExceptionContext(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final SBonitaException e) {
        e.setProcessInstanceIdOnContext(processInstance.getId());
        e.setRootProcessInstanceIdOnContext(processInstance.getRootProcessInstanceId());
        e.setProcessDefinitionIdOnContext(processInstance.getProcessDefinitionId());
        e.setProcessDefinitionNameOnContext(processDefinition.getName());
        e.setProcessDefinitionVersionOnContext(processDefinition.getVersion());
    }

    private void archiveFlowNodeInstance(final SFlowNodeInstance flowNodeInstance,
            final long archiveDate)
            throws SArchivingException {
        try {
            final SAFlowNodeInstance saFlowNodeInstance = getArchivedObject(flowNodeInstance);
            if (saFlowNodeInstance != null) {
                final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saFlowNodeInstance);
                archiveService.recordInsert(archiveDate, insertRecord);
            }
        } catch (final SBonitaException e) {
            throw new SArchivingException(e);
        }
    }

    private SAFlowNodeInstance getArchivedObject(final SFlowNodeInstance flowNodeInstance) {
        SAFlowNodeInstance saFlowNodeInstance = null;
        switch (flowNodeInstance.getType()) {// TODO archive other flow node
            case AUTOMATIC_TASK:
                saFlowNodeInstance = BuilderFactory.get(SAAutomaticTaskInstanceBuilderFactory.class)
                        .createNewAutomaticTaskInstance((SAutomaticTaskInstance) flowNodeInstance).done();
                break;
            case GATEWAY:
                saFlowNodeInstance = BuilderFactory.get(SAGatewayInstanceBuilderFactory.class)
                        .createNewGatewayInstance((SGatewayInstance) flowNodeInstance).done();
                break;
            case MANUAL_TASK:
                saFlowNodeInstance = BuilderFactory.get(SAManualTaskInstanceBuilderFactory.class)
                        .createNewManualTaskInstance((SManualTaskInstance) flowNodeInstance).done();
                break;
            case USER_TASK:
                saFlowNodeInstance = BuilderFactory.get(SAUserTaskInstanceBuilderFactory.class)
                        .createNewUserTaskInstance((SUserTaskInstance) flowNodeInstance)
                        .done();
                break;
            case RECEIVE_TASK:
                saFlowNodeInstance = BuilderFactory.get(SAReceiveTaskInstanceBuilderFactory.class)
                        .createNewReceiveTaskInstance((SReceiveTaskInstance) flowNodeInstance).done();
                break;
            case SEND_TASK:
                saFlowNodeInstance = BuilderFactory.get(SASendTaskInstanceBuilderFactory.class)
                        .createNewSendTaskInstance((SSendTaskInstance) flowNodeInstance)
                        .done();
                break;
            case LOOP_ACTIVITY:
                saFlowNodeInstance = BuilderFactory.get(SALoopActivityInstanceBuilderFactory.class)
                        .createNewLoopActivityInstance((SLoopActivityInstance) flowNodeInstance).done();
                break;
            case CALL_ACTIVITY:
                saFlowNodeInstance = BuilderFactory.get(SACallActivityInstanceBuilderFactory.class)
                        .createNewArchivedCallActivityInstance((SCallActivityInstance) flowNodeInstance).done();
                break;
            case MULTI_INSTANCE_ACTIVITY:
                saFlowNodeInstance = BuilderFactory.get(SAMultiInstanceActivityInstanceBuilderFactory.class)
                        .createNewMultiInstanceActivityInstance((SMultiInstanceActivityInstance) flowNodeInstance)
                        .done();
                break;
            case SUB_PROCESS:
                saFlowNodeInstance = BuilderFactory.get(SASubProcessActivityInstanceBuilderFactory.class)
                        .createNewArchivedSubProcessActivityInstance((SSubProcessActivityInstance) flowNodeInstance)
                        .done();
                break;
            case END_EVENT:
            case START_EVENT:
            case BOUNDARY_EVENT:
            case INTERMEDIATE_CATCH_EVENT:
            case INTERMEDIATE_THROW_EVENT:
            default:
                break;
        }
        return saFlowNodeInstance;
    }

    private void deleteLocalDataInstancesFromActivityInstance(final SFlowNodeInstance flowNodeInstance)
            throws SDataInstanceException {
        List<SDataInstance> dataInstances;
        do {
            dataInstances = dataInstanceService.getLocalDataInstances(flowNodeInstance.getId(),
                    DataInstanceContainer.ACTIVITY_INSTANCE.toString(), 0, 100);
            for (final SDataInstance sDataInstance : dataInstances) {
                dataInstanceService.deleteDataInstance(sDataInstance);
            }
        } while (dataInstances.size() > 0);
    }

    private void archiveContractData(final long archiveDate,
            final long userTaskId)
            throws SArchivingException {
        try {
            contractDataService.archiveAndDeleteUserTaskData(userTaskId, archiveDate);
        } catch (final SBonitaException e) {
            throw new SArchivingException("Unable to archive contract data of container instance with id " + userTaskId,
                    e);
        }
    }

}
