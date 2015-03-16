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
package org.bonitasoft.engine.core.process.instance.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SBoundaryEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SIntermediateCatchEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SStartEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SCallActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SLoopActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SManualTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SReceiveTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SSubProcessActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceServiceImplTest {

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
    private EventService eventService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private DataInstanceService dataInstanceService;

    @Mock
    private TransitionService transitionService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ConnectorInstanceService connectorInstanceService;

    @Mock
    private DocumentService documentService;

    @Mock
    private SCommentService sCommentService;

    @Spy
    @InjectMocks
    private ProcessInstanceServiceImpl processInstanceService;

    @Before
    public void setUp() throws SBonitaException {
        doCallRealMethod().when(processInstanceService).deleteParentProcessInstanceAndElements(anyListOf(SProcessInstance.class));

        doCallRealMethod().when(processInstanceService).deleteArchivedParentProcessInstancesAndElements(anyListOf(SAProcessInstance.class));
        doCallRealMethod().when(processInstanceService).deleteArchivedParentProcessInstanceAndElements(any(SAProcessInstance.class));

        when(processInstance.getId()).thenReturn(processInstanceId);
        when(aProcessInstance.getId()).thenReturn(archivedProcessInstanceId);
        when(aProcessInstance.getSourceObjectId()).thenReturn(processInstanceId);

        when(archiveService.getDefinitiveArchiveReadPersistenceService()).thenReturn(readPersistenceService);

        doReturn(true).when(technicalLoggerService).isLoggable((Class<?>) any(), eq(TechnicalLogSeverity.DEBUG));
    }

    @Test
    public void deleteParentProcessInstanceAndElementsOnAbsentProcessShouldBeIgnored() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService).deleteProcessInstance(processInstance);
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getProcessInstance(processInstanceId);
        doNothing().when(processInstanceService).logProcessInstanceNotFound(any(SProcessInstanceModificationException.class));

        // when:
        processInstanceService.deleteParentProcessInstanceAndElements(processInstance);

        // then:
        verify(processInstanceService).getProcessInstance(processInstanceId);
    }

    @Test(expected = SBonitaException.class)
    public void deleteParentProcessInstanceAndElements_should_throw_exception_when_deleteProcessInstance_failed() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService).deleteProcessInstance(processInstance);
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
    public void deleteParentArchivedPIAndElementsOnAbsentProcessShouldBeIgnored() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService).deleteArchivedProcessInstanceElements(anyLong(), anyLong());
        doReturn(null).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        doNothing().when(processInstanceService).logArchivedProcessInstanceNotFound(any(SProcessInstanceModificationException.class));

        // when:
        processInstanceService.deleteArchivedParentProcessInstanceAndElements(aProcessInstance);

        // then:
        verify(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
    }

    @Test(expected = SBonitaException.class)
    public void exceptionInDeleteParentArchivedPIAndElementsOnStillExistingProcessShouldRaiseException() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService).deleteArchivedProcessInstanceElements(anyLong(), anyLong());
        // getProcessInstance normally returns:
        doReturn(mock(SAProcessInstance.class)).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);

        try {
            // when:
            processInstanceService.deleteArchivedParentProcessInstanceAndElements(aProcessInstance);
        } finally {
            // then:
            verify(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        }
    }

    @Test(expected = SProcessInstanceModificationException.class)
    public void deleteParentArchivedProcessInstanceAndElementsOnStillExistingProcessShouldRaiseException() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService).deleteArchivedProcessInstanceElements(anyLong(), anyLong());
        // getProcessInstance normally returns:
        doReturn(mock(SAProcessInstance.class)).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);

        try {
            // when:
            processInstanceService.deleteArchivedParentProcessInstanceAndElements(aProcessInstance);
        } finally {
            // then:
            verify(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        }
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_0_when_no_elements_are_deleted() throws Exception {
        assertEquals(0, processInstanceService.deleteParentProcessInstanceAndElements(Collections.<SProcessInstance> emptyList()));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_1_when_1_elements_are_deleted() throws Exception {
        final List<SProcessInstance> processInstances = Arrays.asList(mock(SProcessInstance.class));
        assertEquals(1, processInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        final List<SProcessInstance> processInstances = Arrays.asList(mock(SProcessInstance.class), mock(SProcessInstance.class), mock(SProcessInstance.class));
        assertEquals(3, processInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_0_when_no_elements_are_deleted() throws Exception {
        assertEquals(0, processInstanceService.deleteArchivedParentProcessInstancesAndElements(Collections.<SAProcessInstance> emptyList()));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_1_when_1_elements_are_deleted() throws Exception {
        final List<SAProcessInstance> processInstances = Arrays.asList(mock(SAProcessInstance.class));
        assertEquals(1, processInstanceService.deleteArchivedParentProcessInstancesAndElements(processInstances));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        final List<SAProcessInstance> processInstances = Arrays.asList(mock(SAProcessInstance.class), mock(SAProcessInstance.class),
                mock(SAProcessInstance.class));
        assertEquals(3, processInstanceService.deleteArchivedParentProcessInstancesAndElements(processInstances));
    }

    @Test
    public void testDeleteProcessInstance_delete_archived_activity() throws Exception {
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        when(classLoaderService.getLocalClassLoader("PROCESS", sProcessInstance.getId())).thenReturn(classLoader);
        when(archiveService.getDefinitiveArchiveReadPersistenceService()).thenReturn(mock(ReadPersistenceService.class));
        processInstanceService.deleteParentProcessInstanceAndElements(sProcessInstance);
        verify(processInstanceService, times(1)).deleteProcessInstanceElements(sProcessInstance);
        verify(processInstanceService, times(1)).deleteArchivedProcessInstanceElements(sProcessInstance.getId(), sProcessInstance.getProcessDefinitionId());
        verify(activityInstanceService, times(1)).deleteArchivedFlowNodeInstances(sProcessInstance.getId());
    }

    @Test
    public void getNumberOfFailedProcessInstances_should_return_number_of_failed_process_instance() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(SProcessInstance.class, "Failed", queryOptions, null);

        // When
        final long result = processInstanceService.getNumberOfFailedProcessInstances(queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfFailedProcessInstances_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(SProcessInstance.class, "Failed", queryOptions, null);

        // When
        processInstanceService.getNumberOfFailedProcessInstances(queryOptions);
    }

    @Test
    public void searchFailedProcessInstances_should_return_list_of_failed_process_instance() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final List<ProcessInstance> list = Arrays.asList(mock(ProcessInstance.class));
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
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(SProcessInstance.class, "Failed", queryOptions, null);

        // When
        processInstanceService.searchFailedProcessInstances(queryOptions);
    }

    @Test
    public void getNumberOfOpenProcessInstancesSupervisedBy_should_return_number_of_open_process_instance_supervised_by() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class), eq("SupervisedBy"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        final long result = processInstanceService.getNumberOfOpenProcessInstancesSupervisedBy(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfOpenProcessInstancesSupervisedBy_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class), eq("SupervisedBy"),
                eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        processInstanceService.getNumberOfOpenProcessInstancesSupervisedBy(userId, queryOptions);
    }

    @Test
    public void searchOpenProcessInstancesSupervisedBy_should_return_list_of_open_process_instance_supervised_by() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final List<ProcessInstance> list = Arrays.asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("SupervisedBy"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        final List<SProcessInstance> result = processInstanceService.searchOpenProcessInstancesSupervisedBy(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchOpenProcessInstancesSupervisedBy_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("SupervisedBy"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        processInstanceService.searchOpenProcessInstancesSupervisedBy(userId, queryOptions);
    }

    @Test
    public void getNumberOfOpenProcessInstancesInvolvingUser_should_return_number_of_open_process_instance_involving_user() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final long number = 2L;
        doReturn(number).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class), eq("InvolvingUser"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        final long result = processInstanceService.getNumberOfOpenProcessInstancesInvolvingUser(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfOpenProcessInstancesInvolvingUser_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class), eq("InvolvingUser"),
                eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        processInstanceService.getNumberOfOpenProcessInstancesInvolvingUser(userId, queryOptions);
    }

    @Test
    public void searchOpenProcessInstancesInvolvingUser_should_return_list_of_open_process_instance_involving_user() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final List<ProcessInstance> list = Arrays.asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("InvolvingUser"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        final List<SProcessInstance> result = processInstanceService.searchOpenProcessInstancesInvolvingUser(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchOpenProcessInstancesInvolvingUser_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("InvolvingUser"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

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
        doReturn(number).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class), eq("InvolvingUsersManagedBy"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        final long result = processInstanceService.getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the number returned by the mock.", number, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfOpenProcessInstancesInvolvingUsersManagedBy_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).getNumberOfEntities(eq(SProcessInstance.class), eq("InvolvingUsersManagedBy"),
                eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        processInstanceService.getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);
    }

    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedBy_should_return_list_of_open_process_instance_involving_users_managed_by() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        final List<ProcessInstance> list = Arrays.asList(mock(ProcessInstance.class));
        doReturn(list).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("InvolvingUsersManagedBy"), eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        final List<SProcessInstance> result = processInstanceService.searchOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", list, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchOpenProcessInstancesInvolvingUsersManagedBy_should_throw_exception_when_persistence_service_failed() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 10);
        final long userId = 198L;
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).searchEntity(eq(SProcessInstance.class), eq("InvolvingUsersManagedBy"),
                eq(queryOptions),
                anyMapOf(String.class, Object.class));

        // When
        processInstanceService.searchOpenProcessInstancesInvolvingUsersManagedBy(userId, queryOptions);
    }

    @Test
    public void getArchivedProcessInstancesInAllStates_should_return_list_of_archived_process_instance() throws Exception {
        // Given
        final List<Long> archivedProcessInstanceIds = Arrays.asList(41L);
        final Map<String, Object> parameters = Collections.singletonMap("sourceObjectIds", (Object) archivedProcessInstanceIds);
        final SelectListDescriptor<SAProcessInstance> selectListDescriptor = new SelectListDescriptor<SAProcessInstance>(
                "getArchivedProcessInstancesInAllStates", parameters, SAProcessInstance.class, new QueryOptions(0, archivedProcessInstanceIds.size()));
        final List<SAProcessInstance> saProcessInstances = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(saProcessInstances).when(readPersistenceService).selectList(selectListDescriptor);

        // When
        final List<SAProcessInstance> archivedProcessInstances = processInstanceService.getArchivedProcessInstancesInAllStates(archivedProcessInstanceIds);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", saProcessInstances, archivedProcessInstances);
        verify(readPersistenceService).selectList(selectListDescriptor);
    }

    @Test(expected = SProcessInstanceReadException.class)
    public void getArchivedProcessInstancesInAllStates_should_throw_exception_when_there_is_problem() throws Exception {
        // Given
        final List<Long> archivedProcessInstanceIds = Arrays.asList(41L);
        final Map<String, Object> parameters = Collections.singletonMap("sourceObjectIds", (Object) archivedProcessInstanceIds);
        final SelectListDescriptor<SAProcessInstance> selectListDescriptor = new SelectListDescriptor<SAProcessInstance>(
                "getArchivedProcessInstancesInAllStates", parameters, SAProcessInstance.class, new QueryOptions(0, archivedProcessInstanceIds.size()));
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
        final SAProcessInstance archivedProcessInstance = processInstanceService.getArchivedProcessInstance(archivedProcessInstanceId);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", saProcessInstance, archivedProcessInstance);
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
    public void deleteFlowNodeInstanceElements_should_delete_child_process_instance_when_flownode_is_sub_process() throws Exception {
        final SFlowNodeInstance flowNodeInstance = new SSubProcessActivityInstanceImpl();

        deleteFlowNodeInstanceElements_should_delete_child_process_instance(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_delete_child_process_instance_when_flownode_is_call_activity() throws Exception {
        final SFlowNodeInstance flowNodeInstance = new SCallActivityInstanceImpl();

        deleteFlowNodeInstanceElements_should_delete_child_process_instance(flowNodeInstance);
    }

    private void deleteFlowNodeInstanceElements_should_delete_child_process_instance(final SFlowNodeInstance flowNodeInstance)
            throws SBonitaException {
        // Given
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);
        final SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        doReturn(sProcessInstance).when(processInstanceService).getChildOfActivity(flowNodeInstance.getId());
        doNothing().when(processInstanceService).deleteProcessInstance(sProcessInstance);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(processInstanceService).deleteProcessInstance(sProcessInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_log_exception_when_getChildOfActivity_failed_and_log_is_active() throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SSubProcessActivityInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);
        final SProcessInstanceNotFoundException exception = new SProcessInstanceNotFoundException(6);
        doThrow(exception).when(processInstanceService).getChildOfActivity(flowNodeInstance.getId());

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(technicalLoggerService).log((Class<?>) any(), eq(TechnicalLogSeverity.DEBUG), eq(exception));
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_do_nothing_when_getChildOfActivity_failed_and_no_log() throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SCallActivityInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);
        final SProcessInstanceNotFoundException exception = new SProcessInstanceNotFoundException(6);
        doThrow(exception).when(processInstanceService).getChildOfActivity(flowNodeInstance.getId());
        doReturn(false).when(technicalLoggerService).isLoggable((Class<?>) any(), eq(TechnicalLogSeverity.DEBUG));

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(technicalLoggerService, never()).log((Class<?>) any(), eq(TechnicalLogSeverity.DEBUG), eq(exception));
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_and_deleteEventTriggerInstances_when_flownode_is_type_INTERMEDIATE_CATCH_EVENT()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SIntermediateCatchEventInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
        verify(eventInstanceService).deleteEventTriggerInstances(flowNodeInstance.getId());
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_when_flownode_is_type_RECEIVE_TASK() throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SReceiveTaskInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_when_flownode_is_type_START_EVENT() throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SStartEventInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deleteWaitingEvents_and_deleteEventTriggerInstances_when_flownode_is_type_BOUNDARY_EVENT()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SBoundaryEventInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(eventInstanceService).deleteWaitingEvents(flowNodeInstance);
        verify(eventInstanceService).deleteEventTriggerInstances(flowNodeInstance.getId());
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deletePendingMappings_when_flownode_is_type_USER_TASK()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SUserTaskInstanceImpl("name", 3L, 6L, 9L, 12L, STaskPriority.ABOVE_NORMAL, 7L, 8L);
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(activityInstanceService).deletePendingMappings(flowNodeInstance.getId());
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_call_deletePendingMappings_when_flownode_is_type_MANUAL_TASK()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SManualTaskInstanceImpl("name", 1L, 2L, 3L, 4L, STaskPriority.ABOVE_NORMAL, 5L, 6L);
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(activityInstanceService).deletePendingMappings(flowNodeInstance.getId());
    }

    @Test
    public void deleteFlowNodeInstanceElements_should_just_deleteDataInstancesIfNecessary_and_deleteConnectorInstancesIfNecessary_when_flownode_is_loop()
            throws Exception {
        // Given
        final SFlowNodeInstance flowNodeInstance = new SLoopActivityInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doNothing().when(processInstanceService).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        doNothing().when(processInstanceService).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);

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
        final SFlowNodeInstance flowNodeInstance = new SGatewayInstanceImpl();
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);

        // When
        processInstanceService.deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);

        // Then
        verify(processInstanceService, never()).deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
        verify(processInstanceService, never()).deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);
    }

    @Test
    public void getNumberOfProcessInstances_should_call_getNumberOfEntities() throws Exception {
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("processDefinitionId", 45L);
        final SelectOneDescriptor<Long> countDescriptor = new SelectOneDescriptor<Long>("countProcessInstancesOfProcessDefinition", inputParameters,
                SProcessInstance.class);
        when(readPersistenceService.selectOne(countDescriptor)).thenReturn(4L);

        processInstanceService.getNumberOfProcessInstances(45L);

        verify(readPersistenceService).selectOne(argThat(new SelectOneDescriptorMatcher(countDescriptor)));
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfProcessInstances_should_throw_a_read_exception_if_getNumberOfEntities_does_it() throws Exception {
        when(readPersistenceService.selectOne(Matchers.<SelectOneDescriptor<Long>> any())).thenThrow(
                new SBonitaReadException("error"));

        processInstanceService.getNumberOfProcessInstances(45L);
    }

}
