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
package org.bonitasoft.engine.api.impl;

import com.google.common.collect.Lists;
import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.DocumentAPI;
import org.bonitasoft.engine.api.impl.transaction.connector.GetConnectorImplementations;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.impl.IntegerDataInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.parser.JarDependencies;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.contract.data.SContractDataNotFoundException;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContextEntryImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;
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
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAUserTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.impl.SBlobDataInstanceImpl;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExceptionContext;
import org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchHumanTaskInstanceDescriptor;
import org.bonitasoft.engine.search.process.SearchFailedProcessInstancesSupervisedBy;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySetOf;
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

@RunWith(MockitoJUnitRunner.class)
public class ProcessAPIImplTest {

    private static final ConnectorCriterion CONNECTOR_CRITERION_DEFINITION_ID_ASC = ConnectorCriterion.DEFINITION_ID_ASC;

    private static final int MAX_RESULT = 10;
    private static final int START_INDEX = 0;
    private static final long TENANT_ID = 1;
    private static final long ACTOR_ID = 100;
    private static final long PROCESS_DEFINITION_ID = 110;
    private static final long PROCESS_INSTANCE_ID = 45;
    private static final long ARCHIVED_PROCESS_INSTANCE_ID = 45;
    private static final long FLOW_NODE_INSTANCE_ID = 1674;
    private static final long ARCHIVED_FLOW_NODE_INSTANCE_ID = 1674;
    private static final long FLOW_NODE_DEFINITION_ID = 1664;
    private static final String ACTOR_NAME = "employee";
    @Mock
    ProcessManagementAPIImplDelegate managementAPIImplDelegate;
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
    private ParentContainerResolver parentContainerResolver;
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
    @Mock
    private FlowNodeStateManager flowNodeStateManager;
    @Mock
    private DocumentAPI documentAPI;
    @Mock
    private ConnectorService connectorService;
    @Mock
    private GetConnectorImplementations getConnectorImplementation;
    @Mock
    private ContractDataService contractDataService;
    @Mock
    private ExpressionResolverService expressionResolverService;
    private SProcessDefinitionImpl processDefinition;
    private SUserTaskDefinition userTaskDefinition;
    @Captor
    private ArgumentCaptor<List<String>> argument;
    @Spy
    @InjectMocks
    private ProcessAPIImpl processAPI;

    @Before
    public void setup() throws Exception {
        doReturn(tenantAccessor).when(processAPI).getTenantAccessor();
        when(tenantAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(tenantAccessor.getTransientDataService()).thenReturn(transientDataService);
        when(tenantAccessor.getExpressionResolverService()).thenReturn(expressionResolverService);

        when(tenantAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);
        SUserTaskInstanceImpl sUserTaskInstance = new SUserTaskInstanceImpl("userTaskName", FLOW_NODE_DEFINITION_ID, PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID,
                ACTOR_ID, STaskPriority.ABOVE_NORMAL,
                PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID);
        when(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).thenReturn(
                sUserTaskInstance);
        SAUserTaskInstanceImpl value = new SAUserTaskInstanceImpl(sUserTaskInstance);
        value.setId(ARCHIVED_FLOW_NODE_INSTANCE_ID);
        when(activityInstanceService.getArchivedFlowNodeInstance(ARCHIVED_FLOW_NODE_INSTANCE_ID)).thenReturn(
                value);
        processDefinition = new SProcessDefinitionImpl("myProcess", "1.0");
        SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        processDefinition.setProcessContainer(processContainer);
        userTaskDefinition = new SUserTaskDefinitionImpl(FLOW_NODE_DEFINITION_ID, "userTask", "actor");
        processContainer.addActivity(userTaskDefinition);

        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);

        when(tenantAccessor.getClassLoaderService()).thenReturn(classLoaderService);
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);

        when(tenantAccessor.getProcessInstanceService()).thenReturn(processInstanceService);
        SProcessInstanceImpl sProcessInstance = new SProcessInstanceImpl("processName", PROCESS_DEFINITION_ID);
        when(processInstanceService.getProcessInstance(PROCESS_INSTANCE_ID)).thenReturn(sProcessInstance);
        SAProcessInstanceImpl value1 = new SAProcessInstanceImpl(sProcessInstance);
        value1.setId(ARCHIVED_PROCESS_INSTANCE_ID);
        when(processInstanceService.getArchivedProcessInstance(PROCESS_INSTANCE_ID)).thenReturn(value1);

