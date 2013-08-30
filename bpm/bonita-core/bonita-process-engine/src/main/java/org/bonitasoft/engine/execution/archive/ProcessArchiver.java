/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingException;
import org.bonitasoft.engine.core.process.document.mapping.exception.SPageOutOfRangeException;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
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
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.ProcessInstanceLogBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public class ProcessArchiver {

    private static final int BATCH_SIZE = 100;

    public static void archiveProcessInstance(final SProcessInstance processInstance, final ArchiveService archiveService,
            final ProcessInstanceService processInstanceService, final DataInstanceService dataInstanceService,
            final DocumentMappingService documentMappingService, final TechnicalLoggerService logger, final BPMInstanceBuilders instancesBuilders,
            final SDataInstanceBuilders sDataInstanceBuilders, final SCommentService commentService, final SACommentBuilder saCommentBuilder,
            final ProcessDefinitionService processDefinitionService, final ConnectorInstanceService connectorInstanceService) throws SArchivingException {
        final SAProcessInstanceBuilder saProcessInstanceBuilder = instancesBuilders.getSAProcessInstanceBuilder();
        final SAProcessInstance saProcessInstance = saProcessInstanceBuilder.createNewInstance(processInstance).done();
        final long archiveDate = saProcessInstance.getEndDate();
        try {
            dataInstanceService.removeContainer(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.toString());
        } catch (final SDataInstanceException e) {
            throw new SArchivingException("unable to delete data mapping", e);
        }
        SProcessDefinition processDefinition = null;
        try {
            processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
        } catch (final SBonitaException e) {
            throw new SArchivingException(e);
        }
        if (!processDefinition.getProcessContainer().getDataDefinitions().isEmpty()) {
            // Archive SADataInstance
            archiveDataInstances(processInstance, archiveService, dataInstanceService, logger, instancesBuilders, sDataInstanceBuilders, archiveDate);
        }
        // Archive SComment
        archiveComments(processInstance, archiveService, logger, instancesBuilders, commentService, saCommentBuilder, archiveDate);

        // archive document mappings
        archiveDocumentMappings(processInstance, documentMappingService, archiveDate);

        if (!processDefinition.getProcessContainer().getConnectors().isEmpty()) {
            archiveConnectors(connectorInstanceService, archiveDate, processInstance.getId(), SConnectorInstance.PROCESS_TYPE);
        }

        // Archive
        archiveProcessInstance(processInstance, archiveService, processInstanceService, logger, instancesBuilders, saProcessInstance, archiveDate);

    }

    /**
     * @param sProcessInstance
     * @param connectorInstanceService
     * @param archiveDate
     * @param containerId
     * @param containerType
     * @throws SArchivingException
     */
    private static void archiveConnectors(final ConnectorInstanceService connectorInstanceService, final long archiveDate, final long containerId,
            final String containerType) throws SArchivingException {
        try {
            List<SConnectorInstance> connectorInstances;
            int i = 0;
            do {
                connectorInstances = connectorInstanceService.getConnectorInstances(containerId, containerType, i, i + BATCH_SIZE, null, null);
                i += BATCH_SIZE;
                for (final SConnectorInstance sConnectorInstance : connectorInstances) {
                    connectorInstanceService.archiveConnectorInstance(sConnectorInstance, archiveDate);
                }
            } while (connectorInstances.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            throw new SArchivingException("Unable to archive the container instance with id " + containerId, e);
        }
    }

    private static void archiveProcessInstance(final SProcessInstance processInstance, final ArchiveService archiveService,
            final ProcessInstanceService processInstanceService, final TechnicalLoggerService logger, final BPMInstanceBuilders instancesBuilders,
            final SAProcessInstance saProcessInstance, final long archiveDate) throws SArchivingException {
        try {
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saProcessInstance);
            archiveService.recordInsert(archiveDate, insertRecord);

            if (logger.isLoggable(ProcessArchiver.class, TechnicalLogSeverity.DEBUG)) {
                logger.log(ProcessArchiver.class, TechnicalLogSeverity.DEBUG, MessageFormat.format("archiving {0} with id {1} and state {2}", processInstance
                        .getClass().getSimpleName(), processInstance.getId(), processInstance.getStateId()));
            }
            try {
                processInstanceService.deleteProcessInstance(processInstance.getId());
            } catch (final SBonitaException e) {
                throw new SArchivingException("Unable to delete the process instance during the archiving", e);
            }
        } catch (final SRecorderException e) {
            throw new SArchivingException("Unable to archive the process instance with id " + processInstance.getId(), e);
        } catch (final SDefinitiveArchiveNotFound e) {
            logger.log(ProcessArchiver.class, TechnicalLogSeverity.WARNING, "the process instance was not archived id=" + processInstance.getId(), e);
        }
    }

    private static void archiveDocumentMappings(final SProcessInstance processInstance, final DocumentMappingService documentMappingService,
            final long archiveDate) throws SArchivingException {
        List<SDocumentMapping> sDocumentMappings = null;
        do {
            try {
                sDocumentMappings = documentMappingService.getDocumentMappingsForProcessInstance(processInstance.getId(), 0, BATCH_SIZE, null, null);
            } catch (final SPageOutOfRangeException e1) {
                throw new SArchivingException("Unable to archive the process instance with id " + processInstance.getId(), e1);
            } catch (final SDocumentMappingException e1) {
                throw new SArchivingException("Unable to archive the process instance with id " + processInstance.getId(), e1);
            }
            for (final SDocumentMapping sDocumentMapping : sDocumentMappings) {
                try {
                    documentMappingService.archive(sDocumentMapping, archiveDate);
                } catch (final SDocumentMappingException e) {
                    throw new SArchivingException("Unable to archive the process instance with id " + processInstance.getId(), e);
                }
            }
        } while (sDocumentMappings.size() == BATCH_SIZE);
    }

    private static void archiveComments(final SProcessInstance processInstance, final ArchiveService archiveService, final TechnicalLoggerService logger,
            final BPMInstanceBuilders instancesBuilders, final SCommentService commentService, final SACommentBuilder saCommentBuilder, final long archiveDate)
            throws SArchivingException {
        List<SComment> sComments = null;
        try {
            sComments = commentService.getComments(processInstance.getId());
        } catch (final SBonitaReadException e1) {
            logger.log(ProcessArchiver.class, TechnicalLogSeverity.WARNING, "no process comment found for process. id=" + processInstance.getId(), e1);
        }

        if (sComments != null) {
            for (final SComment sComment : sComments) {
                final SAComment saComment = saCommentBuilder.createNewInstance(sComment).done();
                if (saComment != null) {
                    final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saComment);
                    try {
                        archiveService.recordInsert(archiveDate, insertRecord);
                    } catch (final SRecorderException e) {
                        throw new SArchivingException("Unable to archive the process instance with id " + processInstance.getId(), e);
                    } catch (final SDefinitiveArchiveNotFound e) {
                        logger.log(ProcessArchiver.class, TechnicalLogSeverity.WARNING, "the process instance was not archived id=" + processInstance.getId(),
                                e);
                    }
                }
            }
        }
    }

    private static void archiveDataInstances(final SProcessInstance processInstance, final ArchiveService archiveService,
            final DataInstanceService dataInstanceService, final TechnicalLoggerService logger, final BPMInstanceBuilders instancesBuilders,
            final SDataInstanceBuilders sDataInstanceBuilders, final long archiveDate) throws SArchivingException {
        final int archiveBatchSize = 50;
        int currentIndex = 0;
        try {
            List<SDataInstance> sDataInstances = dataInstanceService.getLocalDataInstances(processInstance.getId(),
                    DataInstanceContainer.PROCESS_INSTANCE.toString(), currentIndex, archiveBatchSize);

            while (sDataInstances != null && sDataInstances.size() > 0) {
                for (final SDataInstance sDataInstance : sDataInstances) {
                    final SADataInstance saDataInstance = sDataInstanceBuilders.getSADataInstanceBuilder().createNewInstance(sDataInstance).done();
                    if (saDataInstance != null) {
                        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saDataInstance);
                        try {
                            archiveService.recordInsert(archiveDate, insertRecord);
                        } catch (final SRecorderException e) {
                            throw new SArchivingException("Unable to archive the process instance with id " + processInstance.getId(), e);
                        } catch (final SDefinitiveArchiveNotFound e) {
                            logger.log(ProcessArchiver.class, TechnicalLogSeverity.WARNING,
                                    "the process instance was not archived id=" + processInstance.getId(), e);
                        }
                    }
                }
                currentIndex += archiveBatchSize;
                sDataInstances = dataInstanceService.getLocalDataInstances(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.toString(),
                        currentIndex, archiveBatchSize);
            }
        } catch (final SDataInstanceException e) {
            // /FIXME: improve data service to make difference between invalid container and container without data
            logger.log(ProcessArchiver.class, TechnicalLogSeverity.WARNING, "no data instances found for process. id=" + processInstance.getId(), e);
            // return;
        }
    }

    private static ProcessInstanceLogBuilder getQueriableLog(final BPMInstanceBuilders instancesBuilders, final ActionType actionType, final String message) {
        final ProcessInstanceLogBuilder logBuilder = instancesBuilders.getProcessInstanceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private static <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private static <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private static void archiveFlowNodeInstance(final SFlowNodeInstance flowNodeInstance, final boolean deleteAfterArchive,
            final ProcessInstanceService processInstanceService, final ArchiveService archiveService, final BPMInstanceBuilders bpmInstanceBuilders,
            final DataInstanceService dataInstanceService, final SProcessDefinition processDefinition, final ActivityInstanceService activityInstanceService,
            final ConnectorInstanceService connectorInstanceService) throws SActivityExecutionException {
        try {
            final long archiveDate = System.currentTimeMillis();
            // Remove data instance + data visibility mapping
            if (deleteAfterArchive && flowNodeInstance instanceof SActivityInstance) {
                final SActivityDefinition activityDef = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                        flowNodeInstance.getFlowNodeDefinitionId());
                // only do search for data instances with there are data definitions. Can be null if it's a manual data add at runtime
                if (activityDef != null && !activityDef.getSDataDefinitions().isEmpty()) {
                    try {
                        /*
                         * Delete data instances defined at activity level:
                         * We do not archive because it's done after update not before update
                         */
                        List<SDataInstance> dataInstances;
                        do {
                            dataInstances = dataInstanceService.getLocalDataInstances(flowNodeInstance.getId(),
                                    DataInstanceContainer.ACTIVITY_INSTANCE.toString(), 0, QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
                            for (final SDataInstance sDataInstance : dataInstances) {
                                dataInstanceService.deleteDataInstance(sDataInstance);
                            }
                        } while (dataInstances != null && dataInstances.size() > 0);
                        dataInstanceService.removeContainer(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString());
                    } catch (final SDataInstanceException e) {
                        throw new SActivityExecutionException(e);
                    }
                }

                if (activityDef != null && !activityDef.getConnectors().isEmpty()) {
                    archiveConnectors(connectorInstanceService, archiveDate, flowNodeInstance.getId(), SConnectorInstance.FLOWNODE_TYPE);
                }
            }
            // then archive the flownode instance:
            SAFlowNodeInstance saFlowNodeInstance = null;
            switch (flowNodeInstance.getType()) {// TODO archive other flow node
                case AUTOMATIC_TASK:
                    saFlowNodeInstance = bpmInstanceBuilders.getSAAutomaticTaskInstanceBuilder()
                            .createNewAutomaticTaskInstance((SAutomaticTaskInstance) flowNodeInstance).done();
                    break;
                case GATEWAY:
                    saFlowNodeInstance = bpmInstanceBuilders.getSAGatewayInstanceBuilder().createNewGatewayInstance((SGatewayInstance) flowNodeInstance).done();
                    break;
                case MANUAL_TASK:
                    saFlowNodeInstance = bpmInstanceBuilders.getSAManualTaskInstanceBuilder()
                            .createNewManualTaskInstance((SManualTaskInstance) flowNodeInstance).done();
                    break;
                case USER_TASK:
                    saFlowNodeInstance = bpmInstanceBuilders.getSAUserTaskInstanceBuilder().createNewUserTaskInstance((SUserTaskInstance) flowNodeInstance)
                            .done();
                    break;
                case RECEIVE_TASK:
                    saFlowNodeInstance = bpmInstanceBuilders.getSAReceiveTaskInstanceBuilder()
                            .createNewReceiveTaskInstance((SReceiveTaskInstance) flowNodeInstance).done();
                    break;
                case SEND_TASK:
                    saFlowNodeInstance = bpmInstanceBuilders.getSASendTaskInstanceBuilder().createNewSendTaskInstance((SSendTaskInstance) flowNodeInstance)
                            .done();
                    break;
                case LOOP_ACTIVITY:
                    saFlowNodeInstance = bpmInstanceBuilders.getSALoopActivitynstanceBuilder()
                            .createNewLoopActivityInstance((SLoopActivityInstance) flowNodeInstance).done();
                    break;
                case CALL_ACTIVITY:
                    saFlowNodeInstance = bpmInstanceBuilders.getSACallActivityInstanceBuilder()
                            .createNewArchivedCallActivityInstance((SCallActivityInstance) flowNodeInstance).done();
                    break;
                case MULTI_INSTANCE_ACTIVITY:
                    saFlowNodeInstance = bpmInstanceBuilders.getSAMultiInstanceActivitynstanceBuilder()
                            .createNewMultiInstanceActivityInstance((SMultiInstanceActivityInstance) flowNodeInstance).done();
                    break;
                case SUB_PROCESS:
                    saFlowNodeInstance = bpmInstanceBuilders.getSASubProcessActivityInstanceBuilder()
                            .createNewArchivedSubProcessActivityInstance((SSubProcessActivityInstance) flowNodeInstance).done();
                    break;
                case END_EVENT:
                    break;
                case START_EVENT:
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
            if (saFlowNodeInstance != null) {
                final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saFlowNodeInstance);
                archiveService.recordInsert(archiveDate, insertRecord);
            }
            if (deleteAfterArchive) {
                // Reconnect the persisted object before deleting it:
                final SFlowNodeInstance flowNodeInstance2 = activityInstanceService.getFlowNodeInstance(flowNodeInstance.getId());
                processInstanceService.deleteFlowNodeInstance(flowNodeInstance2, processDefinition);
            }
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
    }

    public static void archiveFlowNodeInstance(final SFlowNodeInstance intTxflowNodeInstance, final boolean deleteAfterArchive, final long processDefinitionId,
            final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService, final ArchiveService archiveService,
            final BPMInstanceBuilders bpmInstanceBuilders, final DataInstanceService dataInstanceService,
            final ActivityInstanceService activityInstanceService, final ConnectorInstanceService connectorInstanceService) throws SActivityExecutionException {
        final SProcessDefinition processDefinition;
        try {
            processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
        archiveFlowNodeInstance(intTxflowNodeInstance, deleteAfterArchive, processInstanceService, archiveService, bpmInstanceBuilders, dataInstanceService,
                processDefinition, activityInstanceService, connectorInstanceService);
    }
}
