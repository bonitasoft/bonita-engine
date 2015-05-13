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
 */
package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoDefinition;
import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAUserTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowErrorEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowSignalEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowErrorEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowMessageEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowSignalEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.execution.state.CompletedActivityStateImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.page.impl.SPageMappingImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModelConvertorTest {

    @Mock
    private FlowNodeStateManager manager;

    @Test
    public void convertDataInstanceIsTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(true);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertTrue(dataInstance.isTransientData());
    }

    @Test
    public void convertDataInstanceIsNotTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(false);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertFalse(dataInstance.isTransientData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnUnknownStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState("un_known_state");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnNullStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState(null);
    }

    @Test
    public void convertSUserToUserDoesntShowPassword() {
        final SUser sUser = mock(SUser.class);

        final User testUser = ModelConvertor.toUser(sUser);

        assertThat(testUser.getPassword()).isEmpty();
        verify(sUser, never()).getPassword();
    }

    private DocumentService createdMockedDocumentService() {
        final DocumentService documentService = mock(DocumentService.class);
        doReturn("url?fileName=document&contentStorageId=123").when(documentService).generateDocumentURL("document", "123");
        return documentService;
    }

    private SMappedDocument createMockedDocument() {
        final SMappedDocument documentMapping = mock(SMappedDocument.class);
        doReturn("document").when(documentMapping).getFileName();
        doReturn(123l).when(documentMapping).getDocumentId();
        doReturn("whateverUrl").when(documentMapping).getUrl();
        return documentMapping;
    }

    @Test
    public void toArchivedUserTaskInstance_sould_return_the_right_identifiers() {
        final SAUserTaskInstanceImpl sInstance = new SAUserTaskInstanceImpl();
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

        when(manager.getState(5)).thenReturn(new CompletedActivityStateImpl());

        final ArchivedUserTaskInstance archivedUserTaskInstance = ModelConvertor.toArchivedUserTaskInstance(sInstance, manager);
        assertThat(archivedUserTaskInstance.getProcessDefinitionId()).isEqualTo(456789456798L);
        assertThat(archivedUserTaskInstance.getRootContainerId()).isEqualTo(1L);
        assertThat(archivedUserTaskInstance.getParentContainerId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getProcessInstanceId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getParentActivityInstanceId()).isEqualTo(456L);
        assertThat(archivedUserTaskInstance.getClaimedDate().getTime()).isEqualTo(claimedDate);
    }

    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_with_generated_url_when_it_has_content() {

        final SMappedDocument documentMapping = createMockedDocument();
        final DocumentService documentService = createdMockedDocumentService();
        doReturn(true).when(documentMapping).hasContent();

        final Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("url?fileName=document&contentStorageId=123", document.getUrl());
    }

    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_url_when_is_external_url() {

        final SMappedDocument documentMapping = createMockedDocument();
        final DocumentService documentService = createdMockedDocumentService();
        doReturn(false).when(documentMapping).hasContent();

        final Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("whateverUrl", document.getUrl());
    }

    @Test
    public void should_convert_server_definition_into_client_definition() {
        final CustomUserInfoDefinitionImpl definition = ModelConvertor.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void should_convert_server_value_into_client_value() {
        final CustomUserInfoValue value = ModelConvertor.convert(
                new DummySCustomUserInfoValue(2L, 2L, 1L, "value"));

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }

    @Test
    public void should_return_null_when_trying_to_convert_a_null_value() {
        final CustomUserInfoValue value = ModelConvertor.convert((SCustomUserInfoValue) null);

        assertThat(value).isNull();
    }

    @Test
    public void toEventTriggerInstance_cant_convert_ERROR_Type() {
        // Given
        final SThrowErrorEventTriggerInstance sEventTriggerInstance = new SThrowErrorEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_SIGNAL_Type() {
        // Given
        final SThrowMessageEventTriggerInstance sEventTriggerInstance = new SThrowMessageEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_MESSAGE_Type() {
        // Given
        final SThrowSignalEventTriggerInstance sEventTriggerInstance = new SThrowSignalEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_TERMINATE_Type() {
        // Given
        final SEventTriggerInstance sEventTriggerInstance = new SEventTriggerInstanceImpl() {

            private static final long serialVersionUID = 514899463254242741L;

            @Override
            public String getDiscriminator() {
                return null;
            }

            @Override
            public SEventTriggerType getEventTriggerType() {
                return SEventTriggerType.TERMINATE;
            }
        };

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_can_convert_TIMER_Type() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstanceImpl(2, "eventInstanceName", 69, "jobTriggerName");

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = (TimerEventTriggerInstance) ModelConvertor.toEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    public void toTimerEventTriggerInstance_can_convert() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstanceImpl(2, "eventInstanceName", 69, "jobTriggerName");
        sTimerEventTriggerInstance.setId(9);

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = ModelConvertor.toTimerEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    public void toArchivedProcessInstance_can_convert() {
        // Given
        final SAProcessInstanceImpl saProcessInstance = new SAProcessInstanceImpl();
        saProcessInstance.setCallerId(-1L);
        saProcessInstance.setDescription("description");
        saProcessInstance.setEndDate(1345646L);
        saProcessInstance.setId(98L);
        saProcessInstance.setLastUpdate(8L);
        saProcessInstance.setMigrationPlanId(9L);
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
        final ArchivedProcessInstance archivedProcessInstance = ModelConvertor.toArchivedProcessInstance(saProcessInstance, sProcessDefinition);

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
    public void toFormMapping_can_convert() {
        // Given
        SFormMappingImpl sFormMapping = new SFormMappingImpl();
        sFormMapping.setId(555l);
        //        sFormMapping.setForm("myForm1");
        sFormMapping.setType(FormMappingType.TASK.getId());
        sFormMapping.setTask("myTask");
        sFormMapping.setProcessDefinitionId(666l);
        sFormMapping.setPageMapping(new SPageMappingImpl());

        // Then
        FormMapping formMapping = ModelConvertor.toFormMapping(sFormMapping);

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
    public void toFormMappings_can_convert() {
        // Given
        SFormMappingImpl sFormMapping = new SFormMappingImpl();
        sFormMapping.setId(555l);
        //        sFormMapping.setForm("myForm1");
        sFormMapping.setType(FormMappingType.TASK.getId());
        sFormMapping.setTask("myTask");
        sFormMapping.setProcessDefinitionId(666l);
        sFormMapping.setPageMapping(new SPageMappingImpl());

        // Then
        List<FormMapping> formMapping = ModelConvertor.toFormMappings(Arrays.<SFormMapping>asList(sFormMapping));

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
    public void toFormMapping_can_convert_null() {
        // Given

        // Then
        FormMapping formMapping = ModelConvertor.toFormMapping(null);

        // When
        assertThat(formMapping).isNull();

    }

    @Test
    public void convertSContractDefinition() {
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
        assertThat(contract.getInputs()).as("should convert inputs").containsExactly(expectedSimpleInput, expectedComplexInput);
    }

    @Test
    public void convertMultipleSContractDefinition() {
        //given
        final InputDefinition expectedSimpleInput = new InputDefinitionImpl("name", Type.TEXT, "description", true);
        final InputDefinition expectedComplexInput = new InputDefinitionImpl("complex input", "complex description", true,
                Collections.singletonList(expectedSimpleInput));
        final InputDefinition expectedComplexWithComplexInput = new InputDefinitionImpl("complex in complex", "complex description", true,
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
        assertThat(contract.getInputs()).as("should convert simple inputs").containsExactly(expectedSimpleInput, expectedComplexWithComplexInput);
    }

    SProcessSimpleRefBusinessDataInstance createProcessSimpleDataReference(String name, long processInstanceId, String type, long businessDataId) {
        SProcessSimpleRefBusinessDataInstanceImpl sProcessSimpleRefBusinessDataInstance = new SProcessSimpleRefBusinessDataInstanceImpl();
        sProcessSimpleRefBusinessDataInstance.setName(name);
        sProcessSimpleRefBusinessDataInstance.setProcessInstanceId(processInstanceId);
        sProcessSimpleRefBusinessDataInstance.setDataClassName(type);
        sProcessSimpleRefBusinessDataInstance.setDataId(businessDataId);
        return sProcessSimpleRefBusinessDataInstance;
    }

    SProcessMultiRefBusinessDataInstance createProcessMultipleDataReference(String name, long processInstanceId, String type, List<Long> dataIds) {
        SProcessMultiRefBusinessDataInstanceImpl sProcessSimpleRefBusinessDataInstance = new SProcessMultiRefBusinessDataInstanceImpl();
        sProcessSimpleRefBusinessDataInstance.setName(name);
        sProcessSimpleRefBusinessDataInstance.setProcessInstanceId(processInstanceId);
        sProcessSimpleRefBusinessDataInstance.setDataClassName(type);
        sProcessSimpleRefBusinessDataInstance.setDataIds(dataIds);
        return sProcessSimpleRefBusinessDataInstance;
    }

    @Test
    public void convert_multiple_business_data() {
        BusinessDataReference businessDataReference = ModelConvertor
                .toBusinessDataReference(createProcessSimpleDataReference("myBData", 157l, "theType", 5555l));

        assertThat(businessDataReference).isEqualTo(new SimpleBusinessDataReferenceImpl("myBData", "theType", 5555l));
    }

    @Test
    public void convert_simple_business_data() {
        BusinessDataReference businessDataReference = ModelConvertor.toBusinessDataReference(createProcessMultipleDataReference("myBData", 157l, "theType",
                Arrays.asList(5555l, 5556l, 5557l)));

        assertThat(businessDataReference).isEqualTo(new MultipleBusinessDataReferenceImpl("myBData", "theType", Arrays.asList(5555l, 5556l, 5557l)));
    }

    @Test
    public void convert_null_business_data() {
        BusinessDataReference businessDataReference = ModelConvertor.toBusinessDataReference(null);

        assertThat(businessDataReference).isNull();
    }

}
