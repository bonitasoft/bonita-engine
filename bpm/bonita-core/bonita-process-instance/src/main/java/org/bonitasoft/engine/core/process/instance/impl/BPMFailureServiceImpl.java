/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.ExceptionContext;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.instance.api.BPMFailureService;
import org.bonitasoft.engine.core.process.instance.model.SABPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SBPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BPMFailureServiceImpl implements BPMFailureService {

    private static final String TYPE_SEPARATOR = "::";
    private static final String CONTEXT_PATH_SEPARATOR = "//";

    private final PersistenceService persistenceService;
    private final ArchiveService archiveService;

    public BPMFailureServiceImpl(PersistenceService persistenceService, ArchiveService archiveService) {
        this.persistenceService = persistenceService;
        this.archiveService = archiveService;
    }

    @Override
    public SBPMFailure createFlowNodeFailure(SFlowNodeInstance flowNodeInstance, Failure failure)
            throws SPersistenceException {
        log.debug("Adding failure for flow node instance {}", flowNodeInstance.getId());
        var bpmFailure = SBPMFailure.builder()
                .flowNodeInstanceId(flowNodeInstance.getId())
                .processInstanceId(flowNodeInstance.getParentProcessInstanceId())
                .rootProcessInstanceId(flowNodeInstance.getRootProcessInstanceId())
                .processDefinitionId(flowNodeInstance.getProcessDefinitionId())
                .scope(failure.scope())
                .context(buildContext(failure.throwable()))
                .errorMessage(ExceptionUtils.getRootCauseMessage(failure.throwable()))
                .stackTrace(ExceptionUtils.getStackTrace(failure.throwable()))
                .build();
        return persistenceService.insert(bpmFailure);
    }

    @Override
    public List<SBPMFailure> getFlowNodeFailures(long flowNodeInstanceId, int maxResults) throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("flowNodeInstanceId", flowNodeInstanceId));
        return persistenceService.selectList(new SelectListDescriptor<>("getFlowNodeFailures", parameters,
                SBPMFailure.class, queryOptions));
    }

    @Override
    public void archiveFlowNodeFailures(long flowNodeInstanceId, long archiveDate) throws SBonitaException {
        log.debug("Archiving failures of flow node instance {}", flowNodeInstanceId);
        archiveService.recordInserts(archiveDate, getFlowNodeFailures(flowNodeInstanceId, Integer.MAX_VALUE)
                .stream()
                .map(SABPMFailure::new)
                .map(ArchiveInsertRecord::new)
                .toArray(ArchiveInsertRecord[]::new));
    }

    @Override
    public void deleteFlowNodeFailures(long flowNodeInstanceId) throws SBonitaException {
        log.debug("Deleting failures of flow node instance {}", flowNodeInstanceId);
        persistenceService.delete(getFlowNodeFailures(flowNodeInstanceId, Integer.MAX_VALUE)
                .stream()
                .map(SBPMFailure::getId)
                .toList(),
                SBPMFailure.class);
    }

    @Override
    public void deleteArchivedFlowNodeFailures(List<Long> flowNodeInstanceIds) throws SBonitaException {
        if (!flowNodeInstanceIds.isEmpty()) {
            log.debug("Deleting archived failures of flow node instances {}", flowNodeInstanceIds);
            archiveService.deleteFromQuery("deleteArchivedBPMFailuresByFlowNodeInstanceIds",
                    Map.ofEntries(Map.entry("flowNodeInstanceIds", flowNodeInstanceIds)));
        }
    }

    @Override
    public List<SABPMFailure> getArchivedFlowNodeFailures(long flowNodeInstanceId, int maxResults)
            throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("flowNodeInstanceId", flowNodeInstanceId));
        return persistenceService.selectList(new SelectListDescriptor<>("getArchivedFlowNodeFailures", parameters,
                SABPMFailure.class, queryOptions));
    }

    @Override
    public SBPMFailure createProcessInstanceFailure(SProcessInstance processInstance, Failure failure)
            throws SPersistenceException {
        log.debug("Registering failure for process instance {}", processInstance.getId());
        var bpmFailure = SBPMFailure.builder()
                .processInstanceId(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .rootProcessInstanceId(processInstance.getRootProcessInstanceId())
                .scope(failure.scope())
                .context(buildContext(failure.throwable()))
                .errorMessage(ExceptionUtils.getRootCauseMessage(failure.throwable()))
                .stackTrace(ExceptionUtils.getStackTrace(failure.throwable()))
                .build();
        return persistenceService.insert(bpmFailure);
    }

    @Override
    public List<SBPMFailure> getProcessInstanceFailures(long processInstanceId, int maxResults)
            throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("processInstanceId", processInstanceId));
        return persistenceService.selectList(new SelectListDescriptor<>("getProcessInstanceFailures", parameters,
                SBPMFailure.class, queryOptions));
    }

    @Override
    public void archiveProcessInstanceFailures(long processInstanceId, long archiveDate) throws SBonitaException {
        log.debug("Archiving failures of process instance {}", processInstanceId);
        archiveService.recordInserts(archiveDate, getProcessInstanceFailures(processInstanceId, Integer.MAX_VALUE)
                .stream()
                .map(SABPMFailure::new)
                .map(ArchiveInsertRecord::new)
                .toArray(ArchiveInsertRecord[]::new));
    }

    @Override
    public void deleteProcessInstanceFailures(long processInstanceId) throws SBonitaException {
        log.debug("Deleting failures of process instance {}", processInstanceId);
        persistenceService.delete(getProcessInstanceFailures(processInstanceId, Integer.MAX_VALUE)
                .stream()
                .map(SBPMFailure::getId)
                .toList(),
                SBPMFailure.class);
    }

    @Override
    public void deleteArchivedProcessInstanceFailures(List<Long> processInstanceIds) throws SBonitaException {
        if (!processInstanceIds.isEmpty()) {
            log.debug("Deleting archived failures of process instances {}", processInstanceIds);
            archiveService.deleteFromQuery("deleteArchivedBPMFailuresByProcessInstanceIds",
                    Map.ofEntries(Map.entry("processInstanceIds", processInstanceIds)));
        }
    }

    @Override
    public List<SABPMFailure> getArchivedProcessInstanceFailures(long processInstanceId, int maxResults)
            throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("processInstanceId", processInstanceId));
        return persistenceService
                .selectList(new SelectListDescriptor<>("getArchivedProcessInstanceFailures", parameters,
                        SABPMFailure.class, queryOptions));
    }

    @Override
    public List<SBPMFailure> getChildProcessInstancesFailures(long rootProcessInstanceId, int maxResults)
            throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("rootProcessInstanceId", rootProcessInstanceId));
        return persistenceService.selectList(new SelectListDescriptor<>("getChildProcessInstancesFailures", parameters,
                SBPMFailure.class, queryOptions));
    }

    @Override
    public List<SABPMFailure> getArchivedChildProcessInstancesFailures(long rootProcessInstanceId, int maxResults)
            throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, maxResults);
        final Map<String, Object> parameters = Map.ofEntries(Map.entry("rootProcessInstanceId", rootProcessInstanceId));
        return persistenceService
                .selectList(new SelectListDescriptor<>("getArchivedChildProcessInstancesFailures", parameters,
                        SABPMFailure.class, queryOptions));
    }

    private static String buildContext(Throwable e) {
        Map<SExceptionContext, Serializable> ctx = new TreeMap<>();
        if (e instanceof ExceptionContext exceptionContext) {
            ctx = exceptionContext.getContext();
        }
        var contextBuilder = new StringBuilder();

        if (ctx.containsKey(SExceptionContext.MESSAGE_INSTANCE_NAME)) {
            contextBuilder.append("message")
                    .append(TYPE_SEPARATOR)
                    .append(ctx.get(SExceptionContext.MESSAGE_INSTANCE_NAME));
        }

        addConnectorContext(contextBuilder, e, ctx);

        addTransitionContext(contextBuilder, ctx);

        var evaluationException = ExceptionUtils.throwableOfType(e, SExpressionEvaluationException.class);
        if (evaluationException != null) {
            addContextPathSeparator(contextBuilder);
            contextBuilder.append("expression")
                    .append(TYPE_SEPARATOR)
                    .append(evaluationException.getExpressionName());
        }
        return contextBuilder.toString();
    }

    private static void addTransitionContext(StringBuilder contextBuilder,
            Map<SExceptionContext, Serializable> ctx) {
        if (ctx.containsKey(SExceptionContext.TRANSITION_TARGET_FLOWNODE_NAME)) {
            if (ctx.containsKey(SExceptionContext.TRANSITION_NAME)) {
                contextBuilder.append(ctx.get(SExceptionContext.TRANSITION_NAME));
                addContextPathSeparator(contextBuilder);
            }
            contextBuilder.append("to")
                    .append(TYPE_SEPARATOR)
                    .append(ctx.get(SExceptionContext.TRANSITION_TARGET_FLOWNODE_NAME));
        }
    }

    private static void addConnectorContext(StringBuilder contextBuilder, Throwable e,
            Map<SExceptionContext, Serializable> ctx) {
        if (ctx.containsKey(SExceptionContext.CONNECTOR_NAME)) {
            addContextPathSeparator(contextBuilder);
            contextBuilder.append(ctx.get(SExceptionContext.CONNECTOR_NAME));
            if (ctx.containsKey(SExceptionContext.CONNECTOR_DEFINITION_ID)) {
                contextBuilder.append(TYPE_SEPARATOR);
                contextBuilder.append(ctx.get(SExceptionContext.CONNECTOR_DEFINITION_ID));
            }
            if (ctx.containsKey(SExceptionContext.CONNECTOR_ACTIVATION_EVENT)) {
                contextBuilder.append(TYPE_SEPARATOR);
                contextBuilder.append(ctx.get(SExceptionContext.CONNECTOR_ACTIVATION_EVENT).toString().toLowerCase());
            }
            var operationExecutionException = ExceptionUtils.throwableOfType(e, SOperationExecutionException.class);
            if (operationExecutionException != null) {
                addContextPathSeparator(contextBuilder);
                contextBuilder.append("output");
            }
        }

        if (ctx.containsKey(SExceptionContext.CONNECTOR_INPUT_NAME)) {
            addContextPathSeparator(contextBuilder);
            contextBuilder.append("input")
                    .append(TYPE_SEPARATOR)
                    .append(ctx.get(SExceptionContext.CONNECTOR_INPUT_NAME));
        }

        var connectorValidationEx = ExceptionUtils.throwableOfType(e, ConnectorValidationException.class);
        if (connectorValidationEx != null) {
            addContextPathSeparator(contextBuilder);
            contextBuilder.append("input-validation");
        }
    }

    private static void addContextPathSeparator(StringBuilder contextBuilder) {
        if (!contextBuilder.isEmpty()) {
            contextBuilder.append(CONTEXT_PATH_SEPARATOR);
        }
    }

}
