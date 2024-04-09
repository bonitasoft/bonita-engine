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
package org.bonitasoft.engine.core.process.instance.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.core.process.instance.impl.ProcessInstanceServiceImpl.IN_REQUEST_SIZE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SBoundaryEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SIntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SStartEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchSignalEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.STimerEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SStartEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceServiceImplTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    private final long processInstanceId = 574815189L;

    private final long archivedProcessInstanceId = 11223344L;

    @Mock
    private SProcessInstance processInstance;

    @Mock
    private SAProcessInstance aProcessInstance;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private EventInstanceService eventInstanceService;

    @Mock
    private Recorder recorder;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private DataInstanceService dataInstanceService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private DocumentService documentService;

    @Mock
    private SCommentService sCommentService;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @Mock
    private ContractDataService contractDataService;

    @Spy
    @InjectMocks
    private ProcessInstanceServiceImpl processInstanceService;

    @Before
    public void setUp() throws SBonitaException {
        doCallRealMethod().when(processInstanceService).deleteParentProcessInstanceAndElements(anyList());

        when(processInstance.getId()).thenReturn(processInstanceId);

        when(archiveService.getDefinitiveArchiveReadPersistenceService()).thenReturn(readPersistenceService);
    }

    @Test
    public void deleteParentProcessInstanceAndElementsOnAbsentProcessShouldBeIgnored() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService)
                .deleteProcessInstance(processInstance);
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService)
                .getProcessInstance(processInstanceId);

        // when:
        processInstanceService.deleteParentProcessInstanceAndElements(processInstance);

        // then:
        verify(processInstanceService).getProcessInstance(processInstanceId);
    }

    @Test(expected = SBonitaException.class)
    public void deleteParentProcessInstanceAndElements_should_throw_exception_when_deleteProcessInstance_failed()
            throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService)
                .deleteProcessInstance(processInstance);
        // getProcessInstance normally returns:
        doReturn(mock(SProcessInstance.class)).when(processInstanceService).getProcessInstance(processInstanceId);

        try {
            // when:
            processInstanceService.deleteParentProcessInstanceAndElements(processInstance);
        } finally {
            // then:
            verify(processInstanceService).getProcessInstance(processInstanceId);
        }
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_0_when_no_elements_are_deleted() throws Exception {
        assertEquals(0, processInstanceService
                .deleteParentProcessInstanceAndElements(Collections.<SProcessInstance> emptyList()));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_1_when_1_elements_are_deleted() throws Exception {
        final List<SProcessInstance> processInstances = asList(mock(SProcessInstance.class));
        assertEquals(1, processInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        final List<SProcessInstance> processInstances = asList(mock(SProcessInstance.class),
                mock(SProcessInstance.class), mock(SProcessInstance.class));
        assertEquals(3, processInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void testDeleteProcessInstance_delete_archived_activity() throws Exception {
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        when(classLoaderService.getClassLoader(identifier(ScopeType.PROCESS, sProcessInstance.getId())))
                .thenReturn(classLoader);
        doReturn(new HashSet<>(asList(4L, 5L, 6L))).when(activityInstanceService)
                .getSourceObjectIdsOfArchivedFlowNodeInstances(any());
        processInstanceService.deleteParentProcessInstanceAndElements(sProcessInstance);
        verify(processInstanceService, times(1)).deleteProcessInstanceElements(sProcessInstance);
        verify(processInstanceService, times(1))
                .deleteArchivedProcessInstances(Collections.singletonList(sProcessInstance.getId()));
        verify(activityInstanceService, times(1)).deleteArchivedFlowNodeInstances(asList(4L, 5L, 6L));
    }

    @Test
    public void getNumberOfFailedProcessInstances_should_return_number_of_failed_process_instance() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(SProcessInstance.class, "Failed",
                queryOptions, null);

        // When
        final long result = processInstanceService.getNumberOfFailedProcessInstances(queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfFailedProcessInstances_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService)
                .getNumberOfEntities(SProcessInstance.class, "Failed", queryOptions, null);

        // When
        processInstanceService.getNumberOfFailedProcessInstances(queryOptions);
    }

    @Test
    public void searchFailedProcessInstances_should_return_list_of_failed_process_instance() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final List<ProcessInstance> list = asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(SProcessInstance.class, "Failed", queryOptions, null);

        // When
        final List<SProcessInstance> result = processInstanceService.searchFailedProcessInstances(queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchFailedProcessInstances_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(SProcessInstance.class,
                "Failed", queryOptions, null);

        // When
        processInstanceService.searchFailedProcessInstances(queryOptions);
    }

    @Test
    public void getNumberOfOpenProcessInstancesSupervisedBy_should_return_number_of_open_process_instance_supervised_by()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class),
                eq("SupervisedBy"), eq(queryOptions),
                anyMap());

        // When
        final long result = processInstanceService.getNumberOfOpenProcessInstancesSupervisedBy(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfOpenProcessInstancesSupervisedBy_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(
                eq(SProcessInstance.class), eq("SupervisedBy"),
                eq(queryOptions),
                anyMap());

        // When
        processInstanceService.getNumberOfOpenProcessInstancesSupervisedBy(userId, queryOptions);
    }

    @Test
    public void searchOpenProcessInstancesSupervisedBy_should_return_list_of_open_process_instance_supervised_by()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final List<ProcessInstance> list = asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("SupervisedBy"),
                eq(queryOptions),
                anyMap());

        // When
        final List<SProcessInstance> result = processInstanceService.searchOpenProcessInstancesSupervisedBy(userId,
                queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchOpenProcessInstancesSupervisedBy_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(eq(SProcessInstance.class),
                eq("SupervisedBy"), eq(queryOptions),
                anyMap());

        // When
        processInstanceService.searchOpenProcessInstancesSupervisedBy(userId, queryOptions);
    }

    @Test
    public void getNumberOfOpenProcessInstancesInvolvingUser_should_return_number_of_open_process_instance_involving_user()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class),
                eq("InvolvingUser"), eq(queryOptions),
                anyMap());

        // When
        final long result = processInstanceService.getNumberOfOpenProcessInstancesInvolvingUser(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfOpenProcessInstancesInvolvingUser_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(
                eq(SProcessInstance.class), eq("InvolvingUser"),
                eq(queryOptions),
                anyMap());

        // When
        processInstanceService.getNumberOfOpenProcessInstancesInvolvingUser(userId, queryOptions);
    }

    @Test
    public void searchOpenProcessInstancesInvolvingUser_should_return_list_of_open_process_instance_involving_user()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final List<ProcessInstance> list = asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("InvolvingUser"),
                eq(queryOptions),
                anyMap());

        // When
        final List<SProcessInstance> result = processInstanceService.searchOpenProcessInstancesInvolvingUser(userId,
                queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchOpenProcessInstancesInvolvingUser_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(eq(SProcessInstance.class),
                eq("InvolvingUser"), eq(queryOptions),
                anyMap());

        // When
        processInstanceService.searchOpenProcessInstancesInvolvingUser(userId, queryOptions);
    }

    @Test
    public void getNumberOfOpenProcessInstancesInvolvingUsersManagedBy_should_return_number_of_open_process_instance_involving_users_managed_by()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class),
                eq("InvolvingUsersManagedBy"), eq(queryOptions),
                anyMap());

        // When
        final long result = processInstanceService.getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(userId,
                queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfOpenProcessInstancesInvolvingUsersManagedBy_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(
                eq(SProcessInstance.class), eq("InvolvingUsersManagedBy"),
                eq(queryOptions),
                anyMap());

        // When
        processInstanceService.getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);
    }

    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedBy_should_return_list_of_open_process_instance_involving_users_managed_by()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final List<ProcessInstance> list = asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(eq(SProcessInstance.class),
                eq("InvolvingUsersManagedBy"), eq(queryOptions),
                anyMap());

        // When
        final List<SProcessInstance> result = processInstanceService
                .searchOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchOpenProcessInstancesInvolvingUsersManagedBy_should_throw_exception_when_persistence_service_failed()
            throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(eq(SProcessInstance.class),
                eq("InvolvingUsersManagedBy"),
                eq(queryOptions),
                anyMap());

        // When
        processInstanceService.searchOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);
    }

    @Test
    public void getArchivedProcessInstancesInAllStates_should_return_list_of_archived_process_instance()
            throws Exception {
        // Given
        final List<Long> archivedProcessInstanceIds = asList(41L);
        final Map<String, Object> parameters = Collections.singletonMap("sourceObjectIds",
                (Object) archivedProcessInstanceIds);
        final SelectListDescriptor<SAProcessInstance> selectListDescriptor = new SelectListDescriptor<SAProcessInstance>(
                "getArchivedProcessInstancesInAllStates", parameters, SAProcessInstance.class,
                new QueryOptions(0, archivedProcessInstanceIds.size()));
        final List<SAProcessInstance> saProcessInstances = asList(mock(SAProcessInstance.class));
        doReturn(saProcessInstances).when(readPersistenceService).selectList(selectListDescriptor);

        // When
        final List<SAProcessInstance> archivedProcessInstances = processInstanceService
                .getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", saProcessInstances,
                archivedProcessInstances);
        verify(readPersistenceService).selectList(selectListDescriptor);
    }

    @Test(expected = SProcessInstanceReadException.class)
    public void getArchivedProcessInstancesInAllStates_should_throw_exception_when_there_is_problem() throws Exception {
        // Given
        final List<Long> archivedProcessInstanceIds = asList(41L);
        final Map<String, Object> parameters = Collections.singletonMap("sourceObjectIds",
                (Object) archivedProcessInstanceIds);
        final SelectListDescriptor<SAProcessInstance> selectListDescriptor = new SelectListDescriptor<SAProcessInstance>(
                "getArchivedProcessInstancesInAllStates", parameters, SAProcessInstance.class,
                new QueryOptions(0, archivedProcessInstanceIds.size()));
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).selectList(selectListDescriptor);

        // When
        processInstanceService.getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);
    }

    @Test
    public void getArchivedProcessInstance_should_return_archived_process_instance() throws Exception {
        // Given
        final long archivedProcessInstanceId = 41L;
        final Map<String, Object> parameters = Collections.singletonMap("id", (Object) archivedProcessInstanceId);
        final SelectOneDescriptor<SAProcessInstance> selectOneDescriptor = new SelectOneDescriptor<SAProcessInstance>(
                "getArchivedProcessInstance", parameters, SAProcessInstance.class);
        final SAProcessInstance saProcessInstance = mock(SAProcessInstance.class);
        doReturn(saProcessInstance).when(readPersistenceService).selectOne(selectOneDescriptor);

        // When
        final SAProcessInstance archivedProcessInstance = processInstanceService
                .getArchivedProcessInstance(archivedProcessInstanceId);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", saProcessInstance,
                archivedProcessInstance);
        verify(readPersistenceService).selectOne(selectOneDescriptor);
    }

    @Test(expected = SProcessInstanceReadException.class)
    public void getArchivedProcessInstance_should_throw_exception_when_there_is_problem() throws Exception {
        // Given
        final long archivedProcessInstanceId = 41L;
        final Map<String, Object> parameters = Collections.singletonMap("id", (Object) archivedProcessInstanceId);
        final SelectOneDescriptor<SAProcessInstance> selectOneDescriptor = new SelectOneDescriptor<SAProcessInstance>(
                "getArchivedProcessInstance", parameters, SAProcessInstance.class);
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).selectOne(selectOneDescriptor);

        // When
        processInstanceService.getArchivedProcessInstance(archivedProcessInstanceId);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_delete_child_process_instance_when_flownode_is_sub_process()
            throws Exception {
        final SFlowNodeInstance flowNodeInstance = new SSubProcessActivityInstance();

        deleteFlowNodeInstanceElements_should_delete_child_process_instance(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_delete_child_process_instance_when_flownode_is_call_activity()
            throws Exception {
        final SFlowNodeInstance flowNodeInstance = new SCallActivityInstance();

        deleteFlowNodeInstanceElements_should_delete_child_process_instance(flowNodeInstance);
    }

    private void deleteFlowNodeInstanceElements_should_delete_child_process_instance(
            final SFlowNodeInstance flowNodeInstance)
            throws SBonitaException {
        // Given
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        doReturn(sProcessInstance).when(processInstanceService).getChildOfActivity(flowNodeInstance.getId());
        doNothing().when(processInstanceService).deleteProcessInstance(sProcessInstance);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(processInstanceService).deleteProcessInstance(sProcessInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_log_exception_when_getChildOfActivity_failed_and_log_is_active()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SSubProcessActivityInstance();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);
        final SProcessInstanceNotFoundException exception = new SProcessInstanceNotFoundException(6);
        doThrow(exception).when(processInstanceService).getChildOfActivity(flowNodeInstance.getId());

        // When
        systemOutRule.clearLog();
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        assertThat(systemOutRule.getLog()).contains("Process instance with id <6> not found");
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_only_log_in_debug_when_getChildOfActivity_failed()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SCallActivityInstance();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);
        final SProcessInstanceNotFoundException exception = new SProcessInstanceNotFoundException(6);
        doThrow(exception).when(processInstanceService).getChildOfActivity(flowNodeInstance.getId());

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);
        assertThat(systemOutRule.getLog()).containsPattern("DEBUG.*.Process instance with id <6> not found");
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_when_flownode_is_type_INTERMEDIATE_CATCH_EVENT()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SIntermediateCatchEventInstance();
        flowNodeInstance.setFlowNodeDefinitionId(42);
        SIntermediateThrowEventDefinitionImpl definition = new SIntermediateThrowEventDefinitionImpl(42, "inter");
        definition.addMessageEventTriggerDefinition(new SThrowMessageEventTriggerDefinitionImpl());
        final SProcessDefinition processDefinition = aProcess().with(definition).done();

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_when_flownode_is_type_RECEIVE_TASK()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SReceiveTaskInstance();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_not_call_deleteWaitingEvents_when_flownode_is_type_START_EVENT_with_no_trigger()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SStartEventInstance();
        flowNodeInstance.setFlowNodeDefinitionId(42);
        final SProcessDefinition processDefinition = aProcess().with(new SStartEventDefinitionImpl(42, "start")).done();

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService, never()).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_when_flownode_is_type_START_EVENT()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SStartEventInstance();
        flowNodeInstance.setFlowNodeDefinitionId(42);
        SStartEventDefinitionImpl start = new SStartEventDefinitionImpl(42, "start");
        start.addSignalEventTrigger(new SCatchSignalEventTriggerDefinitionImpl("signal"));
        final SProcessDefinition processDefinition = aProcess().with(start).done();

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_when_flownode_is_type_BOUNDARY_EVENT()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SBoundaryEventInstance();
        flowNodeInstance.setFlowNodeDefinitionId(42);
        SBoundaryEventDefinitionImpl boundary = new SBoundaryEventDefinitionImpl(42, "boundary");
        boundary.addTimerEventTrigger(new STimerEventTriggerDefinitionImpl(null, null));
        final SProcessDefinition processDefinition = aProcess().with(boundary).done();

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deletePendingMappings_when_flownode_is_type_USER_TASK()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SUserTaskInstance("name", 3L, 6L, 9L, 12L,
                STaskPriority.ABOVE_NORMAL, 7L, 8L);
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(activityInstanceService).deletePendingMappings(flowNodeInstance.getId());
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deletePendingMappings_when_flownode_is_type_MANUAL_TASK()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SManualTaskInstance("name", 1L, 2L, 3L, 4L,
                STaskPriority.ABOVE_NORMAL, 5L, 6L);
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(activityInstanceService).deletePendingMappings(flowNodeInstance.getId());
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_just_deleteDataInstancesIfNecessary_and_deleteConnectorInstancesIfNecessary_when_flownode_is_loop()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SLoopActivityInstance();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(activityInstanceService, never()).deletePendingMappings(flowNodeInstance.getId());
        verify(processInstanceService, never()).deleteSubProcess(flowNodeInstance, processDefinition);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_do_nothing_when_flownode_is_gateway()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SGatewayInstance();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(mock(SFlowElementContainerDefinition.class)).when(processDefinition).getProcessContainer();

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(processInstanceService, never()).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        verify(processInstanceService, never()).deleteConnectorInstancesIfNecessary(flowNodeInstance,
                processDefinition);
    }

    @Test
    public void getNumberOfProcessInstances_should_call_getNumberOfEntities() throws Exception {
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("processDefinitionId", 45L);
        final SelectOneDescriptor<Long> countDescriptor = new SelectOneDescriptor<Long>(
                "countProcessInstancesOfProcessDefinition", inputParameters,
                SProcessInstance.class);
        when(readPersistenceService.selectOne(countDescriptor)).thenReturn(4L);

        processInstanceService.getNumberOfProcessInstances(45L);

        verify(readPersistenceService).selectOne(eq(countDescriptor));
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfProcessInstances_should_throw_a_read_exception_if_getNumberOfEntities_does_it()
            throws Exception {
        when(readPersistenceService.selectOne(ArgumentMatchers.<SelectOneDescriptor<Long>> any())).thenThrow(
                new SBonitaReadException("error"));

        processInstanceService.getNumberOfProcessInstances(45L);
    }

    @Test
    public void _IN_REQUEST_SIZE_should_be_100_for_good_performance() {
        // In any case, it should be <= 1000, as this is the limit of the most restrictive RDBMS we support: Oracle.
        assertThat(IN_REQUEST_SIZE).isLessThanOrEqualTo(100);
    }

    private SProcessDefinitionBuilder aProcess() {
        return new SProcessDefinitionBuilder();
    }

    private class SProcessDefinitionBuilder {

        private SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("aProcess", "1.0");

        SProcessDefinitionBuilder with(SEventDefinition eventDefinition) {
            ((SFlowElementContainerDefinitionImpl) processDefinition.getProcessContainer()).addEvent(eventDefinition);
            return this;
        }

        SProcessDefinition done() {
            return processDefinition;
        }
    }
}
