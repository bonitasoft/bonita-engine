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
package org.bonitasoft.engine.execution.archive;

import java.text.MessageFormat;
import java.util.List;

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
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAAutomaticTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SACallActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAGatewayInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SALoopActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAManualTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAMultiInstanceActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAReceiveTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SASendTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SASubProcessActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ProcessArchiver {

    private static final int BATCH_SIZE = 100;

    public static void archiveProcessInstance(final SProcessInstance processInstance, final ArchiveService archiveService,
            final ProcessInstanceService processInstanceService, final DataInstanceService dataInstanceService,
            final DocumentService documentService, final TechnicalLoggerService logger,
            final SCommentService commentService, final ProcessDefinitionService processDefinitionService,
                                              final ConnectorInstanceService connectorInstanceService, ClassLoaderService classLoaderService) throws SArchivingException {

        //set the classloader to this process because we need it e.g. to archive data instance

        ClassLoader processClassLoader;
        try {
            processClassLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processInstance.getProcessDefinitionId());

        } catch (SClassLoaderException e) {
            throw new SArchivingException(e);
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(processClassLoader);
        try {

        final SAProcessInstance saProcessInstance = BuilderFactory.get(SAProcessInstanceBuilderFactory.class).createNewInstance(processInstance).done();
        final long archiveDate = saProcessInstance.getEndDate();

        SProcessDefinition processDefinition = null;
        try {
            processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
        } catch (final SBonitaException e) {
            throw new SArchivingException(e);
        }
        if (!processDefinition.getProcessContainer().getDataDefinitions().isEmpty()) {
            // Archive SADataInstance
            archiveDataInstances(processDefinition, processInstance, dataInstanceService, archiveDate);
        }
        // Archive SComment
        archiveComments(processDefinition, processInstance, commentService, archiveDate);

        // archive document mappings
        archiveDocumentMappings(processDefinition, processInstance, documentService, archiveDate);

        if (!processDefinition.getProcessContainer().getConnectors().isEmpty()) {
            archiveConnectors(connectorInstanceService, archiveDate, processInstance.getId(), SConnectorInstance.PROCESS_TYPE);
        }

        // Archive
        archiveProcessInstance(processDefinition, processInstance, saProcessInstance, archiveDate, archiveService, processInstanceService, logger);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    }

    private static void archiveConnectors(final ConnectorInstanceService connectorInstanceService, final long archiveDate, final long containerId,
            final String containerType) throws SArchivingException {
        try {
            List<SConnectorInstance> connectorInstances;
            int i = 0;
            do {
                connectorInstances = connectorInstanceService.getConnectorInstances(containerId, containerType, i, i + BATCH_SIZE, "id", OrderByType.ASC);
                i += BATCH_SIZE;
                for (final SConnectorInstance sConnectorInstance : connectorInstances) {
                    connectorInstanceService.archiveConnectorInstance(sConnectorInstance, archiveDate);
                }
            } while (connectorInstances.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            throw new SArchivingException("Unable to archive the container instance with id " + containerId, e);
        }
    }

    private static void archiveProcessInstance(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final SAProcessInstance saProcessInstance, final long archiveDate,
            final ArchiveService archiveService, final ProcessInstanceService processInstanceService, final TechnicalLoggerService logger)
            throws SArchivingException {
        try {
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saProcessInstance);
            archiveService.recordInsert(archiveDate, insertRecord);

            if (logger.isLoggable(ProcessArchiver.class, TechnicalLogSeverity.DEBUG)) {
                final StringBuilder builder = new StringBuilder();
                builder.append("Archiving " + processInstance.getClass().getSimpleName());
                builder.append("with id = <" + processInstance.getId() + ">");
                logger.log(ProcessArchiver.class, TechnicalLogSeverity.DEBUG, MessageFormat.format(" and state {2}", processInstance.getStateId()));
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

    private static void archiveDocumentMappings(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final DocumentService documentService, final long archiveDate) throws SArchivingException {
        try {
            List<SMappedDocument> mappedDocuments;
            int startIndex = 0;
            do {
                mappedDocuments = documentService.getDocumentsOfProcessInstance(processInstance.getId(), startIndex, BATCH_SIZE, null, null);
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

    private static void archiveComments(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final SCommentService commentService, final long archiveDate) throws SArchivingException {
        try {
        List<SComment> sComments = null;
        int startIndex = 0;
        do {
                sComments = commentService
                        .getComments(processInstance.getId(), new QueryOptions(startIndex, BATCH_SIZE, SComment.class, "id", OrderByType.ASC));
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

    private static void setExceptionContext(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final SBonitaException e) {
        e.setProcessInstanceIdOnContext(processInstance.getId());
        e.setRootProcessInstanceIdOnContext(processInstance.getRootProcessInstanceId());
        e.setProcessDefinitionIdOnContext(processInstance.getProcessDefinitionId());
        e.setProcessDefinitionNameOnContext(processDefinition.getName());
        e.setProcessDefinitionVersionOnContext(processDefinition.getVersion());
    }

    private static void archiveDataInstances(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final DataInstanceService dataInstanceService, final long archiveDate) throws SArchivingException {
        try {
            final long processInstanceId = processInstance.getId();
            final int archiveBatchSize = 50;
            int currentIndex = 0;
            List<SDataInstance> sDataInstances = dataInstanceService.getLocalDataInstances(processInstanceId,
                    DataInstanceContainer.PROCESS_INSTANCE.toString(), currentIndex,
                    archiveBatchSize);

            while (sDataInstances != null && sDataInstances.size() > 0) {
                for (final SDataInstance sDataInstance : sDataInstances) {
                    dataInstanceService.archiveDataInstance(sDataInstance, archiveDate);
                }
                currentIndex += archiveBatchSize;
                sDataInstances = dataInstanceService.getLocalDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString(), currentIndex,
                        archiveBatchSize);
            }
        } catch (final SDataInstanceException e) {
            setExceptionContext(processDefinition, processInstance, e);
            throw new SArchivingException("Unable to archive the process instance.", e);
        }
    }

    private static void archiveFlowNodeInstance(final SFlowNodeInstance flowNodeInstance, final ArchiveService archiveService, final long archiveDate)
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

    private static SAFlowNodeInstance getArchivedObject(final SFlowNodeInstance flowNodeInstance) {
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
                        .createNewMultiInstanceActivityInstance((SMultiInstanceActivityInstance) flowNodeInstance).done();
                break;
            case SUB_PROCESS:
                saFlowNodeInstance = BuilderFactory.get(SASubProcessActivityInstanceBuilderFactory.class)
                        .createNewArchivedSubProcessActivityInstance((SSubProcessActivityInstance) flowNodeInstance).done();
                break;
            case END_EVENT:
                // To uncomment if need to fix BS-11970
                //                saFlowNodeInstance = BuilderFactory.get(SAEndEventInstanceBuilderFactory.class)
                //                        .createNewArchivedEndEventInstance((SEndEventInstance) flowNodeInstance).done();
                break;
            case START_EVENT:
                // To uncomment if need to fix BS-11970
                //                saFlowNodeInstance = BuilderFactory.get(SAStartEventInstanceBuilderFactory.class)
                //                        .createNewArchivedStartEventInstance((SStartEventInstance) flowNodeInstance).done();
                break;
            case BOUNDARY_EVENT:
                break;
            case INTERMEDIATE_CATCH_EVENT:
                break;
            case INTERMEDIATE_THROW_EVENT:
                break;
            default:
                break;
        }
        return saFlowNodeInstance;
    }

    private static void deleteLocalDataInstancesFromActivityInstance(final SFlowNodeInstance flowNodeInstance, final DataInstanceService dataInstanceService)
            throws SDataInstanceException {
        List<SDataInstance> dataInstances;
        do {
            dataInstances = dataInstanceService.getLocalDataInstances(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString(), 0, 100);
            for (final SDataInstance sDataInstance : dataInstances) {
                dataInstanceService.deleteDataInstance(sDataInstance);
            }
        } while (dataInstances.size() > 0);
    }

    public static void archiveFlowNodeInstance(final SFlowNodeInstance intTxflowNodeInstance, final boolean deleteAfterArchive, final long processDefinitionId,
            final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService, final ArchiveService archiveService,
            final DataInstanceService dataInstanceService, final ActivityInstanceService activityInstanceService,
            final ConnectorInstanceService connectorInstanceService, final ContractDataService contractDataService) throws SArchivingException {
        try {
            final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            final long archiveDate = System.currentTimeMillis();
            // Remove data instance + data visibility mapping
            if (deleteAfterArchive) {
                if (intTxflowNodeInstance instanceof SActivityInstance) {
                    final SActivityDefinition activityDef = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                            intTxflowNodeInstance.getFlowNodeDefinitionId());
                    // only do search for data instances with there are data definitions. Can be null if it's a manual data add at runtime
                    if (activityDef != null && !activityDef.getSDataDefinitions().isEmpty()) {
                        /*
                         * Delete data instances defined at activity level:
                         * We do not archive because it's done after update not before update
                         */
                        deleteLocalDataInstancesFromActivityInstance(intTxflowNodeInstance, dataInstanceService);
                    }

                    if (activityDef != null && !activityDef.getConnectors().isEmpty()) {
                        archiveConnectors(connectorInstanceService, archiveDate, intTxflowNodeInstance.getId(), SConnectorInstance.FLOWNODE_TYPE);
                    }
                }
                if (intTxflowNodeInstance instanceof SUserTaskInstance) {
                    archiveContractData(contractDataService, archiveDate, intTxflowNodeInstance.getId());
                }

                // then archive the flow node instance:
                archiveFlowNodeInstance(intTxflowNodeInstance, archiveService, archiveDate);

                // Reconnect the persisted object before deleting it:
                final SFlowNodeInstance flowNodeInstance2 = activityInstanceService.getFlowNodeInstance(intTxflowNodeInstance.getId());
                processInstanceService.deleteFlowNodeInstance(flowNodeInstance2, processDefinition);
            } else {
                archiveFlowNodeInstance(intTxflowNodeInstance, archiveService, archiveDate);
            }
        } catch (final SArchivingException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SArchivingException(e);
        }

    }

    private static void archiveContractData(final ContractDataService contractDataService, final long archiveDate, final long userTaskId)
            throws SArchivingException {
        try {
            contractDataService.archiveAndDeleteUserTaskData(userTaskId, archiveDate);
        } catch (final SBonitaException e) {
            throw new SArchivingException("Unable to archive contract data of container instance with id " + userTaskId, e);
        }
    }

    public static boolean willBeArchived(final SFlowNodeInstance flowNodeInstance, final ArchiveService archiveService) {
        final SFlowNodeType type = flowNodeInstance.getType();
        return type != SFlowNodeType.END_EVENT
                && type != SFlowNodeType.START_EVENT
                && type != SFlowNodeType.BOUNDARY_EVENT
                && type != SFlowNodeType.INTERMEDIATE_CATCH_EVENT
                && type != SFlowNodeType.INTERMEDIATE_THROW_EVENT
                && archiveService.isArchivable((Class<? extends org.bonitasoft.engine.persistence.PersistentObject>) flowNodeInstance.getClass()
                        .getInterfaces()[0]);
    }
}
