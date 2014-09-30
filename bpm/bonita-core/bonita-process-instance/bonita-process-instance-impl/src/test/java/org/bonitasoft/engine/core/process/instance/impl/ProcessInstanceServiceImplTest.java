/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
    private Recorder mock;

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
    private ProcessDocumentService processDocumentService;

    @Mock
    private SCommentService sCommentService;

    @Mock
    private TokenService tokenService;

    @Spy
    @InjectMocks
    private ProcessInstanceServiceImpl processInstanceService;

    @Before
    public void setUp() throws SBonitaException {
        doCallRealMethod().when(processInstanceService).deleteParentProcessInstanceAndElements(anyList());
        doCallRealMethod().when(processInstanceService).deleteParentProcessInstanceAndElements(any(SProcessInstance.class));

        doCallRealMethod().when(processInstanceService).deleteParentArchivedProcessInstancesAndElements(anyList());
        doCallRealMethod().when(processInstanceService).deleteParentArchivedProcessInstanceAndElements(any(SAProcessInstance.class));

        when(processInstance.getId()).thenReturn(processInstanceId);
        when(aProcessInstance.getId()).thenReturn(archivedProcessInstanceId);
        when(aProcessInstance.getSourceObjectId()).thenReturn(processInstanceId);

        when(archiveService.getDefinitiveArchiveReadPersistenceService()).thenReturn(readPersistenceService);
    }

    @Test
    public void deleteParentPIAndElementsOnAbsentProcessShouldBeIgnored() throws Exception {
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
    public void exceptionInDeleteParentPIAndElementsOnStillExistingProcessShouldRaiseException() throws Exception {
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
        doThrow(new SProcessInstanceModificationException(new Exception())).when(processInstanceService).deleteArchivedProcessInstanceElements(anyLong(),
                anyLong());
        doThrow(new SProcessInstanceNotFoundException(aProcessInstance.getId())).when(processInstanceService).getArchivedProcessInstance(
                archivedProcessInstanceId);
        doNothing().when(processInstanceService).logArchivedProcessInstanceNotFound(any(SProcessInstanceModificationException.class));

        // when:
        processInstanceService.deleteParentArchivedProcessInstanceAndElements(aProcessInstance);

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
            processInstanceService.deleteParentArchivedProcessInstanceAndElements(aProcessInstance);
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
        List<SProcessInstance> processInstances = Arrays.asList(mock(SProcessInstance.class));
        assertEquals(1, processInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        List<SProcessInstance> processInstances = Arrays.asList(mock(SProcessInstance.class), mock(SProcessInstance.class), mock(SProcessInstance.class));
        assertEquals(3, processInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_0_when_no_elements_are_deleted() throws Exception {
        assertEquals(0, processInstanceService.deleteParentArchivedProcessInstancesAndElements(Collections.<SAProcessInstance> emptyList()));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_1_when_1_elements_are_deleted() throws Exception {
        List<SAProcessInstance> processInstances = Arrays.asList(mock(SAProcessInstance.class));
        assertEquals(1, processInstanceService.deleteParentArchivedProcessInstancesAndElements(processInstances));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        List<SAProcessInstance> processInstances = Arrays.asList(mock(SAProcessInstance.class), mock(SAProcessInstance.class), mock(SAProcessInstance.class));
        assertEquals(3, processInstanceService.deleteParentArchivedProcessInstancesAndElements(processInstances));
    }

    @Test
    public void deleteProcessInstance_delete_archived_activity() throws Exception {
        SProcessInstance sProcessInstance = mock(SProcessInstance.class);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        when(classLoaderService.getLocalClassLoader("PROCESS", sProcessInstance.getId())).thenReturn(classLoader);
        when(archiveService.getDefinitiveArchiveReadPersistenceService()).thenReturn(mock(ReadPersistenceService.class));
        processInstanceService.deleteParentProcessInstanceAndElements(sProcessInstance);
        verify(processInstanceService, times(1)).deleteProcessInstanceElements(sProcessInstance);
        verify(processInstanceService, times(1)).deleteArchivedProcessInstanceElements(sProcessInstance.getId(), sProcessInstance.getProcessDefinitionId());
        verify(processInstanceService, times(1)).deleteArchivedFlowNodeInstances(sProcessInstance.getId());
    }

    @Test
    public void getArchivedProcessInstances_should_return_list_of_archived_process_instance() throws Exception {
        // Given
        final List<Long> archivedProcessInstanceIds = Arrays.asList(41L);
        final Map<String, Object> parameters = Collections.singletonMap("ids", (Object) archivedProcessInstanceIds);
        final SelectListDescriptor<SAProcessInstance> selectListDescriptor = new SelectListDescriptor<SAProcessInstance>(
                "getArchivedProcessInstances", parameters, SAProcessInstance.class, new QueryOptions(0, archivedProcessInstanceIds.size()));
        final List<SAProcessInstance> saProcessInstances = Arrays.asList(mock(SAProcessInstance.class));
        doReturn(saProcessInstances).when(readPersistenceService).selectList(selectListDescriptor);

        // When
        final List<SAProcessInstance> archivedProcessInstances = processInstanceService.getArchivedProcessInstances(archivedProcessInstanceIds);

        // Then
        assertEquals("The result should be equals to the list returned by the mock.", saProcessInstances, archivedProcessInstances);
        verify(readPersistenceService).selectList(selectListDescriptor);
    }

    @Test(expected = SProcessInstanceReadException.class)
    public void getArchivedProcessInstances_should_throw_exception_when_there_is_problem() throws Exception {
        // Given
        final List<Long> archivedProcessInstanceIds = Arrays.asList(41L);
        final Map<String, Object> parameters = Collections.singletonMap("ids", (Object) archivedProcessInstanceIds);
        final SelectListDescriptor<SAProcessInstance> selectListDescriptor = new SelectListDescriptor<SAProcessInstance>(
                "getArchivedProcessInstances", parameters, SAProcessInstance.class, new QueryOptions(0, archivedProcessInstanceIds.size()));
        doThrow(new SBonitaReadException("plop")).when(readPersistenceService).selectList(selectListDescriptor);

        // When
        processInstanceService.getArchivedProcessInstances(archivedProcessInstanceIds);
    }
}
