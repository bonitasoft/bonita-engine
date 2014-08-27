package org.bonitasoft.engine.api.impl;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.impl.IntegerDataInstanceImpl;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.exception.SProcessDocumentDeletionException;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.impl.SDocumentMappingImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ProcessAPIImplTest {

    final long tenantId = 1;

    private static final long ACTOR_ID = 100;

    private static final long PROCESS_DEFINITION_ID = 110;

    private static final String ACTOR_NAME = "employee";

    @Mock
    private TenantServiceAccessor tenantAccessor;

    @Mock
    private TransientDataService transientDataService;

    @Mock
    private OperationService operationService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private DataInstanceService dataInstanceService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ClassLoaderService classLoaderService;

    private ProcessAPIImpl processAPI;

    @Before
    public void setup() {
        processAPI = spy(new ProcessAPIImpl());
        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        when(tenantAccessor.getTenantId()).thenReturn(tenantId);
        when(tenantAccessor.getTransientDataService()).thenReturn(transientDataService);
        when(tenantAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);
        when(tenantAccessor.getClassLoaderService()).thenReturn(classLoaderService);
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantAccessor.getDataInstanceService()).thenReturn(dataInstanceService);
        when(tenantAccessor.getOperationService()).thenReturn(operationService);
    }

    @Test
    public void cancelAnUnknownProcessInstanceThrowsANotFoundException() throws Exception {
        final long processInstanceId = 45;
        final long userId = 9;
        final LockService lockService = mock(LockService.class);
        final TransactionalProcessInstanceInterruptor interruptor = mock(TransactionalProcessInstanceInterruptor.class);

        when(tenantAccessor.getLockService()).thenReturn(lockService);
        doReturn(userId).when(processAPI).getUserId();
        doReturn(interruptor).when(processAPI).buildProcessInstanceInterruptor(tenantAccessor);
        doThrow(new SProcessInstanceNotFoundException(processInstanceId)).when(interruptor).interruptProcessInstance(processInstanceId,
                SStateCategory.CANCELLING, userId);

        try {
            processAPI.cancelProcessInstance(processInstanceId);
            fail("The process instance does not exists");
        } catch (final ProcessInstanceNotFoundException pinfe) {
            verify(lockService).lock(processInstanceId, SFlowElementsContainerType.PROCESS.name(), tenantId);
            verify(lockService).unlock(any(BonitaLock.class), eq(tenantId));
        }
    }

    @Test
    public void should_updateProcessDataInstance_call_updateProcessDataInstances() throws Exception {
        final long processInstanceId = 42l;
        doNothing().when(processAPI).updateProcessDataInstances(eq(processInstanceId), any(Map.class));

        processAPI.updateProcessDataInstance("foo", processInstanceId, "go");

        verify(processAPI).updateProcessDataInstances(eq(processInstanceId), eq(Collections.<String, Serializable>singletonMap("foo", "go")));
    }

    @Test
    public void should_updateProcessDataInstances_call_DataInstanceService() throws Exception {
        final long processInstanceId = 42l;

        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final DataInstanceService dataInstanceService = mock(DataInstanceService.class);

        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());

        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        doReturn(dataInstanceService).when(tenantAccessor).getDataInstanceService();

        final SDataInstance sDataFoo = mock(SDataInstance.class);
        doReturn("foo").when(sDataFoo).getName();
        final SDataInstance sDataBar = mock(SDataInstance.class);
        doReturn("bar").when(sDataBar).getName();
        doReturn(asList(sDataFoo, sDataBar)).when(dataInstanceService).getDataInstances(eq(asList("foo", "bar")), anyLong(), anyString());

        // Then update the data instances
        final Map<String, Serializable> dataNameValues = new HashMap<String, Serializable>();
        dataNameValues.put("foo", "go");
        dataNameValues.put("bar", "go");
        processAPI.updateProcessDataInstances(processInstanceId, dataNameValues);

        // Check that we called DataInstanceService for each pair data/value
        verify(dataInstanceService, times(2)).updateDataInstance(any(SDataInstance.class), any(EntityUpdateDescriptor.class));
        verify(dataInstanceService).updateDataInstance(eq(sDataFoo), any(EntityUpdateDescriptor.class));
        verify(dataInstanceService).updateDataInstance(eq(sDataBar), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_updateProcessDataInstances_call_DataInstance_on_non_existing_data_throw_UpdateException() throws Exception {
        final long processInstanceId = 42l;

        final TenantServiceAccessor tenantAccessor = mock(TenantServiceAccessor.class);
        final DataInstanceService dataInstanceService = mock(DataInstanceService.class);

        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());

        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        doReturn(dataInstanceService).when(tenantAccessor).getDataInstanceService();

        doThrow(new SDataInstanceReadException("Mocked")).when(dataInstanceService).getDataInstances(eq(asList("foo", "bar")), anyLong(), anyString());

        // Then update the data instances
        final Map<String, Serializable> dataNameValues = new HashMap<String, Serializable>();
        dataNameValues.put("foo", "go");
        dataNameValues.put("bar", "go");
        try {
            processAPI.updateProcessDataInstances(processInstanceId, dataNameValues);
            fail("An exception should have been thrown.");
        } catch (final UpdateException e) {
            // Ok
        }

        // Check that we called DataInstanceService for each pair data/value
        verify(dataInstanceService, never()).updateDataInstance(any(SDataInstance.class), any(EntityUpdateDescriptor.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void replayingAFailedJobNoParamShouldExecuteAgainSchedulerServiceWithNoParameters() throws Exception {
        final long jobDescriptorId = 25L;
        final SchedulerService schedulerService = mock(SchedulerService.class);
        when(tenantAccessor.getSchedulerService()).thenReturn(schedulerService);
        doNothing().when(schedulerService).executeAgain(anyLong(), anyList());

        processAPI.replayFailedJob(jobDescriptorId, null);
        processAPI.replayFailedJob(jobDescriptorId, Collections.EMPTY_MAP);

        verify(schedulerService, times(2)).executeAgain(jobDescriptorId);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void replayingAFailedJobShouldExecuteAgainSchedulerServiceWithSomeParameters() throws Exception {
        final Map<String, Serializable> parameters = Collections.singletonMap("anyparam", (Serializable) Boolean.FALSE);
        final long jobDescriptorId = 544L;
        final SchedulerService schedulerService = mock(SchedulerService.class);
        when(tenantAccessor.getSchedulerService()).thenReturn(schedulerService);
        doNothing().when(schedulerService).executeAgain(anyLong(), anyList());

        doReturn(new ArrayList()).when(processAPI).getJobParameters(parameters);

        processAPI.replayFailedJob(jobDescriptorId, parameters);

        verify(schedulerService).executeAgain(eq(jobDescriptorId), anyList());
    }

    @Test
    public void replayingAFailedJobWithNoParamShouldCallWithNullParams() throws Exception {
        final long jobDescriptorId = 544L;

        // This spy is specific to this test method:
        final ProcessAPIImpl myProcessAPI = spy(new ProcessAPIImpl());
        doNothing().when(myProcessAPI).replayFailedJob(jobDescriptorId, null);

        myProcessAPI.replayFailedJob(jobDescriptorId);

        verify(myProcessAPI).replayFailedJob(jobDescriptorId, null);
    }

    @Test
    public void getJobParametersShouldConvertMapIntoList() {
        // given:
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        final String key1 = "mon param 1";
        final String key2 = "my second param";
        final SJobParameter expectedValue1 = mockSJobParameter(key1);
        parameters.put(expectedValue1.getKey(), expectedValue1.getValue());

        final SJobParameter expectedValue2 = mockSJobParameter(key2);
        parameters.put(expectedValue2.getKey(), expectedValue2.getValue());

        doReturn(expectedValue1).when(processAPI).buildSJobParameter(eq(key1), any(Serializable.class));
        doReturn(expectedValue2).when(processAPI).buildSJobParameter(eq(key2), any(Serializable.class));

        // when:
        final List<SJobParameter> jobParameters = processAPI.getJobParameters(parameters);

        // then:
        assertThat(jobParameters).containsOnly(expectedValue1, expectedValue2);
    }

    private SJobParameter mockSJobParameter(final String key) {
        final SJobParameter jobParam = mock(SJobParameter.class);
        when(jobParam.getKey()).thenReturn(key);
        when(jobParam.getValue()).thenReturn(Integer.MAX_VALUE);
        return jobParam;
    }

    @Test
    public void testGetActivityTransientDataInstances() throws Exception {
        final String dataValue = "TestOfCourse";
        final long activityInstanceId = 13244;
        final String dataName = "TransientName";
        doNothing().when(processAPI).updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        final SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        when(activityInstanceService.getFlowNodeInstance(activityInstanceId)).thenReturn(flowNodeInstance);

        final int nbResults = 100;
        final int startIndex = 0;
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        final List<SDataInstance> sDataInstances = Lists.newArrayList(sDataInstance);
        when(transientDataService.getDataInstances(activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name(), startIndex, nbResults))
        .thenReturn(sDataInstances);
        final IntegerDataInstanceImpl dataInstance = mock(IntegerDataInstanceImpl.class);
        doReturn(Lists.newArrayList(dataInstance)).when(processAPI).convertModelToDataInstances(sDataInstances);

        final List<DataInstance> dis = processAPI.getActivityTransientDataInstances(activityInstanceId, startIndex, nbResults);

        assertThat(dis).contains(dataInstance);

        verify(processAPI, times(1)).convertModelToDataInstances(sDataInstances);
        verify(transientDataService, times(1)).getDataInstances(activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name(), startIndex, nbResults);
        verify(tenantAccessor, times(1)).getTransientDataService();
        verify(tenantAccessor, times(1)).getClassLoaderService();
        verify(tenantAccessor, times(1)).getActivityInstanceService();
        verify(activityInstanceService, times(1)).getFlowNodeInstance(activityInstanceId);
        verify(flowNodeInstance, times(1)).getLogicalGroup(anyInt());
        verify(classLoaderService, times(1)).getLocalClassLoader(eq(ScopeType.PROCESS.name()), anyInt());
    }

    @Test
    public void testGetActivityTransientDataInstance() throws Exception {
        final String dataValue = "TestOfCourse";
        final int activityInstanceId = 13244;
        final String dataName = "TransientName";
        doNothing().when(processAPI).updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        final SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        when(activityInstanceService.getFlowNodeInstance(activityInstanceId)).thenReturn(flowNodeInstance);

        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(transientDataService.getDataInstance(dataName, activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name())).thenReturn(sDataInstance);
        final IntegerDataInstanceImpl dataInstance = mock(IntegerDataInstanceImpl.class);
        doReturn(dataInstance).when(processAPI).convertModeltoDataInstance(sDataInstance);

        final DataInstance di = processAPI.getActivityTransientDataInstance(dataName, activityInstanceId);

        assertThat(di).isEqualTo(dataInstance);

        verify(processAPI, times(1)).convertModeltoDataInstance(sDataInstance);
        verify(transientDataService, times(1)).getDataInstance(dataName, activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name());
        verify(tenantAccessor, times(1)).getTransientDataService();
        verify(tenantAccessor, times(1)).getClassLoaderService();
        verify(tenantAccessor, times(1)).getActivityInstanceService();
        verify(activityInstanceService, times(1)).getFlowNodeInstance(activityInstanceId);
        verify(flowNodeInstance, times(1)).getLogicalGroup(anyInt());
        verify(classLoaderService, times(1)).getLocalClassLoader(eq(ScopeType.PROCESS.name()), anyInt());
    }

    @Test
    public void testUpdateActivityTransientDataInstance_should_call_update() throws Exception {
        final String dataValue = "TestOfCourse";
        final int activityInstanceId = 13244;
        final String dataName = "TransientName";
        doNothing().when(processAPI).updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        final SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        when(activityInstanceService.getFlowNodeInstance(activityInstanceId)).thenReturn(flowNodeInstance);

        processAPI.updateActivityTransientDataInstance(dataName, activityInstanceId, dataValue);

        verify(processAPI).updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        verify(tenantAccessor, times(1)).getTransientDataService();
        verify(tenantAccessor, times(1)).getClassLoaderService();
        verify(tenantAccessor, times(1)).getActivityInstanceService();
        verify(activityInstanceService, times(1)).getFlowNodeInstance(activityInstanceId);
        verify(flowNodeInstance, times(1)).getLogicalGroup(anyInt());
        verify(classLoaderService, times(1)).getLocalClassLoader(eq(ScopeType.PROCESS.name()), anyInt());
    }

    @Test(expected = UpdateException.class)
    public void testUpdateActivityTransientDataInstance_should_throw_Exception() throws Exception {
        final String dataValue = "TestOfCourse";
        final int activityInstanceId = 13244;
        final String dataName = "TransientName";
        doThrow(new SDataInstanceException("")).when(processAPI).updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        final SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        when(activityInstanceService.getFlowNodeInstance(activityInstanceId)).thenReturn(flowNodeInstance);

        processAPI.updateActivityTransientDataInstance(dataName, activityInstanceId, dataValue);
    }

    @Test
    public void testUpdateTransientData() throws Exception {
        final String dataValue = "TestOfCourse";
        final int activityInstanceId = 13244;
        final String dataName = "TransientName";
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(transientDataService.getDataInstance(dataName, activityInstanceId,
                DataInstanceContainer.ACTIVITY_INSTANCE.toString())).thenReturn(sDataInstance);
        processAPI.updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        verify(transientDataService).updateDataInstance(eq(sDataInstance), any(EntityUpdateDescriptor.class));
        verify(transientDataService, times(1)).getDataInstance(dataName, activityInstanceId,
                DataInstanceContainer.ACTIVITY_INSTANCE.toString());
    }

    public void getUserIdsForActor_returns_result_of_actor_mapping_service() throws Exception {
        // given
        final SActor actor = mock(SActor.class);
        when(actor.getId()).thenReturn(ACTOR_ID);

        final ActorMappingService actorMappingService = mock(ActorMappingService.class);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(actorMappingService.getPossibleUserIdsOfActorId(ACTOR_ID, 0, 10)).thenReturn(Arrays.asList(1L, 10L));
        when(actorMappingService.getActor(ACTOR_NAME, PROCESS_DEFINITION_ID)).thenReturn(actor);

        // when
        final List<Long> userIdsForActor = processAPI.getUserIdsForActor(PROCESS_DEFINITION_ID, ACTOR_NAME, 0, 10);

        // then
        assertThat(userIdsForActor).containsExactly(1L, 10L);
    }

    @Test
    public void getUserIdsForActor_throws_RetrieveException_when_actorMappingService_throws_SBonitaException() throws Exception {
        // given
        final SActor actor = mock(SActor.class);
        when(actor.getId()).thenReturn(ACTOR_ID);

        final ActorMappingService actorMappingService = mock(ActorMappingService.class);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(actorMappingService.getActor(ACTOR_NAME, PROCESS_DEFINITION_ID)).thenThrow(new SActorNotFoundException(""));

        // when
        try {
            processAPI.getUserIdsForActor(PROCESS_DEFINITION_ID, ACTOR_NAME, 0, 10);
            fail("Exception expected");
        } catch (final RetrieveException e) {
            // then ok
        }


    }




    @Test
    public void updateActivityInstanceVariables_should_load_processDef_classes() throws Exception {
        final String dataInstanceName = "acase";

        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName)
                .setType(LeftOperand.TYPE_DATA).done();
        final String customDataTypeName = "com.bonitasoft.support.Case";
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("updateDataCaseTest",
                "new com.bonitasoft.support.Case(\"title\", \"description\")",
                customDataTypeName);
        final Operation operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        final ClassLoader contextClassLoader = mock(ClassLoader.class);
        when(classLoaderService.getLocalClassLoader(anyString(), anyLong())).thenReturn(contextClassLoader);
        final SProcessDefinition processDef = mock(SProcessDefinition.class);
        when(processDefinitionService.getProcessDefinition(anyLong())).thenReturn(processDef);
        final SActivityInstance activityInstance = mock(SActivityInstance.class);
        when(activityInstanceService.getActivityInstance(anyLong())).thenReturn(activityInstance);
        final SFlowElementContainerDefinition flowElementContainerDefinition = mock(SFlowElementContainerDefinition.class);
        when(processDef.getProcessContainer()).thenReturn(flowElementContainerDefinition);
        when(flowElementContainerDefinition.getFlowNode(anyLong())).thenReturn(mock(SActivityDefinition.class));

        final SDataInstance dataInstance = mock(SDataInstance.class);
        when(dataInstanceService.getDataInstances(any(List.class), anyLong(),
                eq(DataInstanceContainer.ACTIVITY_INSTANCE.toString()))).thenReturn(Arrays.asList(dataInstance));

        doReturn(mock(SOperation.class)).when(processAPI).convertOperation(operation);

        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(operation);
        processAPI.updateActivityInstanceVariables(operations, 2, null);

        verify(classLoaderService).getLocalClassLoader(anyString(), anyLong());
    }

    @Test
    public void should_removeDocument_call_the_service() throws Exception {
        //given
        DocumentService documentService = mock(DocumentService.class);
        when(tenantAccessor.getDocumentService()).thenReturn(documentService);


        SDocumentMappingImpl sProcessDocument = new SDocumentMappingImpl();
        sProcessDocument.setId(123l);

        doReturn(sProcessDocument).when(documentService).getDocument(123l);

        //when
        Document removeDocument = processAPI.removeDocument(123l);

        //then
        assertThat(ModelConvertor.toDocument(sProcessDocument, documentService)).isEqualTo(removeDocument);
        verify(documentService, times(1)).removeDocument(sProcessDocument);

    }


    @Test
    public void should_removeDocument_throw_DocumentNotFoundException() throws Exception {
        //given
        DocumentService documentService = mock(DocumentService.class);
        when(tenantAccessor.getDocumentService()).thenReturn(documentService);
        doThrow(SDocumentNotFoundException.class).when(documentService).getDocument(123l);

        //when
        try {
            processAPI.removeDocument(123l);
            fail("should not succeed if document does not exists");
        } catch (DocumentNotFoundException e) {
            //ok
        }
        //then
        verify(documentService, times(0)).removeDocument(any(SDocumentMapping.class));
    }


    @Test
    public void should_removeDocument_throw_DeletionException() throws Exception {
        //given
        DocumentService documentService = mock(DocumentService.class);
        when(tenantAccessor.getDocumentService()).thenReturn(documentService);
        SDocumentMappingImpl sProcessDocument = new SDocumentMappingImpl();
        sProcessDocument.setId(123l);
        doReturn(sProcessDocument).when(documentService).getDocument(123l);
        doThrow(SProcessDocumentDeletionException.class).when(documentService).removeDocument(sProcessDocument);

        //when
        try {
            processAPI.removeDocument(123l);
            fail("should not succeed if document does not exists");
        } catch (DeletionException e) {
            //ok
        }
        //then: exception
    }


}
