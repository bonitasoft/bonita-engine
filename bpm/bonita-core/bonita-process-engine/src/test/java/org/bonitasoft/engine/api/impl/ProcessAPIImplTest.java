/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.impl.IntegerDataInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
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
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    private ProcessInstanceService processInstanceService;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private ActorMappingService actorMappingService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private SearchEntitiesDescriptor searchEntitiesDescriptor;

    @Mock
    private EventInstanceService eventInstanceService;

    @Spy
    @InjectMocks
    private ProcessAPIImpl processAPI;

    @Before
    public void setup() {
        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        when(tenantAccessor.getTenantId()).thenReturn(tenantId);
        when(tenantAccessor.getTransientDataService()).thenReturn(transientDataService);
        when(tenantAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);
        when(tenantAccessor.getClassLoaderService()).thenReturn(classLoaderService);
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantAccessor.getProcessInstanceService()).thenReturn(processInstanceService);
        when(tenantAccessor.getDataInstanceService()).thenReturn(dataInstanceService);
        when(tenantAccessor.getOperationService()).thenReturn(operationService);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(tenantAccessor.getSchedulerService()).thenReturn(schedulerService);
        when(tenantAccessor.getSearchEntitiesDescriptor()).thenReturn(searchEntitiesDescriptor);
        when(tenantAccessor.getEventInstanceService()).thenReturn(eventInstanceService);
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
    public void generateRelativeResourcePathShouldHandleBackslashOS() {
        // given:
        String pathname = "C:\\hello\\hi\\folder";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = processAPI.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

    @Test
    public void generateRelativeResourcePathShouldNotContainFirstSlash() {
        // given:
        String pathname = "/home/target/some_folder/";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = processAPI.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

    @Test
    public void generateRelativeResourcePathShouldWorkWithRelativeInitialPath() {
        // given:
        String pathname = "target/nuns";
        final String resourceRelativePath = "resource/toto.lst";

        // when:
        final String generatedRelativeResourcePath = processAPI.generateRelativeResourcePath(new File(pathname), new File(pathname + File.separator
                + resourceRelativePath));

        // then:
        assertThat(generatedRelativeResourcePath).isEqualTo(resourceRelativePath);
    }

    @Test
    public void should_updateProcessDataInstance_call_updateProcessDataInstances() throws Exception {
        final long processInstanceId = 42l;
        doNothing().when(processAPI).updateProcessDataInstances(eq(processInstanceId), any(Map.class));

        processAPI.updateProcessDataInstance("foo", processInstanceId, "go");

        verify(processAPI).updateProcessDataInstances(eq(processInstanceId), eq(Collections.<String, Serializable> singletonMap("foo", "go")));
    }

    @Test
    public void should_updateProcessDataInstances_call_DataInstanceService() throws Exception {
        final long processInstanceId = 42l;

        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());

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
        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());
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
        doNothing().when(schedulerService).executeAgain(anyLong(), anyList());

        processAPI.replayFailedJob(jobDescriptorId, null);
        processAPI.replayFailedJob(jobDescriptorId, Collections.EMPTY_MAP);

        verify(schedulerService, times(2)).executeAgain(jobDescriptorId);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void replayingAFailedJobShouldExecuteAgainSchedulerServiceWithSomeParameters() throws Exception {
        final Map<String, Serializable> parameters = Collections.singletonMap("anyparam", (Serializable) Boolean.FALSE);
        final long jobDescriptorId = 544L;
        doNothing().when(schedulerService).executeAgain(anyLong(), anyList());
        doReturn(new ArrayList()).when(processAPI).getJobParameters(parameters);

        processAPI.replayFailedJob(jobDescriptorId, parameters);

        verify(schedulerService).executeAgain(eq(jobDescriptorId), anyList());
    }

    @Test
    public void replayingAFailedJobWithNoParamShouldCallWithNullParams() throws Exception {
        final long jobDescriptorId = 544L;

        doNothing().when(processAPI).replayFailedJob(jobDescriptorId, null);

        processAPI.replayFailedJob(jobDescriptorId);

        verify(processAPI).replayFailedJob(jobDescriptorId, null);
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
    public void getActivityTransientDataInstances() throws Exception {
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
    public void getActivityTransientDataInstance() throws Exception {
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
    public void updateActivityTransientDataInstance_should_call_update() throws Exception {
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
    public void updateActivityTransientDataInstance_should_throw_Exception() throws Exception {
        final String dataValue = "TestOfCourse";
        final int activityInstanceId = 13244;
        final String dataName = "TransientName";
        doThrow(new SDataInstanceException("")).when(processAPI).updateTransientData(dataName, activityInstanceId, dataValue, transientDataService);
        final SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        when(activityInstanceService.getFlowNodeInstance(activityInstanceId)).thenReturn(flowNodeInstance);

        processAPI.updateActivityTransientDataInstance(dataName, activityInstanceId, dataValue);
    }

    @Test
    public void updateTransientData() throws Exception {
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

    @Test(expected = IllegalArgumentException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_no_argument() throws Exception {
        processAPI.deleteArchivedProcessInstances();
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_null_argument() throws Exception {
        processAPI.deleteArchivedProcessInstances(null);
    }

    @Test
    public void deleteArchivedProcessInstances_by_ids_should_return_0_when_no_archived_process_instance_for_ids() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<Long> archivedProcessInstanceIds = Arrays.asList(archivedProcessInstanceId);
        doReturn(new ArrayList<SAProcessInstance>()).when(processInstanceService).getArchivedProcessInstances(archivedProcessInstanceIds);

        // When
        final long deleteArchivedProcessInstances = processAPI.deleteArchivedProcessInstances(archivedProcessInstanceId);

        // Then
        assertEquals("Must to return 0, when there are no archived process instance to delete.", 0, deleteArchivedProcessInstances);
        verify(processInstanceService).getArchivedProcessInstances(archivedProcessInstanceIds);
    }

    @Test
    public void deleteArchivedProcessInstances_by_ids_should_return_number_of_deleted_archived_process_instance_when_exist() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<Long> archivedProcessInstanceIds = Arrays.asList(archivedProcessInstanceId);
        final List<SAProcessInstance> archivedProcessInstancesToDelete = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(archivedProcessInstancesToDelete).when(processInstanceService).getArchivedProcessInstances(archivedProcessInstanceIds);
        doReturn(1L).when(processInstanceService).deleteParentArchivedProcessInstancesAndElements(archivedProcessInstancesToDelete);

        // When
        final long deleteArchivedProcessInstances = processAPI.deleteArchivedProcessInstances(archivedProcessInstanceId);

        // Then
        assertEquals("Must to return 1 deleted archived process instance.", 1L, deleteArchivedProcessInstances);
        verify(processInstanceService).getArchivedProcessInstances(archivedProcessInstanceIds);
        verify(processInstanceService).deleteParentArchivedProcessInstancesAndElements(archivedProcessInstancesToDelete);
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_getArchivedProcessInstances_throws_exception() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doThrow(new SProcessInstanceReadException(new Exception())).when(processInstanceService).getArchivedProcessInstances(
                Arrays.asList(archivedProcessInstanceId));

        // When
        processAPI.deleteArchivedProcessInstances(archivedProcessInstanceId);
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_deleteParentArchivedProcessInstancesAndElements_throws_exception()
            throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<SAProcessInstance> archivedProcessInstancesToDelete = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(archivedProcessInstancesToDelete).when(processInstanceService).getArchivedProcessInstances(Arrays.asList(archivedProcessInstanceId));
        doThrow(new SProcessInstanceModificationException(new Exception())).when(processInstanceService).deleteParentArchivedProcessInstancesAndElements(
                archivedProcessInstancesToDelete);

        // When
        processAPI.deleteArchivedProcessInstances(archivedProcessInstanceId);
    }

    @Test(expected = ProcessInstanceHierarchicalDeletionException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_parent_still_active() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<SAProcessInstance> archivedProcessInstancesToDelete = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(archivedProcessInstancesToDelete).when(processInstanceService).getArchivedProcessInstances(Arrays.asList(archivedProcessInstanceId));
        doThrow(new SProcessInstanceHierarchicalDeletionException("Parent still active", archivedProcessInstanceId)).when(processInstanceService)
                .deleteParentArchivedProcessInstancesAndElements(archivedProcessInstancesToDelete);

        // When
        processAPI.deleteArchivedProcessInstances(archivedProcessInstanceId);
    }

    @Test
    public void deleteArchivedProcessInstance_by_id_should_do_nothing_when_no_archived_process_instance() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doReturn(null).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);

        // When
        processAPI.deleteArchivedProcessInstance(archivedProcessInstanceId);

        // Then
        verify(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        verify(processInstanceService, never()).deleteParentArchivedProcessInstanceAndElements(any(SAProcessInstance.class));
    }

    @Test
    public void deleteArchivedProcessInstance_by_id_should_delete_archived_process_instance_when_exist() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doReturn(mock(SAProcessInstance.class)).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);

        // When
        processAPI.deleteArchivedProcessInstance(archivedProcessInstanceId);

        // Then
        verify(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        verify(processInstanceService).deleteParentArchivedProcessInstanceAndElements(any(SAProcessInstance.class));
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstance_by_id_should_throw_exception_when_getArchivedProcessInstance_throws_exception() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doThrow(new SProcessInstanceReadException(new Exception())).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);

        // When
        processAPI.deleteArchivedProcessInstance(archivedProcessInstanceId);
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstance_by_id_should_throw_exception_when_deleteParentArchivedProcessInstanceAndElements_throws_exception()
            throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doReturn(mock(SAProcessInstance.class)).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        doThrow(new SProcessInstanceModificationException(new Exception())).when(processInstanceService).deleteParentArchivedProcessInstanceAndElements(
                any(SAProcessInstance.class));

        // When
        processAPI.deleteArchivedProcessInstance(archivedProcessInstanceId);
    }

    @Test(expected = ProcessInstanceHierarchicalDeletionException.class)
    public void deleteArchivedProcessInstance_by_id_should_throw_exception_when_parent_still_active() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doReturn(mock(SAProcessInstance.class)).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        doThrow(new SProcessInstanceHierarchicalDeletionException("Parent still active", archivedProcessInstanceId)).when(processInstanceService)
                .deleteParentArchivedProcessInstanceAndElements(any(SAProcessInstance.class));

        // When
        processAPI.deleteArchivedProcessInstance(archivedProcessInstanceId);
    }

    @Test(expected = SearchException.class)
    public void searchEventTriggerInstances_should_throw_exception_when_transaction_throws_exception() throws Exception {
        // Given
        final long processInstanceId = 42l;
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10).done();

        doThrow(new SBonitaSearchException(new Exception())).when(eventInstanceService).getNumberOfTimerEventTriggerInstances(eq(processInstanceId),
                any(QueryOptions.class));

        // When
        processAPI.searchTimerEventTriggerInstances(processInstanceId, searchOptions);
    }

    @Test(expected = UpdateException.class)
    public void updateTimerEventTriggerInstance_should_throw_exception_when_new_execution_date_is_null() throws Exception {
        processAPI.updateExecutionDateOfTimerEventTriggerInstance(6, null);
    }

    @Test(expected = TimerEventTriggerInstanceNotFoundException.class)
    public void updateTimerEventTriggerInstance_should_throw_exception_when_timer_event_trigger_not_exist() throws Exception {
        processAPI.updateExecutionDateOfTimerEventTriggerInstance(6, new Date());
    }

    @Test(expected = UpdateException.class)
    public void updateTimerEventTriggerInstance_should_throw_exception_when_cant_get_timer_event_trigger() throws Exception {
        // Given
        final int timerEventTriggerInstanceId = 6;
        doThrow(new SEventTriggerInstanceReadException(new Exception(""))).when(eventInstanceService).getEventTriggerInstance(STimerEventTriggerInstance.class,
                timerEventTriggerInstanceId);

        // When
        processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerEventTriggerInstanceId, new Date());
    }

    @Test(expected = UpdateException.class)
    public void updateTimerEventTriggerInstance_should_throw_exception_when_cant_update_timer_event_trigger() throws Exception {
        // Given
        final int timerEventTriggerInstanceId = 6;
        final STimerEventTriggerInstance sTimerEventTriggerInstance = mock(STimerEventTriggerInstance.class);
        doReturn(sTimerEventTriggerInstance).when(eventInstanceService).getEventTriggerInstance(STimerEventTriggerInstance.class, timerEventTriggerInstanceId);
        doThrow(new SEventTriggerInstanceModificationException(new Exception(""))).when(eventInstanceService).updateEventTriggerInstance(
                eq(sTimerEventTriggerInstance), any(EntityUpdateDescriptor.class));

        // When
        processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerEventTriggerInstanceId, new Date());
    }

    @Test(expected = UpdateException.class)
    public void updateTimerEventTriggerInstance_should_throw_exception_when_cant_reschedule_job() throws Exception {
        // Given
        final int timerEventTriggerInstanceId = 6;
        final Date date = new Date();
        final STimerEventTriggerInstance sTimerEventTriggerInstance = mock(STimerEventTriggerInstance.class);
        doReturn(sTimerEventTriggerInstance).when(eventInstanceService).getEventTriggerInstance(STimerEventTriggerInstance.class, timerEventTriggerInstanceId);

        doThrow(new SSchedulerException(new Exception(""))).when(schedulerService).rescheduleJob(anyString(), anyString(), eq(date));

        // When
        processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerEventTriggerInstanceId, new Date());
    }
}
