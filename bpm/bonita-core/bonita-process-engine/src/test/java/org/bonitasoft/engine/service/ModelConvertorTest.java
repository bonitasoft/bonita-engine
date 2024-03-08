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
package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.service.ModelConvertor.toUserMembership;
import static org.bonitasoft.engine.tenant.TenantResourceState.INSTALLED;
import static org.bonitasoft.engine.tenant.TenantResourceType.BDM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.execution.state.CompletedActivityState;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.impl.UserMembershipImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.STenantResourceState;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.bonitasoft.engine.tenant.TenantResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelConvertorTest {

    @Mock
    private FlowNodeStateManager manager;

    @Mock
    private FormRequiredAnalyzer formRequiredAnalyzer;

    @Test
    void convertDataInstanceIsTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(true);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertTrue(dataInstance.isTransientData());
    }

    @Test
    void convertDataInstanceIsNotTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(false);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertFalse(dataInstance.isTransientData());
    }

    @Test
    void getProcessInstanceState_conversionOnUnknownStateShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ModelConvertor.getProcessInstanceState("un_known_state"));;
    }

    @Test
    void getProcessInstanceState_conversionOnNullStateShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ModelConvertor.getProcessInstanceState(null));
    }

    @Test
    void convertSUserToUserDoesntShowPassword() {
        final SUser sUser = mock(SUser.class);

        final User testUser = ModelConvertor.toUser(sUser);

        assertThat(testUser.getPassword()).isEmpty();
        verify(sUser, never()).getPassword();
    }

    private DocumentService createdMockedDocumentService() {
        final DocumentService documentService = mock(DocumentService.class);
        lenient().doReturn("url?fileName=document&contentStorageId=123").when(documentService).generateDocumentURL(
                "document",
                "123");
        return documentService;
    }

    private SMappedDocument createMockedDocument() {
        final SMappedDocument documentMapping = mock(SMappedDocument.class);
        doReturn("document").when(documentMapping).getFileName();
        doReturn(123l).when(documentMapping).getDocumentId();
        lenient().doReturn("whateverUrl").when(documentMapping).getUrl();
        return documentMapping;
    }

    @Test
    void toArchivedUserTaskInstance_should_return_the_right_identifiers() {
        final SAUserTaskInstance sInstance = new SAUserTaskInstance();
        sInstance.setRootContainerId(1L);
        sInstance.setParentContainerId(2L);
        sInstance.setLogicalGroup(0, 456789456798L);
        sInstance.setLogicalGroup(1, 1L);
        sInstance.setLogicalGroup(2, 456L);
        sInstance.setLogicalGroup(3, 2L);
        sInstance.setStateId(5);
        final long claimedDate = System.currentTimeMillis();
        sInstance.setClaimedDate(claimedDate);
        sInstance.setPriority(STaskPriority.NORMAL);

        when(manager.getState(5)).thenReturn(new CompletedActivityState());

        final ArchivedUserTaskInstance archivedUserTaskInstance = ModelConvertor.toArchivedUserTaskInstance(sInstance,
                manager);
        assertThat(archivedUserTaskInstance.getProcessDefinitionId()).isEqualTo(456789456798L);
        assertThat(archivedUserTaskInstance.getRootContainerId()).isEqualTo(1L);
        assertThat(archivedUserTaskInstance.getParentContainerId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getProcessInstanceId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getParentActivityInstanceId()).isEqualTo(456L);
        assertThat(archivedUserTaskInstance.getClaimedDate().getTime()).isEqualTo(claimedDate);
    }

    @Test
    void getDocument_from_process_instance_and_name_should_return_a_document_with_generated_url_when_it_has_content() {

        final SMappedDocument documentMapping = createMockedDocument();
        final DocumentService documentService = createdMockedDocumentService();
        doReturn(true).when(documentMapping).hasContent();

        final Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("url?fileName=document&contentStorageId=123", document.getUrl());
    }

    @Test
    void getDocument_from_process_instance_and_name_should_return_a_document_url_when_is_external_url() {

        final SMappedDocument documentMapping = createMockedDocument();
        final DocumentService documentService = createdMockedDocumentService();
        doReturn(false).when(documentMapping).hasContent();

        final Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("whateverUrl", document.getUrl());
    }

    @Test
    void should_convert_server_definition_into_client_definition() {
        final CustomUserInfoDefinitionImpl definition = ModelConvertor.convert(
                SCustomUserInfoDefinition.builder().name("name").id(1).description("description").build());

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    void should_convert_server_value_into_client_value() {
        final CustomUserInfoValue value = ModelConvertor.convert(
                SCustomUserInfoValue.builder().definitionId(2).userId(1).value("value").build());

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }

    @Test
    void should_return_null_when_trying_to_convert_a_null_value() {
        final CustomUserInfoValue value = ModelConvertor.convert((SCustomUserInfoValue) null);

        assertThat(value).isNull();
    }

    @Test
    void toEventTriggerInstance_can_convert_TIMER_Type() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstance(2,
                "eventInstanceName", 69, "jobTriggerName");

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = ModelConvertor
                .toTimerEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    void toTimerEventTriggerInstance_can_convert() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstance(2,
                "eventInstanceName", 69, "jobTriggerName");
        sTimerEventTriggerInstance.setId(9);

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = ModelConvertor
                .toTimerEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    void toProcessInstance() {
        // Given
        final SProcessInstance sProcessInstance = new SProcessInstance();
        sProcessInstance.setCallerId(-1L);
        sProcessInstance.setDescription("description");
        sProcessInstance.setEndDate(1345646L);
        sProcessInstance.setId(98L);
        sProcessInstance.setLastUpdate(8L);
        sProcessInstance.setName("name2");
        sProcessInstance.setProcessDefinitionId(9L);
        sProcessInstance.setRootProcessInstanceId(9745L);
        sProcessInstance.setStartDate(8864564156L);
        sProcessInstance.setStartedBy(46L);
        sProcessInstance.setStartedBySubstitute(962L);
        sProcessInstance.setStateId(4);
        sProcessInstance.setStringIndex1("stringIndex1");
        sProcessInstance.setStringIndex2("stringIndex2");
        sProcessInstance.setStringIndex3("stringIndex3");
        sProcessInstance.setStringIndex4("stringIndex4");
        sProcessInstance.setStringIndex5("stringIndex5");
        sProcessInstance.setTenantId(514L);

        final SProcessDefinitionImpl sProcessDefinition = new SProcessDefinitionImpl("name", "version");
        sProcessDefinition.setStringIndex(1, "label1", null);
        sProcessDefinition.setStringIndex(2, "label2", null);
        sProcessDefinition.setStringIndex(3, "label3", null);
        sProcessDefinition.setStringIndex(4, "label4", null);
        sProcessDefinition.setStringIndex(5, "label5", null);

        // Then
        final ProcessInstance processInstance = ModelConvertor
                .toProcessInstance(sProcessDefinition, sProcessInstance);

        // When
        assertNotNull(processInstance);
        assertEquals(sProcessInstance.getCallerId(), processInstance.getCallerId());
        assertEquals(sProcessInstance.getId(), processInstance.getId());
        assertEquals(sProcessInstance.getDescription(), processInstance.getDescription());
        assertEquals(sProcessInstance.getEndDate(), processInstance.getEndDate().getTime());
        assertEquals(sProcessInstance.getLastUpdate(), processInstance.getLastUpdate().getTime());
        assertEquals(sProcessInstance.getName(), processInstance.getName());
        assertEquals(sProcessInstance.getProcessDefinitionId(), processInstance.getProcessDefinitionId());
        assertEquals(sProcessInstance.getRootProcessInstanceId(), processInstance.getRootProcessInstanceId());
        assertEquals(sProcessInstance.getStartDate(), processInstance.getStartDate().getTime());
        assertEquals(sProcessInstance.getStartedBy(), processInstance.getStartedBy());
        assertEquals(sProcessInstance.getStartedBySubstitute(), processInstance.getStartedBySubstitute());
        assertEquals(sProcessDefinition.getStringIndexLabel(1), processInstance.getStringIndexLabel(1));
        assertEquals(sProcessInstance.getStringIndex1(), processInstance.getStringIndex1());
        assertEquals(sProcessDefinition.getStringIndexLabel(2), processInstance.getStringIndexLabel(2));
        assertEquals(sProcessInstance.getStringIndex2(), processInstance.getStringIndex2());
        assertEquals(sProcessDefinition.getStringIndexLabel(3), processInstance.getStringIndexLabel(3));
        assertEquals(sProcessInstance.getStringIndex3(), processInstance.getStringIndex3());
        assertEquals(sProcessDefinition.getStringIndexLabel(4), processInstance.getStringIndexLabel(4));
        assertEquals(sProcessInstance.getStringIndex4(), processInstance.getStringIndex4());
        assertEquals(sProcessDefinition.getStringIndexLabel(5), processInstance.getStringIndexLabel(5));
        assertEquals(sProcessInstance.getStringIndex5(), processInstance.getStringIndex5());
    }

    @Test
    void toProcessInstance_wit_missing_process_definition() {
        // Given
        final SProcessInstance sProcessInstance = new SProcessInstance();
        sProcessInstance.setCallerId(-1L);
        sProcessInstance.setDescription("description");
        sProcessInstance.setEndDate(1345646L);
        sProcessInstance.setId(98L);
        sProcessInstance.setLastUpdate(8L);
        sProcessInstance.setName("name2");
        sProcessInstance.setProcessDefinitionId(9L);
        sProcessInstance.setRootProcessInstanceId(9745L);
        sProcessInstance.setStartDate(8864564156L);
        sProcessInstance.setStartedBy(46L);
        sProcessInstance.setStartedBySubstitute(962L);
        sProcessInstance.setStateId(4);
        sProcessInstance.setStringIndex1("stringIndex1");
        sProcessInstance.setStringIndex2("stringIndex2");
        sProcessInstance.setStringIndex3("stringIndex3");
        sProcessInstance.setStringIndex4("stringIndex4");
        sProcessInstance.setStringIndex5("stringIndex5");
        sProcessInstance.setTenantId(514L);

        // Then
        final ProcessInstance processInstance = ModelConvertor
                .toProcessInstance(null, sProcessInstance);

        // When
        assertNotNull(processInstance);
        assertEquals(sProcessInstance.getCallerId(), processInstance.getCallerId());
        assertEquals(sProcessInstance.getId(), processInstance.getId());
        assertEquals(sProcessInstance.getDescription(), processInstance.getDescription());
        assertEquals(sProcessInstance.getEndDate(), processInstance.getEndDate().getTime());
        assertEquals(sProcessInstance.getLastUpdate(), processInstance.getLastUpdate().getTime());
        assertEquals(sProcessInstance.getName(), processInstance.getName());
        assertEquals(sProcessInstance.getProcessDefinitionId(), processInstance.getProcessDefinitionId());
        assertEquals(sProcessInstance.getRootProcessInstanceId(), processInstance.getRootProcessInstanceId());
        assertEquals(sProcessInstance.getStartDate(), processInstance.getStartDate().getTime());
        assertEquals(sProcessInstance.getStartedBy(), processInstance.getStartedBy());
        assertEquals(sProcessInstance.getStartedBySubstitute(), processInstance.getStartedBySubstitute());
        assertNull(processInstance.getStringIndexLabel(1));
        assertEquals(sProcessInstance.getStringIndex1(), processInstance.getStringIndex1());
        assertNull(processInstance.getStringIndexLabel(2));
        assertEquals(sProcessInstance.getStringIndex2(), processInstance.getStringIndex2());
        assertNull(processInstance.getStringIndexLabel(3));
        assertEquals(sProcessInstance.getStringIndex3(), processInstance.getStringIndex3());
        assertNull(processInstance.getStringIndexLabel(4));
        assertEquals(sProcessInstance.getStringIndex4(), processInstance.getStringIndex4());
        assertNull(processInstance.getStringIndexLabel(5));
        assertEquals(sProcessInstance.getStringIndex5(), processInstance.getStringIndex5());
    }

    @Test
    void toArchivedProcessInstance_can_convert() {
        // Given
        final SAProcessInstance saProcessInstance = new SAProcessInstance();
        saProcessInstance.setCallerId(-1L);
        saProcessInstance.setDescription("description");
        saProcessInstance.setEndDate(1345646L);
        saProcessInstance.setId(98L);
        saProcessInstance.setLastUpdate(8L);
        saProcessInstance.setName("name2");
        saProcessInstance.setProcessDefinitionId(9L);
        saProcessInstance.setRootProcessInstanceId(9745L);
        saProcessInstance.setSourceObjectId(741L);
        saProcessInstance.setStartDate(8864564156L);
        saProcessInstance.setStartedBy(46L);
        saProcessInstance.setStartedBySubstitute(962L);
        saProcessInstance.setStateId(4);
        saProcessInstance.setStringIndex1("stringIndex1");
        saProcessInstance.setStringIndex2("stringIndex2");
        saProcessInstance.setStringIndex3("stringIndex3");
        saProcessInstance.setStringIndex4("stringIndex4");
        saProcessInstance.setStringIndex5("stringIndex5");
        saProcessInstance.setTenantId(514L);

        final SProcessDefinitionImpl sProcessDefinition = new SProcessDefinitionImpl("name", "version");
        sProcessDefinition.setStringIndex(1, "label1", null);
        sProcessDefinition.setStringIndex(2, "label2", null);
        sProcessDefinition.setStringIndex(3, "label3", null);
        sProcessDefinition.setStringIndex(4, "label4", null);
        sProcessDefinition.setStringIndex(5, "label5", null);

        // Then
        final ArchivedProcessInstance archivedProcessInstance = ModelConvertor
                .toArchivedProcessInstance(saProcessInstance, sProcessDefinition);

        // When
        assertNotNull(archivedProcessInstance);
        assertEquals(saProcessInstance.getCallerId(), archivedProcessInstance.getCallerId());
        assertEquals(saProcessInstance.getId(), archivedProcessInstance.getId());
        assertEquals(saProcessInstance.getDescription(), archivedProcessInstance.getDescription());
        assertEquals(saProcessInstance.getEndDate(), archivedProcessInstance.getEndDate().getTime());
        assertEquals(saProcessInstance.getLastUpdate(), archivedProcessInstance.getLastUpdate().getTime());
        assertEquals(saProcessInstance.getName(), archivedProcessInstance.getName());
        assertEquals(saProcessInstance.getProcessDefinitionId(), archivedProcessInstance.getProcessDefinitionId());
        assertEquals(saProcessInstance.getRootProcessInstanceId(), archivedProcessInstance.getRootProcessInstanceId());
        assertEquals(saProcessInstance.getSourceObjectId(), archivedProcessInstance.getSourceObjectId());
        assertEquals(saProcessInstance.getStartDate(), archivedProcessInstance.getStartDate().getTime());
        assertEquals(saProcessInstance.getStartedBy(), archivedProcessInstance.getStartedBy());
        assertEquals(saProcessInstance.getStartedBySubstitute(), archivedProcessInstance.getStartedBySubstitute());
        assertEquals(saProcessInstance.getStateId(), archivedProcessInstance.getStateId());
        assertEquals(sProcessDefinition.getStringIndexLabel(1), archivedProcessInstance.getStringIndexLabel(1));
        assertEquals(saProcessInstance.getStringIndex1(), archivedProcessInstance.getStringIndexValue(1));
        assertEquals(sProcessDefinition.getStringIndexLabel(2), archivedProcessInstance.getStringIndexLabel(2));
        assertEquals(saProcessInstance.getStringIndex2(), archivedProcessInstance.getStringIndexValue(2));
        assertEquals(sProcessDefinition.getStringIndexLabel(3), archivedProcessInstance.getStringIndexLabel(3));
        assertEquals(saProcessInstance.getStringIndex3(), archivedProcessInstance.getStringIndexValue(3));
        assertEquals(sProcessDefinition.getStringIndexLabel(4), archivedProcessInstance.getStringIndexLabel(4));
        assertEquals(saProcessInstance.getStringIndex4(), archivedProcessInstance.getStringIndexValue(4));
        assertEquals(sProcessDefinition.getStringIndexLabel(5), archivedProcessInstance.getStringIndexLabel(5));
        assertEquals(saProcessInstance.getStringIndex5(), archivedProcessInstance.getStringIndexValue(5));
    }

    @Test
    void toArchivedProcessInstance_can_convert_when_process_definition_is_missing() {
        // Given
        final SAProcessInstance saProcessInstance = new SAProcessInstance();
        saProcessInstance.setCallerId(-1L);
        saProcessInstance.setDescription("description");
        saProcessInstance.setEndDate(1345646L);
        saProcessInstance.setId(98L);
        saProcessInstance.setLastUpdate(8L);
        saProcessInstance.setName("name2");
        saProcessInstance.setProcessDefinitionId(9L);
        saProcessInstance.setRootProcessInstanceId(9745L);
        saProcessInstance.setSourceObjectId(741L);
        saProcessInstance.setStartDate(8864564156L);
        saProcessInstance.setStartedBy(46L);
        saProcessInstance.setStartedBySubstitute(962L);
        saProcessInstance.setStateId(4);
        saProcessInstance.setStringIndex1("stringIndex1");
        saProcessInstance.setStringIndex2("stringIndex2");
        saProcessInstance.setStringIndex3("stringIndex3");
        saProcessInstance.setStringIndex4("stringIndex4");
        saProcessInstance.setStringIndex5("stringIndex5");
        saProcessInstance.setTenantId(514L);

        // Then
        final ArchivedProcessInstance archivedProcessInstance = ModelConvertor
                .toArchivedProcessInstance(saProcessInstance, null);

        // When
        assertNotNull(archivedProcessInstance);
        assertEquals(saProcessInstance.getCallerId(), archivedProcessInstance.getCallerId());
        assertEquals(saProcessInstance.getId(), archivedProcessInstance.getId());
        assertEquals(saProcessInstance.getDescription(), archivedProcessInstance.getDescription());
        assertEquals(saProcessInstance.getEndDate(), archivedProcessInstance.getEndDate().getTime());
        assertEquals(saProcessInstance.getLastUpdate(), archivedProcessInstance.getLastUpdate().getTime());
        assertEquals(saProcessInstance.getName(), archivedProcessInstance.getName());
        assertEquals(saProcessInstance.getProcessDefinitionId(), archivedProcessInstance.getProcessDefinitionId());
        assertEquals(saProcessInstance.getRootProcessInstanceId(), archivedProcessInstance.getRootProcessInstanceId());
        assertEquals(saProcessInstance.getSourceObjectId(), archivedProcessInstance.getSourceObjectId());
        assertEquals(saProcessInstance.getStartDate(), archivedProcessInstance.getStartDate().getTime());
        assertEquals(saProcessInstance.getStartedBy(), archivedProcessInstance.getStartedBy());
        assertEquals(saProcessInstance.getStartedBySubstitute(), archivedProcessInstance.getStartedBySubstitute());
        assertEquals(saProcessInstance.getStateId(), archivedProcessInstance.getStateId());
        assertNull(archivedProcessInstance.getStringIndexLabel(1));
        assertEquals(saProcessInstance.getStringIndex1(), archivedProcessInstance.getStringIndexValue(1));
        assertNull(archivedProcessInstance.getStringIndexLabel(2));
        assertEquals(saProcessInstance.getStringIndex2(), archivedProcessInstance.getStringIndexValue(2));
        assertNull(archivedProcessInstance.getStringIndexLabel(3));
        assertEquals(saProcessInstance.getStringIndex3(), archivedProcessInstance.getStringIndexValue(3));
        assertNull(archivedProcessInstance.getStringIndexLabel(4));
        assertEquals(saProcessInstance.getStringIndex4(), archivedProcessInstance.getStringIndexValue(4));
        assertNull(archivedProcessInstance.getStringIndexLabel(5));
        assertEquals(saProcessInstance.getStringIndex5(), archivedProcessInstance.getStringIndexValue(5));
    }

    @Test
    void toFormMapping_can_convert() {
        // Given
        SFormMapping sFormMapping = new SFormMapping();
        sFormMapping.setId(555l);
        //        sFormMapping.setForm("myForm1");
        sFormMapping.setType(FormMappingType.TASK.getId());
        sFormMapping.setTask("myTask");
        sFormMapping.setProcessDefinitionId(666l);
        sFormMapping.setPageMapping(new SPageMapping());

        // Then
        FormMapping formMapping = ModelConvertor.toFormMapping(sFormMapping, formRequiredAnalyzer);

        // When
        assertThat(formMapping).isNotNull();
        assertThat(formMapping.getId()).isEqualTo(555l);
        assertThat(formMapping.getType()).isEqualTo(FormMappingType.TASK);
        //        assertThat(formMapping.getTarget()).isEqualTo(FormMappingTarget.LEGACY);
        //        assertThat(formMapping.getForm()).isEqualTo("myForm1");
        assertThat(formMapping.getTask()).isEqualTo("myTask");
        assertThat(formMapping.getProcessDefinitionId()).isEqualTo(666l);

    }

    @Test
    void toFormMappings_can_convert() {
        // Given
        SFormMapping sFormMapping = new SFormMapping();
        sFormMapping.setId(555l);
        //        sFormMapping.setForm("myForm1");
        sFormMapping.setType(FormMappingType.TASK.getId());
        sFormMapping.setTask("myTask");
        sFormMapping.setProcessDefinitionId(666l);
        sFormMapping.setPageMapping(new SPageMapping());

        // Then
        List<FormMapping> formMapping = ModelConvertor.toFormMappings(Arrays.<SFormMapping> asList(sFormMapping),
                formRequiredAnalyzer);

        // When
        assertThat(formMapping).hasSize(1);
        assertThat(formMapping.get(0).getId()).isEqualTo(555l);
        assertThat(formMapping.get(0).getType()).isEqualTo(FormMappingType.TASK);
        //        assertThat(formMapping.get(0).getTarget()).isEqualTo(FormMappingTarget.URL);
        //        assertThat(formMapping.get(0).getForm()).isEqualTo("myForm1");
        assertThat(formMapping.get(0).getTask()).isEqualTo("myTask");
        assertThat(formMapping.get(0).getProcessDefinitionId()).isEqualTo(666l);

    }

    @Test
    void toFormMapping_can_convert_null() {
        // Given

        // Then
        FormMapping formMapping = ModelConvertor.toFormMapping(null, formRequiredAnalyzer);

        // When
        assertThat(formMapping).isNull();

    }

    @Test
    void convertSContractDefinition() {
        //given
        final InputDefinition expectedSimpleInput = new InputDefinitionImpl("name", Type.TEXT, "description");
        final InputDefinition expectedComplexInput = new InputDefinitionImpl("complex input", "complex description",
                Arrays.asList(expectedSimpleInput));
        final ConstraintDefinition expectedRule = new ConstraintDefinitionImpl("name", "expression", "explanation");
        expectedRule.getInputNames().add("input1");
        expectedRule.getInputNames().add("input2");

        //when
        final SContractDefinition contractDefinition = new SContractDefinitionImpl();
        final SConstraintDefinition sRule = new SConstraintDefinitionImpl(expectedRule);
        final SInputDefinition sSimpleInput = new SInputDefinitionImpl(expectedSimpleInput);
        final SInputDefinition sComplexInput = new SInputDefinitionImpl(expectedComplexInput);

        contractDefinition.getConstraints().add(sRule);
        contractDefinition.getInputDefinitions().add(sSimpleInput);
        contractDefinition.getInputDefinitions().add(sComplexInput);

        final ContractDefinition contract = ModelConvertor.toContract(contractDefinition);

        //then
        assertThat(contract.getConstraints()).as("should convert rules").containsExactly(expectedRule);
        assertThat(contract.getInputs()).as("should convert inputs").containsExactly(expectedSimpleInput,
                expectedComplexInput);
    }

    @Test
    void convertNullSContractDefinition() {
        //when
        final ContractDefinition contract = ModelConvertor.toContract(null);

        //then
        assertThat(contract).as("contract null").isNull();
    }

    @Test
    void convertMultipleSContractDefinition() {
        //given
        final InputDefinition expectedSimpleInput = new InputDefinitionImpl("name", Type.TEXT, "description", true);
        final InputDefinition expectedComplexInput = new InputDefinitionImpl("complex input", "complex description",
                true,
                Collections.singletonList(expectedSimpleInput));
        final InputDefinition expectedComplexWithComplexInput = new InputDefinitionImpl("complex in complex",
                "complex description", true,
                null, Collections.singletonList(expectedComplexInput));

        final ConstraintDefinition expectedRule = new ConstraintDefinitionImpl("name", "expression", "explanation");
        expectedRule.getInputNames().add("input1");
        expectedRule.getInputNames().add("input2");

        //when
        final SContractDefinition contractDefinition = new SContractDefinitionImpl();
        final SConstraintDefinition sRule = new SConstraintDefinitionImpl(expectedRule);
        final SInputDefinition sSimpleInput = new SInputDefinitionImpl(expectedSimpleInput);
        final SInputDefinition sComplexInput = new SInputDefinitionImpl(expectedComplexWithComplexInput);

        contractDefinition.getConstraints().add(sRule);
        contractDefinition.getInputDefinitions().add(sSimpleInput);
        contractDefinition.getInputDefinitions().add(sComplexInput);

        final ContractDefinition contract = ModelConvertor.toContract(contractDefinition);

        //then
        assertThat(contract.getConstraints()).as("should convert rules").containsExactly(expectedRule);
        assertThat(contract.getInputs()).as("should convert simple inputs").containsExactly(expectedSimpleInput,
                expectedComplexWithComplexInput);
    }

    SProcessSimpleRefBusinessDataInstance createProcessSimpleDataReference(String name, long processInstanceId,
            String type, long businessDataId) {
        SProcessSimpleRefBusinessDataInstance sProcessSimpleRefBusinessDataInstance = new SProcessSimpleRefBusinessDataInstance();
        sProcessSimpleRefBusinessDataInstance.setName(name);
        sProcessSimpleRefBusinessDataInstance.setProcessInstanceId(processInstanceId);
        sProcessSimpleRefBusinessDataInstance.setDataClassName(type);
        sProcessSimpleRefBusinessDataInstance.setDataId(businessDataId);
        return sProcessSimpleRefBusinessDataInstance;
    }

    SProcessMultiRefBusinessDataInstance createProcessMultipleDataReference(String name, long processInstanceId,
            String type, List<Long> dataIds) {
        SProcessMultiRefBusinessDataInstance sProcessSimpleRefBusinessDataInstance = new SProcessMultiRefBusinessDataInstance();
        sProcessSimpleRefBusinessDataInstance.setName(name);
        sProcessSimpleRefBusinessDataInstance.setProcessInstanceId(processInstanceId);
        sProcessSimpleRefBusinessDataInstance.setDataClassName(type);
        sProcessSimpleRefBusinessDataInstance.setDataIds(dataIds);
        return sProcessSimpleRefBusinessDataInstance;
    }

    @Test
    void convert_multiple_business_data() {
        BusinessDataReference businessDataReference = ModelConvertor
                .toBusinessDataReference(createProcessSimpleDataReference("myBData", 157l, "theType", 5555l));

        assertThat(businessDataReference).isEqualTo(new SimpleBusinessDataReferenceImpl("myBData", "theType", 5555l));
    }

    @Test
    void convert_simple_business_data() {
        BusinessDataReference businessDataReference = ModelConvertor
                .toBusinessDataReference(createProcessMultipleDataReference("myBData", 157l, "theType",
                        Arrays.asList(5555l, 5556l, 5557l)));

        assertThat(businessDataReference).isEqualTo(
                new MultipleBusinessDataReferenceImpl("myBData", "theType", Arrays.asList(5555l, 5556l, 5557l)));
    }

    @Test
    void convert_null_business_data() {
        BusinessDataReference businessDataReference = ModelConvertor.toBusinessDataReference(null);

        assertThat(businessDataReference).isNull();
    }

    @Test
    void toArchivedFlownodeInstance_should_convert_reachStateDate_for_gateways() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SAGatewayInstance saFlowNode = new SAGatewayInstance();
        final long reachedStateDate = 4534311114L;
        saFlowNode.setReachedStateDate(reachedStateDate);
        assertThat(ModelConvertor.toArchivedFlowNodeInstance(saFlowNode, flowNodeStateManager).getReachedStateDate())
                .isEqualTo(new Date(reachedStateDate));
    }

    @Test
    void toArchivedFlownodeInstance_should_convert_reachStateDate_for_receivetask() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SAReceiveTaskInstance receiveTaskInstance = new SAReceiveTaskInstance();
        assertThat(ModelConvertor.toArchivedFlowNodeInstance(receiveTaskInstance, flowNodeStateManager)
                .getReachedStateDate()).isNotNull();
    }

    @Test
    void toArchivedFlownodeInstance_should_convert_reachStateDate_for_sendtask() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SASendTaskInstance sendTaskInstance = new SASendTaskInstance();
        assertThat(
                ModelConvertor.toArchivedFlowNodeInstance(sendTaskInstance, flowNodeStateManager).getReachedStateDate())
                        .isNotNull();
    }

    @Test
    void toArchivedFlownodeInstance_should_convert_reachStateDate_for_usertask() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SAUserTaskInstance saFlowNode = new SAUserTaskInstance();
        saFlowNode.setPriority(STaskPriority.UNDER_NORMAL);
        assertThat(ModelConvertor.toArchivedFlowNodeInstance(saFlowNode, flowNodeStateManager).getReachedStateDate())
                .isNotNull();
    }

    @Test
    void toFlownodeInstance_should_convert_reachStateDate_for_usertask() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SUserTaskInstance sFlowNode = new SUserTaskInstance();
        sFlowNode.setPriority(STaskPriority.UNDER_NORMAL);
        assertThat(ModelConvertor.toFlowNodeInstance(sFlowNode, flowNodeStateManager).getReachedStateDate())
                .isNotNull();
    }

    @Test
    void toArchivedFlownodeInstance_should_convert_lastUpdateDate_for_usertask() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SAUserTaskInstance saFlowNode = new SAUserTaskInstance();
        saFlowNode.setPriority(STaskPriority.UNDER_NORMAL);
        final long lastUpdateDate = 5746354125555L;
        saFlowNode.setLastUpdateDate(lastUpdateDate);
        assertThat(ModelConvertor.toArchivedFlowNodeInstance(saFlowNode, flowNodeStateManager).getLastUpdateDate())
                .isEqualTo(new Date(lastUpdateDate));
    }

    @Test
    void toFlownodeInstance_should_convert_lastUpdateDate_for_usertask() {
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SUserTaskInstance sFlowNode = new SUserTaskInstance();
        sFlowNode.setPriority(STaskPriority.UNDER_NORMAL);
        assertThat(ModelConvertor.toFlowNodeInstance(sFlowNode, flowNodeStateManager).getLastUpdateDate()).isNotNull();
        assertThat(ModelConvertor.toFlowNodeInstance(new SGatewayInstance(), flowNodeStateManager).getLastUpdateDate())
                .isNotNull();
    }

    @Test
    void should_convert_expected_end_date() {
        //given
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SUserTaskInstance urgentSTask = new SUserTaskInstance();
        urgentSTask.setExpectedEndDate(15L);
        urgentSTask.setPriority(STaskPriority.UNDER_NORMAL);

        final SUserTaskInstance takeYourTimeSTask = new SUserTaskInstance();
        takeYourTimeSTask.setPriority(STaskPriority.UNDER_NORMAL);

        //when
        final UserTaskInstance urgentTask = ModelConvertor.toUserTaskInstance(urgentSTask, flowNodeStateManager);
        final UserTaskInstance takeYourTimeTask = ModelConvertor.toUserTaskInstance(takeYourTimeSTask,
                flowNodeStateManager);

        //then
        assertThat(urgentTask.getExpectedEndDate()).isEqualTo(new Date(15L));
        assertThat(takeYourTimeTask.getExpectedEndDate()).isNull();
    }

    @Test
    void should_convert_archived_expected_end_date() {
        //given
        final FlowNodeStateManager flowNodeStateManager = mock(FlowNodeStateManager.class);
        doReturn(mock(FlowNodeState.class)).when(flowNodeStateManager).getState(anyInt());

        final SAUserTaskInstance urgentASTask = new SAUserTaskInstance();
        urgentASTask.setExpectedEndDate(15L);
        urgentASTask.setPriority(STaskPriority.UNDER_NORMAL);

        final SAUserTaskInstance takeYourTimeASTask = new SAUserTaskInstance();
        takeYourTimeASTask.setPriority(STaskPriority.UNDER_NORMAL);

        //when
        final ArchivedUserTaskInstance urgentATask = ModelConvertor.toArchivedUserTaskInstance(urgentASTask,
                flowNodeStateManager);
        final ArchivedUserTaskInstance takeYourTimeATask = ModelConvertor.toArchivedUserTaskInstance(takeYourTimeASTask,
                flowNodeStateManager);

        //then
        assertThat(urgentATask.getExpectedEndDate()).isEqualTo(new Date(15L));
        assertThat(takeYourTimeATask.getExpectedEndDate()).isNull();
    }

    @Test
    void should_set_the_parentPath_when_creating_a_UserMembership() {
        //given
        SUserMembership sUserMembership = new SUserMembership(257L, 157L, 357L, 457L, 557L, 190119993L,
                "dummy rolename", "dummy groupname", "dummy username", "Bonita/dummy");

        //when
        UserMembershipImpl userMembership = (UserMembershipImpl) toUserMembership(sUserMembership);

        //then
        assertThat(userMembership.getGroupParentPath()).isNotNull();
        assertThat(userMembership.getGroupParentPath()).isEqualToIgnoringCase("Bonita/dummy");
    }

    @Test
    void toTenantResource_should_convert_all_fields() {
        // given:
        final OffsetDateTime offsetDateTime = OffsetDateTime.parse("1970-01-01T00:00:15Z");
        String resourceName = "any";
        STenantResourceLight sTenantResource = new STenantResourceLight(resourceName,
                TenantResourceType.BDM, 12L, offsetDateTime.toInstant().toEpochMilli(), STenantResourceState.INSTALLED);

        // when:
        TenantResource tenantResource = ModelConvertor.toTenantResource(sTenantResource);

        // then:
        assertThat(tenantResource.getName()).isEqualTo(resourceName);
        assertThat(tenantResource.getLastUpdatedBy()).isEqualTo(12L);
        assertThat(tenantResource.getLastUpdateDate()).isEqualTo(offsetDateTime);
        assertThat(tenantResource.getState()).isEqualTo(INSTALLED);
        assertThat(tenantResource.getType()).isEqualTo(BDM);
    }
}