        when(tenantAccessor.getDataInstanceService()).thenReturn(dataInstanceService);
        when(tenantAccessor.getOperationService()).thenReturn(operationService);
        when(tenantAccessor.getActorMappingService()).thenReturn(actorMappingService);
        when(tenantAccessor.getConnectorService()).thenReturn(connectorService);
        when(tenantAccessor.getSchedulerService()).thenReturn(schedulerService);
        when(tenantAccessor.getSearchEntitiesDescriptor()).thenReturn(searchEntitiesDescriptor);
        when(tenantAccessor.getEventInstanceService()).thenReturn(eventInstanceService);
        when(tenantAccessor.getFlowNodeStateManager()).thenReturn(flowNodeStateManager);
        when(tenantAccessor.getParentContainerResolver()).thenReturn(parentContainerResolver);
        when(tenantAccessor.getContractDataService()).thenReturn(contractDataService);
    }

    @Test
    public void getFlownodeStateCounters_should_build_proper_journal_and_archived_counters() throws Exception {
        final long processInstanceId = 9811L;
        final List<SFlowNodeInstanceStateCounter> flownodes = new ArrayList<>(4);
        flownodes.add(new SFlowNodeInstanceStateCounter("step1", "completed", 2L));
        flownodes.add(new SFlowNodeInstanceStateCounter("step2", "completed", 4L));
        flownodes.add(new SFlowNodeInstanceStateCounter("step1", "ready", 1L));
        flownodes.add(new SFlowNodeInstanceStateCounter("step3", "failed", 8L));
        when(activityInstanceService.getNumberOfFlownodesInAllStates(processInstanceId)).thenReturn(flownodes);

        final List<SFlowNodeInstanceStateCounter> archivedFlownodes = new ArrayList<>(1);
        archivedFlownodes.add(new SFlowNodeInstanceStateCounter("step2", "aborted", 3L));
        when(activityInstanceService.getNumberOfArchivedFlownodesInAllStates(processInstanceId)).thenReturn(archivedFlownodes);

        final Map<String, Map<String, Long>> flownodeStateCounters = processAPI.getFlownodeStateCounters(processInstanceId);
        assertThat(flownodeStateCounters.size()).isEqualTo(3);

        final Map<String, Long> step1 = flownodeStateCounters.get("step1");
        assertThat(step1.size()).isEqualTo(2);
        assertThat(step1.get("completed")).isEqualTo(2L);
        assertThat(step1.get("ready")).isEqualTo(1L);

        final Map<String, Long> step2 = flownodeStateCounters.get("step2");
        assertThat(step2.size()).isEqualTo(2);
        assertThat(step2.get("aborted")).isEqualTo(3L);
        assertThat(step2.get("completed")).isEqualTo(4L);

        final Map<String, Long> step3 = flownodeStateCounters.get("step3");
        assertThat(step3.size()).isEqualTo(1);
        assertThat(step3.get("failed")).isEqualTo(8L);
    }

    @Test
    public void cancelAnUnknownProcessInstanceThrowsANotFoundException() throws Exception {
        final long userId = 9;
        final LockService lockService = mock(LockService.class);
        final TransactionalProcessInstanceInterruptor interruptor = mock(TransactionalProcessInstanceInterruptor.class);

        when(tenantAccessor.getLockService()).thenReturn(lockService);
        doReturn(userId).when(processAPI).getUserId();
        doReturn(interruptor).when(processAPI).buildProcessInstanceInterruptor(tenantAccessor);
        doThrow(new SProcessInstanceNotFoundException(PROCESS_INSTANCE_ID)).when(interruptor).interruptProcessInstance(PROCESS_INSTANCE_ID,
                SStateCategory.CANCELLING, userId);

        try {
            processAPI.cancelProcessInstance(PROCESS_INSTANCE_ID);
            fail("The process instance does not exists");
        } catch (final ProcessInstanceNotFoundException pinfe) {
            verify(lockService).lock(PROCESS_INSTANCE_ID, SFlowElementsContainerType.PROCESS.name(), TENANT_ID);
            verify(lockService).unlock(any(BonitaLock.class), eq(TENANT_ID));
        }
    }

    @Test
    public void updateProcessDataInstance_should_call_updateProcessDataInstances() throws Exception {
        // Given
        doNothing().when(processAPI).updateProcessDataInstances(eq(PROCESS_INSTANCE_ID), anyMapOf(String.class, Serializable.class));

        // When
        processAPI.updateProcessDataInstance("foo", PROCESS_INSTANCE_ID, "go");

        // Then
        verify(processAPI).updateProcessDataInstances(eq(PROCESS_INSTANCE_ID), eq(Collections.<String, Serializable> singletonMap("foo", "go")));
    }

    @Test(expected = UpdateException.class)
    public void updateProcessDataInstance_should_throw_exception_when_updateProcessDataInstances_failed() throws Exception {
        // Given
        doThrow(new UpdateException()).when(processAPI).updateProcessDataInstances(eq(PROCESS_INSTANCE_ID), anyMapOf(String.class, Serializable.class));
        // When
        processAPI.updateProcessDataInstance("foo", PROCESS_INSTANCE_ID, "go");
    }

    @Test
    public void updateProcessDataInstances_should_update_data_instances_when_new_value_is_instance_of_data_type() throws Exception {
        // Given
        ClassLoader processClassLoader = mock(ClassLoader.class);
        when(processClassLoader.loadClass(SBlobDataInstanceImpl.class.getName())).thenReturn((Class) SBlobDataInstanceImpl.class);
        when(processClassLoader.loadClass(String.class.getName())).thenReturn((Class) String.class);
        doReturn(processClassLoader).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());

        final SBlobDataInstanceImpl sDataFoo = new SBlobDataInstanceImpl();
        sDataFoo.setClassName(String.class.getName());
        sDataFoo.setName("foo");

        final SBlobDataInstanceImpl sDataBar = new SBlobDataInstanceImpl();
        sDataBar.setClassName(String.class.getName());
        sDataBar.setName("bar");

        doReturn(asList(sDataFoo, sDataBar)).when(dataInstanceService).getDataInstances(argument.capture(), anyLong(), anyString(),
                any(ParentContainerResolver.class));

        // Then update the data instances
        final Map<String, Serializable> dataNameValues = new HashMap<String, Serializable>();
        dataNameValues.put("foo", "go");
        dataNameValues.put("bar", "go");

        // When
        processAPI.updateProcessDataInstances(PROCESS_INSTANCE_ID, dataNameValues);

        // Then
        // Check that we called DataInstanceService for each pair data/value
        verify(dataInstanceService, times(2)).updateDataInstance(any(SDataInstance.class), any(EntityUpdateDescriptor.class));
        assertThat(argument.getValue()).containsOnly("foo", "bar");
        verify(dataInstanceService).updateDataInstance(eq(sDataFoo), any(EntityUpdateDescriptor.class));
        verify(dataInstanceService).updateDataInstance(eq(sDataBar), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void updateProcessDataInstances_should_throw_exception_when_new_value_is_not_instance_of_data_type() throws Exception {
        // Given
        final String dataName = "dataName";
        ClassLoader processClassLoader = mock(ClassLoader.class);
        when(processClassLoader.loadClass(SBlobDataInstanceImpl.class.getName())).thenReturn((Class) SBlobDataInstanceImpl.class);
        when(processClassLoader.loadClass(List.class.getName())).thenReturn((Class) List.class);
        doReturn(processClassLoader).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());
        when(classLoaderService.getLocalClassLoader(anyString(), anyLong())).thenReturn(processClassLoader);

        final Map<String, Serializable> dataNameValues = singletonMap(dataName, (Serializable) "dataValue");

        final SBlobDataInstanceImpl dataInstance = new SBlobDataInstanceImpl();
        dataInstance.setClassName(List.class.getName());
        dataInstance.setName(dataName);
        doReturn(Collections.singletonList(dataInstance)).when(dataInstanceService).getDataInstances(Collections.singletonList(dataName),
                PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.toString(), parentContainerResolver);

        // When
        try {
            processAPI.updateProcessDataInstances(PROCESS_INSTANCE_ID, dataNameValues);
            fail("An exception should have been thrown.");
        } catch (final UpdateException e) {
            // Then
            assertThat(e.getMessage()).isEqualTo(
                    "DATA_NAME=" + dataName + " | DATA_CLASS_NAME=java.util.List | The type of new value [" + String.class.getName()
                            + "] is not compatible with the type of the data.");
            final Map<ExceptionContext, Serializable> exceptionContext = e.getContext();
            assertThat(List.class.getName()).isEqualTo(exceptionContext.get(ExceptionContext.DATA_CLASS_NAME));
            assertThat(dataName).isEqualTo(exceptionContext.get(ExceptionContext.DATA_NAME));
        }
    }

    @Test
    public void should_updateProcessDataInstances_call_DataInstance_on_non_existing_data_throw_UpdateException() throws Exception {
        final long processInstanceId = 42l;
        doReturn(null).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());
        doThrow(new SDataInstanceReadException("Mocked")).when(dataInstanceService).getDataInstances(argument.capture(), anyLong(), anyString(),
                any(ParentContainerResolver.class));

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
        assertThat(argument.getValue()).containsOnly("foo", "bar");
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
        final String dataName = "TransientName";
        doNothing().when(processAPI).updateTransientData(eq(dataName), eq(FLOW_NODE_INSTANCE_ID), eq(dataValue), eq(transientDataService),
                any(ClassLoader.class));

        final int nbResults = 100;
        final int startIndex = 0;
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        final List<SDataInstance> sDataInstances = Lists.newArrayList(sDataInstance);
        when(transientDataService.getDataInstances(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name(), startIndex, nbResults))
                .thenReturn(sDataInstances);
        final IntegerDataInstanceImpl dataInstance = mock(IntegerDataInstanceImpl.class);
        doReturn(Lists.newArrayList(dataInstance)).when(processAPI).convertModelToDataInstances(sDataInstances);

        final List<DataInstance> dis = processAPI.getActivityTransientDataInstances(FLOW_NODE_INSTANCE_ID, startIndex, nbResults);

        assertThat(dis).contains(dataInstance);

        verify(processAPI, times(1)).convertModelToDataInstances(sDataInstances);
        verify(transientDataService, times(1)).getDataInstances(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name(), startIndex, nbResults);
        verify(tenantAccessor, times(1)).getTransientDataService();
        verify(tenantAccessor, times(1)).getClassLoaderService();
        verify(tenantAccessor, times(1)).getActivityInstanceService();
        verify(activityInstanceService, times(1)).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);
        verify(classLoaderService, times(1)).getLocalClassLoader(eq(ScopeType.PROCESS.name()), anyInt());
    }

    @Test
    public void getActivityTransientDataInstance() throws Exception {
        final String dataValue = "TestOfCourse";
        final String dataName = "TransientName";
        doNothing().when(processAPI).updateTransientData(eq(dataName), eq(FLOW_NODE_INSTANCE_ID), eq(dataValue), eq(transientDataService),
                any(ClassLoader.class));

        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(transientDataService.getDataInstance(dataName, FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())).thenReturn(sDataInstance);
        final IntegerDataInstanceImpl dataInstance = mock(IntegerDataInstanceImpl.class);
        doReturn(dataInstance).when(processAPI).convertModeltoDataInstance(sDataInstance);

        final DataInstance di = processAPI.getActivityTransientDataInstance(dataName, FLOW_NODE_INSTANCE_ID);

        assertThat(di).isEqualTo(dataInstance);

        verify(processAPI, times(1)).convertModeltoDataInstance(sDataInstance);
        verify(transientDataService, times(1)).getDataInstance(dataName, FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name());
        verify(tenantAccessor, times(1)).getTransientDataService();
        verify(tenantAccessor, times(1)).getClassLoaderService();
        verify(tenantAccessor, times(1)).getActivityInstanceService();
        verify(activityInstanceService, times(1)).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);
        verify(classLoaderService, times(1)).getLocalClassLoader(eq(ScopeType.PROCESS.name()), anyInt());
    }

    @Test
    public void updateActivityDataInstance_should_throw_exception_when_new_value_is_not_instance_of_data_type() throws Exception {
        // Given
        final String dataName = "dataName";
        ClassLoader processClassLoader = mock(ClassLoader.class);
        when(processClassLoader.loadClass(SBlobDataInstanceImpl.class.getName())).thenReturn((Class) SBlobDataInstanceImpl.class);
        when(processClassLoader.loadClass(List.class.getName())).thenReturn((Class) List.class);
        doReturn(processClassLoader).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());
        when(classLoaderService.getLocalClassLoader(anyString(), anyLong())).thenReturn(processClassLoader);

        final SBlobDataInstanceImpl dataInstance = new SBlobDataInstanceImpl();
        dataInstance.setClassName(List.class.getName());
        dataInstance.setName(dataName);
        doReturn(dataInstance).when(dataInstanceService).getDataInstance(dataName, FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.toString(),
                parentContainerResolver);

        // When
        try {

            processAPI.updateActivityDataInstance(dataName, FLOW_NODE_INSTANCE_ID, "dataValue");
            fail("An exception should have been thrown.");
        } catch (final UpdateException e) {
            // Then
            assertThat("DATA_NAME=" + dataName + " | DATA_CLASS_NAME=java.util.List | The type of new value [" + String.class.getName()
                    + "] is not compatible with the type of the data.").isEqualTo(e.getMessage());
            final Map<ExceptionContext, Serializable> exceptionContext = e.getContext();
            assertThat(List.class.getName()).isEqualTo(exceptionContext.get(ExceptionContext.DATA_CLASS_NAME));
            assertThat(dataName).isEqualTo(exceptionContext.get(ExceptionContext.DATA_NAME));
        }
    }

    @Test
    public void updateActivityTransientDataInstance_should_throw_exception_when_new_value_is_not_instance_of_data_type() throws Exception {
        // Given
        final String dataName = "dataName";
        ClassLoader processClassLoader = mock(ClassLoader.class);
        when(processClassLoader.loadClass(SBlobDataInstanceImpl.class.getName())).thenReturn((Class) SBlobDataInstanceImpl.class);
        when(processClassLoader.loadClass(List.class.getName())).thenReturn((Class) List.class);
        doReturn(processClassLoader).when(processAPI).getProcessInstanceClassloader(any(TenantServiceAccessor.class), anyLong());
        when(classLoaderService.getLocalClassLoader(anyString(), anyLong())).thenReturn(processClassLoader);

        final SBlobDataInstanceImpl dataInstance = new SBlobDataInstanceImpl();
        dataInstance.setClassName(List.class.getName());
        dataInstance.setName(dataName);
        doReturn(dataInstance).when(transientDataService).getDataInstance(dataName, FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.toString());

        // When
        try {

            processAPI.updateActivityTransientDataInstance(dataName, FLOW_NODE_INSTANCE_ID, "dataValue");
            fail("An exception should have been thrown.");
        } catch (final UpdateException e) {
            // Then
            assertThat("DATA_NAME=" + dataName + " | DATA_CLASS_NAME=java.util.List | The type of new value [" + String.class.getName()
                    + "] is not compatible with the type of the data.").isEqualTo(e.getMessage());
            final Map<ExceptionContext, Serializable> exceptionContext = e.getContext();
            assertThat(List.class.getName()).isEqualTo(exceptionContext.get(ExceptionContext.DATA_CLASS_NAME));
            assertThat(dataName).isEqualTo(exceptionContext.get(ExceptionContext.DATA_NAME));
        }
    }

    @Test
    public void updateActivityTransientDataInstance_should_call_updateTransientData_when_new_value_is_instance_of_data_type() throws Exception {
        // Given
        final String dataValue = "TestOfCourse";
        final String dataName = "TransientName";
        ClassLoader contextClassLoader = mock(ClassLoader.class);
        when(classLoaderService.getLocalClassLoader(anyString(), anyLong())).thenReturn(contextClassLoader);
        doNothing().when(processAPI).updateTransientData(dataName, FLOW_NODE_INSTANCE_ID, dataValue, transientDataService, contextClassLoader);

        // When
        processAPI.updateActivityTransientDataInstance(dataName, FLOW_NODE_INSTANCE_ID, dataValue);

        // Then

        verify(processAPI).updateTransientData(dataName, FLOW_NODE_INSTANCE_ID, dataValue, transientDataService, contextClassLoader);
        verify(tenantAccessor, times(1)).getTransientDataService();
        verify(tenantAccessor, times(1)).getClassLoaderService();
        verify(tenantAccessor, times(1)).getActivityInstanceService();
        verify(activityInstanceService, times(1)).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);
        verify(classLoaderService, times(1)).getLocalClassLoader(eq(ScopeType.PROCESS.name()), anyInt());
    }

    @Test(expected = UpdateException.class)
    public void updateActivityTransientDataInstance_should_throw_exception_when_new_value_is_instance_of_data_type_and_updateTransientData_failed()
            throws Exception {
        // Given
        final String dataValue = "TestOfCourse";
        final String dataName = "TransientName";
        doThrow(new SDataInstanceException("")).when(processAPI).updateTransientData(eq(dataName), eq(FLOW_NODE_INSTANCE_ID), eq(dataValue),
                eq(transientDataService), any(ClassLoader.class));

        // When
        processAPI.updateActivityTransientDataInstance(dataName, FLOW_NODE_INSTANCE_ID, dataValue);
    }

    @Test
    public void updateTransientData() throws Exception {
        // Given
        final String dataValue = "TestOfCourse";
        final String dataName = "TransientName";
        final SBlobDataInstanceImpl dataInstance = new SBlobDataInstanceImpl();
        dataInstance.setClassName(String.class.getName());
        dataInstance.setName(dataName);
        when(transientDataService.getDataInstance(dataName, FLOW_NODE_INSTANCE_ID,
                DataInstanceContainer.ACTIVITY_INSTANCE.toString())).thenReturn(dataInstance);

        // When
        processAPI.updateTransientData(dataName, FLOW_NODE_INSTANCE_ID, dataValue, transientDataService, this.getClass().getClassLoader());

        // Then
        verify(transientDataService).updateDataInstance(eq(dataInstance), any(EntityUpdateDescriptor.class));
        verify(transientDataService, times(1)).getDataInstance(dataName, FLOW_NODE_INSTANCE_ID,
                DataInstanceContainer.ACTIVITY_INSTANCE.toString());
    }

    @Test
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
        when(dataInstanceService.getDataInstances(anyListOf(String.class), anyLong(),
                eq(DataInstanceContainer.ACTIVITY_INSTANCE.toString()), any(ParentContainerResolver.class))).thenReturn(Arrays.asList(dataInstance));

        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(operation);
        doReturn(Arrays.asList(mock(SOperation.class))).when(processAPI).convertOperations(operations);

        processAPI.updateActivityInstanceVariables(operations, 2, null);

        verify(classLoaderService).getLocalClassLoader(anyString(), anyLong());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_list_is_empty() throws Exception {
        processAPI.deleteArchivedProcessInstancesInAllStates(Collections.<Long> emptyList());
    }

    @Test
    public void searchFailedProcessInstances_should_return_failed_process_instances() throws Exception {
        // Given
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 20).done();
        final long numberOfFailedProcessInstances = 2L;
        final List<ProcessInstance> failedProcessInstances = Arrays.asList((ProcessInstance) new ProcessInstanceImpl("name"));
        final long processDefinitionId = 9L;
        final List<SProcessInstance> sFailedProcessInstances = Arrays.asList((SProcessInstance) new SProcessInstanceImpl("name", processDefinitionId));
        doReturn(numberOfFailedProcessInstances).when(processInstanceService).getNumberOfFailedProcessInstances(any(QueryOptions.class));
        doReturn(sFailedProcessInstances).when(processInstanceService).searchFailedProcessInstances(any(QueryOptions.class));
        doReturn(mock(SProcessDefinition.class)).when(processDefinitionService).getProcessDefinition(processDefinitionId);

        // When
        final SearchResult<ProcessInstance> searchFailedProcessInstances = processAPI.searchFailedProcessInstances(searchOptions);

        // Then
        assertThat(numberOfFailedProcessInstances).isEqualTo(searchFailedProcessInstances.getCount());
        assertThat(failedProcessInstances).isEqualTo(searchFailedProcessInstances.getResult());
    }

    @Test(expected = RetrieveException.class)
    public void getConnectorsImplementations_should_throw__exception() throws Exception {
        //given
        final SConnectorException sConnectorException = new SConnectorException("message");
        doThrow(sConnectorException).when(connectorService).getConnectorImplementations(anyLong(), anyLong(),
                anyInt(), anyInt(), anyString(),
                any(OrderByType.class));

        //when then exception
        processAPI.getConnectorImplementations(PROCESS_DEFINITION_ID, START_INDEX, MAX_RESULT, CONNECTOR_CRITERION_DEFINITION_ID_ASC);

    }

    @Test(expected = RetrieveException.class)
    public void getNumberOfConnectorImplementations_should_throw__exception() throws Exception {
        //given
        final SConnectorException sConnectorException = new SConnectorException("message");
        doThrow(sConnectorException).when(connectorService).getNumberOfConnectorImplementations(anyLong(), anyLong());

        //when then exception
        processAPI.getNumberOfConnectorImplementations(PROCESS_DEFINITION_ID);

    }

    @Test(expected = SearchException.class)
    public void searchFailedProcessInstances_should_throw_exception_when_transaction_content_failed() throws Exception {
        // Given
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 20).done();
        doThrow(new SBonitaReadException(new Exception("plop"))).when(processInstanceService).getNumberOfFailedProcessInstances(any(QueryOptions.class));

        // When
        processAPI.searchFailedProcessInstances(searchOptions);
    }

    @Test
    public void getConnectorsImplementations_should_return_list() throws Exception {
        //given
        final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = createConnectorList();

        doReturn(sConnectorImplementationDescriptors).when(connectorService).getConnectorImplementations(anyLong(), anyLong(),
                anyInt(), anyInt(), anyString(),
                any(OrderByType.class));

        //when
        final List<ConnectorImplementationDescriptor> connectorImplementations = processAPI.getConnectorImplementations(PROCESS_DEFINITION_ID, START_INDEX,
                MAX_RESULT, CONNECTOR_CRITERION_DEFINITION_ID_ASC);

        //then
        assertThat(connectorImplementations).as("should return connectore implementation").hasSameSizeAs(sConnectorImplementationDescriptors);
    }

    @Test
    public void getNumberOfConnectorImplementations_should_return_count() throws Exception {
        //given
        final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = createConnectorList();

        doReturn((long) sConnectorImplementationDescriptors.size()).when(connectorService)
                .getNumberOfConnectorImplementations(PROCESS_DEFINITION_ID, TENANT_ID);

        //when
        final long numberOfConnectorImplementations = processAPI.getNumberOfConnectorImplementations(PROCESS_DEFINITION_ID);

        //then
        assertThat(numberOfConnectorImplementations).as("should return count").isEqualTo(sConnectorImplementationDescriptors.size());
    }

    private List<SConnectorImplementationDescriptor> createConnectorList() {
        final List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors = new ArrayList<SConnectorImplementationDescriptor>();
        final SConnectorImplementationDescriptor sConnectorImplementationDescriptor = new SConnectorImplementationDescriptor("className", "id", "version",
                "definitionId", "definitionVersion", new JarDependencies(Arrays.asList("dep1", "dep2")));
        sConnectorImplementationDescriptors.add(sConnectorImplementationDescriptor);
        sConnectorImplementationDescriptors.add(sConnectorImplementationDescriptor);
        sConnectorImplementationDescriptors.add(sConnectorImplementationDescriptor);
        return sConnectorImplementationDescriptors;
    }

    @Test
    public void evaluateExpressionsOnCompletedActivityInstance_should_call_getLastArchivedProcessInstance_using_parentProcessInstanceId() throws Exception {
        //given
        final long processInstanceId = 21L;
        final long activityInstanceId = 5L;
        final ArchivedActivityInstance activityInstance = mock(ArchivedActivityInstance.class);
        given(activityInstance.getProcessInstanceId()).willReturn(processInstanceId);
        given(activityInstance.getArchiveDate()).willReturn(new Date());
        doReturn(activityInstance).when(processAPI).getArchivedActivityInstance(activityInstanceId);

        final ArchivedProcessInstance procInst = mock(ArchivedProcessInstance.class);
        given(procInst.getProcessDefinitionId()).willReturn(1000L);
        doReturn(procInst).when(processAPI).getLastArchivedProcessInstance(anyLong());

        //when
        processAPI.evaluateExpressionsOnCompletedActivityInstance(activityInstanceId, new HashMap<Expression, Map<String, Serializable>>());

        //then
        verify(processAPI).getLastArchivedProcessInstance(processInstanceId);
        verify(activityInstance, never()).getParentContainerId();
        verify(activityInstance, never()).getParentActivityInstanceId();
        verify(activityInstance, never()).getRootContainerId();
    }

    @Test
    public void purgeClassLoader_should_call_delegate() throws Exception {
        processAPI.purgeClassLoader(45L);

        verify(managementAPIImplDelegate).purgeClassLoader(45L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_null_argument() throws Exception {
        processAPI.deleteArchivedProcessInstancesInAllStates(null);
    }

    @Test
    public void deleteArchivedProcessInstances_by_ids_should_return_0_when_no_archived_process_instance_for_ids() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<Long> archivedProcessInstanceIds = Arrays.asList(archivedProcessInstanceId);
        doReturn(new ArrayList<SAProcessInstance>()).when(processInstanceService).getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);

        // When
        final long deleteArchivedProcessInstances = processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);

        // Then
        assertThat(0L).isEqualTo(deleteArchivedProcessInstances).as("Must to return 0, when there are no archived process instance to delete.");
        verify(processInstanceService).getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);
    }

    @Test
    public void deleteArchivedProcessInstances_by_ids_should_return_number_of_deleted_archived_process_instance_when_exist() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<Long> archivedProcessInstanceIds = Arrays.asList(archivedProcessInstanceId);
        final List<SAProcessInstance> archivedProcessInstancesToDelete = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(archivedProcessInstancesToDelete).when(processInstanceService).getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);
        doReturn(1L).when(processInstanceService).deleteArchivedParentProcessInstancesAndElements(archivedProcessInstancesToDelete);

        // When
        final long deleteArchivedProcessInstances = processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);

        // Then
        assertThat(1L).as("Must to return 1 deleted archived process instance.").isEqualTo(deleteArchivedProcessInstances);
        verify(processInstanceService).getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);
        verify(processInstanceService).deleteArchivedParentProcessInstancesAndElements(archivedProcessInstancesToDelete);
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_getArchivedProcessInstances_throws_exception() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doThrow(new SProcessInstanceReadException(new Exception())).when(processInstanceService).getArchivedProcessInstancesInAllStates(
                Arrays.asList(archivedProcessInstanceId));

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_deleteParentArchivedProcessInstancesAndElements_throws_exception()
            throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<SAProcessInstance> archivedProcessInstancesToDelete = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(archivedProcessInstancesToDelete).when(processInstanceService)
                .getArchivedProcessInstancesInAllStates(Arrays.asList(archivedProcessInstanceId));
        doThrow(new SProcessInstanceModificationException(new Exception())).when(processInstanceService).deleteArchivedParentProcessInstancesAndElements(
                archivedProcessInstancesToDelete);

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);
    }

    @Test(expected = ProcessInstanceHierarchicalDeletionException.class)
    public void deleteArchivedProcessInstances_by_ids_should_throw_exception_when_parent_still_active() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        final List<SAProcessInstance> archivedProcessInstancesToDelete = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(archivedProcessInstancesToDelete).when(processInstanceService)
                .getArchivedProcessInstancesInAllStates(Arrays.asList(archivedProcessInstanceId));
        doThrow(new SProcessInstanceHierarchicalDeletionException("Parent still active", archivedProcessInstanceId)).when(processInstanceService)
                .deleteArchivedParentProcessInstancesAndElements(archivedProcessInstancesToDelete);

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);
    }

    @Test
    public void deleteArchivedProcessInstance_by_id_should_delete_archived_process_instance_when_exist() throws Exception {
        // Given
        final long processInstanceId = 42l;
        doReturn(Arrays.asList(mock(SAProcessInstance.class))).when(processInstanceService).getArchivedProcessInstancesInAllStates(anyListOf(Long.class));

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(processInstanceId);

        // Then
        verify(processInstanceService).getArchivedProcessInstancesInAllStates(anyListOf(Long.class));
        verify(processInstanceService).deleteArchivedParentProcessInstancesAndElements(anyListOf(SAProcessInstance.class));
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstance_by_id_should_throw_exception_when_getArchivedProcessInstance_throws_exception() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doThrow(new SProcessInstanceReadException(new Exception())).when(processInstanceService).getArchivedProcessInstancesInAllStates(anyListOf(Long.class));

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);
    }

    @Test(expected = DeletionException.class)
    public void deleteArchivedProcessInstance_by_id_should_throw_exception_when_deleteParentArchivedProcessInstanceAndElements_throws_exception()
            throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doReturn(Collections.singletonList(mock(SAProcessInstance.class))).when(processInstanceService).getArchivedProcessInstancesInAllStates(
                anyListOf(Long.class));
        doThrow(new SProcessInstanceModificationException(new Exception())).when(processInstanceService).deleteArchivedParentProcessInstancesAndElements(
                anyListOf(SAProcessInstance.class));

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);
    }

    @Test(expected = ProcessInstanceHierarchicalDeletionException.class)
    public void deleteArchivedProcessInstance_by_id_should_throw_exception_when_parent_still_active() throws Exception {
        // Given
        final long archivedProcessInstanceId = 42l;
        doReturn(Collections.singletonList(mock(SAProcessInstance.class))).when(processInstanceService).getArchivedProcessInstancesInAllStates(
                anyListOf(Long.class));
        doThrow(new SProcessInstanceHierarchicalDeletionException("Parent still active", archivedProcessInstanceId)).when(processInstanceService)
                .deleteArchivedParentProcessInstancesAndElements(anyListOf(SAProcessInstance.class));

        // When
        processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstanceId);
    }

    @Test(expected = SearchException.class)
    public void searchEventTriggerInstances_should_throw_exception_when_transaction_throws_exception() throws Exception {
        // Given
        final long processInstanceId = 42l;
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10).done();

        doThrow(new SBonitaReadException(new Exception())).when(eventInstanceService).getNumberOfTimerEventTriggerInstances(eq(processInstanceId),
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
        processAPI.updateExecutionDateOfTimerEventTriggerInstance(timerEventTriggerInstanceId, date);
    }

    @Test(expected = SearchException.class)
    public void searchHumanTaskInstancesWithSearchException() throws Exception {
        // Given
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        doReturn(new SearchHumanTaskInstanceDescriptor()).when(searchEntitiesDescriptor).getSearchHumanTaskInstanceDescriptor();

        final SearchResult<HumanTaskInstance> humanTasksSearch = processAPI.searchHumanTaskInstances(searchOptionsBuilder.sort("tyefv", Order.ASC).done());
        assertThat(0).isEqualTo(humanTasksSearch.getCount());
    }

    @Test
    public void getPendingHumanTaskInstances_should_return_user_tasks_of_enabled_and_disabled_processes() throws Exception {
        final Set<Long> actorIds = new HashSet<Long>();
        actorIds.add(454545L);
        final long userId = 1983L;
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        processDefinitionIds.add(7897987L);
        when(processDefinitionService.getProcessDefinitionIds(0, Integer.MAX_VALUE)).thenReturn(processDefinitionIds);
        final List<SActor> actors = new ArrayList<SActor>();
        final SActor actor = mock(SActor.class);
        actors.add(actor);
        when(actor.getId()).thenReturn(454545L);
        when(actorMappingService.getActors(new HashSet<Long>(processDefinitionIds), userId)).thenReturn(actors);
        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForActivityInstance(ActivityInstanceCriterion.NAME_DESC);

        processAPI.getPendingHumanTaskInstances(userId, 0, 100, ActivityInstanceCriterion.NAME_DESC);

        verify(processDefinitionService).getProcessDefinitionIds(0, Integer.MAX_VALUE);
        verify(actorMappingService).getActors(anySetOf(Long.class), eq(userId));
        verify(activityInstanceService).getPendingTasks(eq(userId), anySetOf(Long.class), eq(0), eq(100), eq(orderAndField.getField()),
                eq(orderAndField.getOrder()));
    }

    @Test
    public void getUserTaskContractVariableValue_should_return_archived_value() throws Exception {
        when(contractDataService.getArchivedUserTaskDataValue(1983L, "id")).thenReturn(10L);

        final Long id = (Long) processAPI.getUserTaskContractVariableValue(1983L, "id");

        assertThat(id).isEqualTo(10L);
    }

    @Test(expected = BonitaRuntimeException.class)
    public void getUserTaskContractVariableValue_should_throw_an_exception_if_an_exception_occurs_when_reading_data() throws Exception {
        //given
        when(contractDataService.getArchivedUserTaskDataValue(1983L, "id")).thenThrow(new SBonitaReadException("exception"));

        //when then exception
        processAPI.getUserTaskContractVariableValue(1983L, "id");
    }

    public void getUserTaskContractVariableValue_should_throw_an_exception_if_an_exception_occurs_when_getting_data() throws Exception {
        when(contractDataService.getArchivedUserTaskDataValue(1983L, "id")).thenThrow(new SContractDataNotFoundException("exception"));

        processAPI.getUserTaskContractVariableValue(1983L, "id");
    }

    @Test
    public void getProcessContract_should_return_null_when_no_contract() throws ProcessDefinitionNotFoundException {
        final ContractDefinition processContract = processAPI.getProcessContract(PROCESS_DEFINITION_ID);

        assertThat(processContract).as("process contract").isNull();
    }

    @Test
    public void getArchivedProcessInstance_Should_Return_Instance_Of_Disabled_Process() throws Exception {
        final long processInstanceId = PROCESS_INSTANCE_ID;
        final long processDefinitionId = PROCESS_DEFINITION_ID;

        final SAProcessInstance saProcessInstance = mock(SAProcessInstance.class);
        when(processInstanceService.getArchivedProcessInstance(processInstanceId)).thenReturn(saProcessInstance);
        when(saProcessInstance.getProcessDefinitionId()).thenReturn(PROCESS_DEFINITION_ID);
        final SProcessDefinition sProcessDefinition = mock(SProcessDefinition.class);
        when(processDefinitionService.getProcessDefinition(processDefinitionId)).thenReturn(sProcessDefinition);

        final ArchivedProcessInstance archivedProcessInstanceMocked = mock(ArchivedProcessInstance.class);
        doReturn(archivedProcessInstanceMocked).when(processAPI).toArchivedProcessInstance(saProcessInstance, sProcessDefinition);

        final ArchivedProcessInstance archivedProcessInstance = processAPI.getArchivedProcessInstance(processInstanceId);
        assertThat(archivedProcessInstance).isEqualTo(archivedProcessInstanceMocked);
    }

    @Test
    public void searchFailedProcessInstancesSupervisedBy_should_Return_ProcessInstances_And_Call_ProcessInstanceService() throws Exception {
        final long userId = 0;
        final ProcessInstance mockedProcessInstance = mock(ProcessInstance.class);
        final IdentityService identityService = mock(IdentityService.class);
        when(tenantAccessor.getIdentityService()).thenReturn(identityService);
        doReturn(mock(GetSUser.class)).when(processAPI).createTxUserGetter(userId, identityService);

        final SearchOptions searchOptions = mock(SearchOptions.class);
        final SearchFailedProcessInstancesSupervisedBy searchFailedProcessInstancesSupervisedBy = mock(SearchFailedProcessInstancesSupervisedBy.class);
        doReturn(searchFailedProcessInstancesSupervisedBy).when(processAPI).createSearchFailedProcessInstancesSupervisedBy(userId, searchOptions,
                processInstanceService, searchEntitiesDescriptor, processDefinitionService);

        final SearchResult<ProcessInstance> searchResult = mock(SearchResult.class);
        when(searchFailedProcessInstancesSupervisedBy.getResult()).thenReturn(searchResult);

        final SearchResult<ProcessInstance> failedProcessInstancesSupervisedBy = processAPI.searchFailedProcessInstancesSupervisedBy(userId,
                searchOptions);
        assertThat(failedProcessInstancesSupervisedBy).isEqualTo(searchResult);
    }

    @Test
    public void searchFailedProcessInstancesSupervisedBy_should_Return_Empty_Result_When_User_Does_Not_Exist() throws Exception {
        final long userId = 0;
        final ProcessInstance mockedProcessInstance = mock(ProcessInstance.class);
        final IdentityService identityService = mock(IdentityService.class);
        when(tenantAccessor.getIdentityService()).thenReturn(identityService);
        final GetSUser getSUser = mock(GetSUser.class);
        doReturn(getSUser).when(processAPI).createTxUserGetter(userId, identityService);
        doThrow(new SBonitaException() {
        }).when(getSUser).execute();

        final SearchResult<ProcessInstance> failedProcessInstancesSupervisedBy = processAPI.searchFailedProcessInstancesSupervisedBy(userId,
                mock(SearchOptions.class));
        assertThat(failedProcessInstancesSupervisedBy.getCount()).isEqualTo(0);
        assertThat(failedProcessInstancesSupervisedBy.getResult()).hasSize(0);
    }

    @Test
    public void should_getUserTaskExecutionContext_evaluate_context_of_activity() throws Exception {
        SExpressionImpl e1 = createExpression("e1");
        userTaskDefinition.getContext().add(new SContextEntryImpl("key1", e1));
        SExpressionImpl e2 = createExpression("e2");
        userTaskDefinition.getContext().add(new SContextEntryImpl("key2", e2));
        doReturn(Arrays.asList("e1", "e2")).when(expressionResolverService).evaluate(eq(Arrays.<SExpression> asList(e1, e2)), any(SExpressionContext.class));

        Map<String, Serializable> userTaskExecutionContext = processAPI.getUserTaskExecutionContext(FLOW_NODE_INSTANCE_ID);

        assertThat(userTaskExecutionContext).containsOnly(entry("key1", "e1"), entry("key2", "e2"));
    }

    @Test
    public void should_getProcessInstanceExecutionContext_evaluate_context_of_process() throws Exception {
        SExpressionImpl e1 = createExpression("e1");
        processDefinition.getContext().add(new SContextEntryImpl("key1", e1));
        SExpressionImpl e2 = createExpression("e2");
        processDefinition.getContext().add(new SContextEntryImpl("key2", e2));
        doReturn(Arrays.asList("e1", "e2")).when(expressionResolverService).evaluate(eq(Arrays.<SExpression> asList(e1, e2)), any(SExpressionContext.class));

        Map<String, Serializable> userTaskExecutionContext = processAPI.getProcessInstanceExecutionContext(PROCESS_INSTANCE_ID);

        assertThat(userTaskExecutionContext).containsOnly(entry("key1", "e1"), entry("key2", "e2"));
    }

    @Test
    public void should_getArchivedPExecutionContext_evaluate_context_of_activity() throws Exception {
        SExpressionImpl e1 = createExpression("e1");
        userTaskDefinition.getContext().add(new SContextEntryImpl("key1", e1));
        SExpressionImpl e2 = createExpression("e2");
        userTaskDefinition.getContext().add(new SContextEntryImpl("key2", e2));
        doReturn(Arrays.asList("e1", "e2")).when(expressionResolverService).evaluate(eq(Arrays.<SExpression> asList(e1, e2)), any(SExpressionContext.class));

        Map<String, Serializable> userTaskExecutionContext = processAPI.getArchivedUserTaskExecutionContext(ARCHIVED_FLOW_NODE_INSTANCE_ID);

        assertThat(userTaskExecutionContext).containsOnly(entry("key1", "e1"), entry("key2", "e2"));
    }

    @Test
    public void should_getArchivedProcessInstanceExecutionContext_evaluate_context_of_process() throws Exception {
        SExpressionImpl e1 = createExpression("e1");
        processDefinition.getContext().add(new SContextEntryImpl("key1", e1));
        SExpressionImpl e2 = createExpression("e2");
        processDefinition.getContext().add(new SContextEntryImpl("key2", e2));
        doReturn(Arrays.asList("e1", "e2")).when(expressionResolverService).evaluate(eq(Arrays.<SExpression> asList(e1, e2)), any(SExpressionContext.class));

        Map<String, Serializable> userTaskExecutionContext = processAPI.getArchivedProcessInstanceExecutionContext(ARCHIVED_PROCESS_INSTANCE_ID);

        assertThat(userTaskExecutionContext).containsOnly(entry("key1", "e1"), entry("key2", "e2"));
    }

    SExpressionImpl createExpression(String name) {
        SExpressionImpl sExpression = new SExpressionImpl();
        sExpression.setName(name);
        return sExpression;
    }

    @Test
    public void getDesignProcessDefinition_Should_Return_Design() throws Exception {
        //given
        int processDefinitionId = 123;
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        doReturn(processDefinitionService).when(tenantAccessor).getProcessDefinitionService();
        DesignProcessDefinition designProcessDefinition = mock(DesignProcessDefinition.class);
        when(processDefinitionService.getDesignProcessDefinition(processDefinitionId)).thenReturn(designProcessDefinition);
        //when
        DesignProcessDefinition designProcessDefinitionResult = processAPI.getDesignProcessDefinition(processDefinitionId);
        //then
        assertThat(designProcessDefinitionResult).isSameAs(designProcessDefinition);
        verify(processDefinitionService).getDesignProcessDefinition(processDefinitionId);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getDesignProcessDefinition_Should_ThrowException_When_No_ProcessDefinition_Exists() throws Exception {
        int processDefinitionId = 123;
        when(tenantAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        doReturn(processDefinitionService).when(tenantAccessor).getProcessDefinitionService();
        when(processDefinitionService.getDesignProcessDefinition(processDefinitionId)).thenThrow(
                new SProcessDefinitionNotFoundException("impossible to found given process definition"));
        processAPI.getDesignProcessDefinition(processDefinitionId);
    }
}
