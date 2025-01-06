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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.instance.api.BPMFailureService;
import org.bonitasoft.engine.core.process.instance.model.SABPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SBPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BPMFailureServiceImplTest {

    @Mock
    private PersistenceService persistenceService;
    @Mock
    private ArchiveService archiveService;

    private BPMFailureServiceImpl service;

    @BeforeEach
    void setup() throws Exception {
        service = spy(new BPMFailureServiceImpl(persistenceService, archiveService));
        when(persistenceService.insert(any(SBPMFailure.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_proper_content() throws Exception {
        var now = Instant.now();
        var flowNodeInstance = createFlowNodeInstance();
        var failure = new BPMFailureService.Failure("scope", new Throwable("error message"));

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        verify(persistenceService).insert(bpmFailure);
        assertThat(bpmFailure.getFlowNodeInstanceId()).isEqualTo(flowNodeInstance.getId());
        assertThat(bpmFailure.getProcessDefinitionId()).isEqualTo(flowNodeInstance.getProcessDefinitionId());
        assertThat(bpmFailure.getProcessInstanceId()).isEqualTo(flowNodeInstance.getParentProcessInstanceId());
        assertThat(bpmFailure.getRootProcessInstanceId()).isEqualTo(flowNodeInstance.getRootProcessInstanceId());
        assertThat(bpmFailure.getScope()).isEqualTo("scope");
        assertThat(bpmFailure.getErrorMessage()).isEqualTo("Throwable: error message");
        assertThat(bpmFailure.getStackTrace()).isEqualTo(ExceptionUtils.getStackTrace(failure.throwable()));
        assertThat(Instant.ofEpochMilli(bpmFailure.getFailureDate())).isCloseTo(now, within(1000, ChronoUnit.MILLIS));
    }

    @Test
    void should_createProcessInstanceFailure_record_failure_with_proper_content() throws Exception {
        var now = Instant.now();
        var processInstance = createProcessInstance();
        var failure = new BPMFailureService.Failure("scope", new Throwable("error message"));

        var bpmFailure = service.createProcessInstanceFailure(processInstance, failure);

        verify(persistenceService).insert(bpmFailure);
        assertThat(bpmFailure.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(bpmFailure.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(bpmFailure.getRootProcessInstanceId()).isEqualTo(processInstance.getRootProcessInstanceId());
        assertThat(bpmFailure.getScope()).isEqualTo("scope");
        assertThat(bpmFailure.getErrorMessage()).isEqualTo("Throwable: error message");
        assertThat(bpmFailure.getStackTrace()).isEqualTo(ExceptionUtils.getStackTrace(failure.throwable()));
        assertThat(Instant.ofEpochMilli(bpmFailure.getFailureDate())).isCloseTo(now, within(1000, ChronoUnit.MILLIS));
    }

    private static SFlowNodeInstance createFlowNodeInstance() {
        SFlowNodeInstance flowNodeInstance = Mockito.mock(SFlowNodeInstance.class);
        when(flowNodeInstance.getId()).thenReturn(1L);
        when(flowNodeInstance.getParentProcessInstanceId()).thenReturn(2L);
        when(flowNodeInstance.getProcessDefinitionId()).thenReturn(3L);
        when(flowNodeInstance.getRootProcessInstanceId()).thenReturn(4L);
        return flowNodeInstance;
    }

    private static SProcessInstance createProcessInstance() {
        SProcessInstance processInstance = Mockito.mock(SProcessInstance.class);
        when(processInstance.getId()).thenReturn(10L);
        when(processInstance.getProcessDefinitionId()).thenReturn(11L);
        when(processInstance.getRootProcessInstanceId()).thenReturn(12L);
        return processInstance;
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_expression_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var failure = new BPMFailureService.Failure("scope",
                new SExpressionEvaluationException(new RuntimeException("error in expression"), "expressionName"));

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("expression::expressionName");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_message_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInMessage = new SBonitaRuntimeException("error in message");
        errorInMessage.setMessageInstanceNameOnContext("messageName");
        var failure = new BPMFailureService.Failure("scope", errorInMessage);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("message::messageName");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_connector_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException("error in connector");
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("get-info::rest-connector::on_enter");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_connector_input_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException("error in connector");
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        errorInConnector.setConnectorInputOnContext("username");
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("get-info::rest-connector::on_enter//input::username");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_connector_input_expression_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException(new SExpressionEvaluationException(
                new RuntimeException("error in input expression"), "expressionName"));
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        errorInConnector.setConnectorInputOnContext("username");
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("get-info::rest-connector::on_enter//input::username//expression::expressionName");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_connector_output_expression_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException(
                new SOperationExecutionException(new SExpressionEvaluationException(
                        new RuntimeException("error in output expression"), "expressionName")));
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("get-info::rest-connector::on_enter//output//expression::expressionName");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_connector_validation_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInConnector = new SBonitaRuntimeException(new ConnectorValidationException("error in validation"));
        errorInConnector.setConnectorDefinitionIdOnContext("rest-connector");
        errorInConnector.setConnectorNameOnContext("get-info");
        errorInConnector.setConnectorActivationEventOnContext(ConnectorEvent.ON_ENTER.name());
        var failure = new BPMFailureService.Failure("scope", errorInConnector);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext()).isEqualTo("get-info::rest-connector::on_enter//input-validation");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_named_transition_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInTransition = new SBonitaRuntimeException(new SExpressionEvaluationException(
                new RuntimeException("error in consition expression"), "expressionName"));
        errorInTransition.getContext().put(SExceptionContext.TRANSITION_NAME, "transitionName");
        errorInTransition.getContext().put(SExceptionContext.TRANSITION_TARGET_FLOWNODE_NAME, "gateway::gatewayName");
        var failure = new BPMFailureService.Failure("scope", errorInTransition);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("transitionName//to::gateway::gatewayName//expression::expressionName");
    }

    @Test
    void should_createFlowNodeFailure_record_failure_with_unnamed_transition_context() throws Exception {
        var flowNodeInstance = createFlowNodeInstance();
        var errorInTransition = new SBonitaRuntimeException(new SExpressionEvaluationException(
                new RuntimeException("error in consition expression"), "expressionName"));
        errorInTransition.getContext().put(SExceptionContext.TRANSITION_TARGET_FLOWNODE_NAME, "gateway::gatewayName");
        var failure = new BPMFailureService.Failure("scope", errorInTransition);

        var bpmFailure = service.createFlowNodeFailure(flowNodeInstance, failure);

        assertThat(bpmFailure.getContext())
                .isEqualTo("to::gateway::gatewayName//expression::expressionName");
    }

    @Test
    void should_getFlowNodeFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.getFlowNodeFailures(1, 10);

        ArgumentCaptor<SelectListDescriptor<SBPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getFlowNodeFailures");
        assertThat(descriptor.getInputParameter("flowNodeInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SBPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }

    @Test
    void should_getProcessInstanceFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.getProcessInstanceFailures(1, 10);

        ArgumentCaptor<SelectListDescriptor<SBPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getProcessInstanceFailures");
        assertThat(descriptor.getInputParameter("processInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SBPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }

    @Test
    void should_archiveFlowNodeFailures_call_archive_service_with_proper_parameters() throws Exception {
        var failureDate = Instant.now().toEpochMilli();
        doReturn(List.of(SBPMFailure.builder()
                .failureDate(failureDate)
                .flowNodeInstanceId(1L)
                .rootProcessInstanceId(1L)
                .processDefinitionId(1L)
                .processInstanceId(1L)
                .scope("scope")
                .context("context")
                .errorMessage("errorMessage")
                .stackTrace("stackTrace")
                .build())).when(service).getFlowNodeFailures(1L, Integer.MAX_VALUE);
        long archiveDate = Instant.now().toEpochMilli();

        service.archiveFlowNodeFailures(1L, archiveDate);

        var captor = ArgumentCaptor.forClass(ArchiveInsertRecord.class);
        verify(archiveService).recordInserts(eq(archiveDate), captor.capture());

        var archiveInsertRecord = captor.getValue();
        var entity = archiveInsertRecord.getEntity();
        assertThat(entity).isInstanceOf(SABPMFailure.class);
        var archiveBPMFailure = (SABPMFailure) entity;
        assertThat(archiveBPMFailure.getFailureDate()).isEqualTo(failureDate);
        assertThat(archiveBPMFailure.getFlowNodeInstanceId()).isEqualTo(1L);
        assertThat(archiveBPMFailure.getProcessDefinitionId()).isEqualTo(1L);
        assertThat(archiveBPMFailure.getProcessInstanceId()).isEqualTo(1L);
    }

    @Test
    void should_archiveProcessInstanceFailures_call_archive_service_with_proper_parameters() throws Exception {
        var failureDate = Instant.now().toEpochMilli();
        doReturn(List.of(SBPMFailure.builder()
                .failureDate(failureDate)
                .rootProcessInstanceId(1L)
                .processDefinitionId(1L)
                .processInstanceId(1L)
                .scope("scope")
                .context("context")
                .errorMessage("errorMessage")
                .stackTrace("stackTrace")
                .build())).when(service).getProcessInstanceFailures(1L, Integer.MAX_VALUE);
        long archiveDate = Instant.now().toEpochMilli();

        service.archiveProcessInstanceFailures(1L, archiveDate);

        var captor = ArgumentCaptor.forClass(ArchiveInsertRecord.class);
        verify(archiveService).recordInserts(eq(archiveDate), captor.capture());

        var archiveInsertRecord = captor.getValue();
        var entity = archiveInsertRecord.getEntity();
        assertThat(entity).isInstanceOf(SABPMFailure.class);
        var archiveBPMFailure = (SABPMFailure) entity;
        assertThat(archiveBPMFailure.getFailureDate()).isEqualTo(failureDate);
        assertThat(archiveBPMFailure.getRootProcessInstanceId()).isEqualTo(1L);
        assertThat(archiveBPMFailure.getProcessDefinitionId()).isEqualTo(1L);
        assertThat(archiveBPMFailure.getProcessInstanceId()).isEqualTo(1L);
    }

    @Test
    void should_deleteFlowNodeFailures_call_persistence_service_with_proper_parameters() throws Exception {
        doReturn(List.of(SBPMFailure.builder()
                .id(1L)
                .build(),
                SBPMFailure.builder()
                        .id(2L)
                        .build()))
                .when(service).getFlowNodeFailures(1L, Integer.MAX_VALUE);

        service.deleteFlowNodeFailures(1L);

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(persistenceService).delete(captor.capture(), eq(SBPMFailure.class));

        assertThat(captor.getValue()).contains(1L, 2L);
    }

    @Test
    void should_deleteProcessInstanceFailures_call_persistence_service_with_proper_parameters() throws Exception {
        doReturn(List.of(SBPMFailure.builder()
                .id(1L)
                .build(),
                SBPMFailure.builder()
                        .id(2L)
                        .build()))
                .when(service).getProcessInstanceFailures(1L, Integer.MAX_VALUE);

        service.deleteProcessInstanceFailures(1L);

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(persistenceService).delete(captor.capture(), eq(SBPMFailure.class));

        assertThat(captor.getValue()).contains(1L, 2L);
    }

    @Test
    void should_deleteArchivedFlowNodeFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.deleteArchivedFlowNodeFailures(List.of(1L, 2L));

        verify(archiveService).deleteFromQuery("deleteArchivedBPMFailuresByFlowNodeInstanceIds",
                Map.ofEntries(Map.entry("flowNodeInstanceIds", List.of(1L, 2L))));
    }

    @Test
    void should_getArchivedFlowNodeFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.getArchivedFlowNodeFailures(1L, 10);

        ArgumentCaptor<SelectListDescriptor<SABPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getArchivedFlowNodeFailures");
        assertThat(descriptor.getInputParameter("flowNodeInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SABPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }

    @Test
    void should_deleteArchivedProcessInstanceFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.deleteArchivedProcessInstanceFailures(List.of(1L, 2L));

        verify(archiveService).deleteFromQuery("deleteArchivedBPMFailuresByProcessInstanceIds",
                Map.ofEntries(Map.entry("processInstanceIds", List.of(1L, 2L))));
    }

    @Test
    void should_getArchivedProcessInstanceFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.getArchivedProcessInstanceFailures(1L, 10);

        ArgumentCaptor<SelectListDescriptor<SABPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getArchivedProcessInstanceFailures");
        assertThat(descriptor.getInputParameter("processInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SABPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }

    @Test
    void should_getSubProcessInstanceFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.getSubProcessInstanceFailures(1, 10);

        ArgumentCaptor<SelectListDescriptor<SBPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getSubProcessInstanceFailures");
        assertThat(descriptor.getInputParameter("rootProcessInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SBPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }

    @Test
    void should_getArchivedSubProcessInstanceFailures_call_the_right_query_with_proper_parameters() throws Exception {
        service.getArchivedSubProcessInstanceFailures(1L, 10);

        ArgumentCaptor<SelectListDescriptor<SABPMFailure>> captor = ArgumentCaptor.forClass(SelectListDescriptor.class);
        verify(persistenceService).selectList(captor.capture());

        var descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo("getArchivedSubProcessInstanceFailures");
        assertThat(descriptor.getInputParameter("rootProcessInstanceId")).isEqualTo(1L);
        assertThat(descriptor.getReturnType()).isEqualTo(SABPMFailure.class);
        assertThat(descriptor.getStartIndex()).isZero();
        assertThat(descriptor.getPageSize()).isEqualTo(10);
    }
}
