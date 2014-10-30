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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceServiceImplTest {

    @Mock
    private ProcessInstanceServiceImpl mockedProcessInstanceService;

    private ProcessInstanceServiceImpl processInstanceService;

    private final long processInstanceId = 574815189L;

    private final long archivedProcessInstanceId = 11223344L;

    @Mock
    private SProcessInstance processInstance;

    @Mock
    private SAProcessInstance aProcessInstance;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private EventInstanceService eventService;

    @Mock
    private Recorder mock;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private ReadPersistenceService readPersistenceService;;

    @Before
    public void setUp() throws SBonitaException {
        processInstanceService = spy(new ProcessInstanceServiceImpl(mock,
                readPersistenceService, mock(EventService.class),
                mock(ActivityInstanceService.class), mock(TechnicalLoggerService.class),
                eventService, mock(DataInstanceService.class),
                archiveService, mock(TransitionService.class), mock(ProcessDefinitionService.class), mock(ConnectorInstanceService.class),
                classLoaderService, mock(DocumentService.class), mock(SCommentService.class), mock(TokenService.class)));

        doCallRealMethod().when(mockedProcessInstanceService).deleteParentProcessInstanceAndElements(anyList());
        doCallRealMethod().when(mockedProcessInstanceService).deleteParentProcessInstanceAndElements(any(SProcessInstance.class));

        doCallRealMethod().when(mockedProcessInstanceService).deleteParentArchivedProcessInstancesAndElements(anyList());
        doCallRealMethod().when(mockedProcessInstanceService).deleteParentArchivedProcessInstanceAndElements(any(SAProcessInstance.class));

        when(processInstance.getId()).thenReturn(processInstanceId);
        when(aProcessInstance.getId()).thenReturn(archivedProcessInstanceId);
        when(aProcessInstance.getSourceObjectId()).thenReturn(processInstanceId);
    }

    @Test
    public void deleteParentPIAndElementsOnAbsentProcessShouldBeIgnored() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(mockedProcessInstanceService).deleteProcessInstance(processInstance);
        doThrow(SProcessInstanceNotFoundException.class).when(mockedProcessInstanceService).getProcessInstance(processInstanceId);
        doNothing().when(mockedProcessInstanceService).logProcessInstanceNotFound(any(SProcessInstanceModificationException.class));

        // when:
        mockedProcessInstanceService.deleteParentProcessInstanceAndElements(processInstance);

        // then:
        verify(mockedProcessInstanceService).getProcessInstance(processInstanceId);
    }

    @Test(expected = SBonitaException.class)
    public void exceptionInDeleteParentPIAndElementsOnStillExistingProcessShouldRaiseException() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(mockedProcessInstanceService).deleteProcessInstance(processInstance);
        // getProcessInstance normally returns:
        doReturn(mock(SProcessInstance.class)).when(mockedProcessInstanceService).getProcessInstance(processInstanceId);

        try {
            // when:
            mockedProcessInstanceService.deleteParentProcessInstanceAndElements(processInstance);
        } finally {
            // then:
            verify(mockedProcessInstanceService).getProcessInstance(processInstanceId);
        }
    }

    @Test
    public void deleteParentArchivedPIAndElementsOnAbsentProcessShouldBeIgnored() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(mockedProcessInstanceService).deleteArchivedProcessInstanceElements(anyLong(), anyLong());
        doThrow(SProcessInstanceNotFoundException.class).when(mockedProcessInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        doNothing().when(mockedProcessInstanceService).logArchivedProcessInstanceNotFound(any(SProcessInstanceModificationException.class));

        // when:
        mockedProcessInstanceService.deleteParentArchivedProcessInstanceAndElements(aProcessInstance);

        // then:
        verify(mockedProcessInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
    }

    @Test(expected = SBonitaException.class)
    public void exceptionInDeleteParentArchivedPIAndElementsOnStillExistingProcessShouldRaiseException() throws Exception {
        // given:
        doThrow(SProcessInstanceModificationException.class).when(mockedProcessInstanceService).deleteArchivedProcessInstanceElements(anyLong(), anyLong());
        // getProcessInstance normally returns:
        doReturn(mock(SAProcessInstance.class)).when(mockedProcessInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);

        try {
            // when:
            mockedProcessInstanceService.deleteParentArchivedProcessInstanceAndElements(aProcessInstance);
        } finally {
            // then:
            verify(mockedProcessInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
        }
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_0_when_no_elements_are_deleted() throws Exception {
        assertEquals(0, mockedProcessInstanceService.deleteParentProcessInstanceAndElements(Collections.<SProcessInstance> emptyList()));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_1_when_1_elements_are_deleted() throws Exception {
        final List<SProcessInstance> processInstances = Arrays.asList(mock(SProcessInstance.class));
        assertEquals(1, mockedProcessInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentProcessInstanceAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        final List<SProcessInstance> processInstances = Arrays.asList(mock(SProcessInstance.class), mock(SProcessInstance.class), mock(SProcessInstance.class));
        assertEquals(3, mockedProcessInstanceService.deleteParentProcessInstanceAndElements(processInstances));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_0_when_no_elements_are_deleted() throws Exception {
        assertEquals(0, mockedProcessInstanceService.deleteParentArchivedProcessInstancesAndElements(Collections.<SAProcessInstance> emptyList()));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_1_when_1_elements_are_deleted() throws Exception {
        final List<SAProcessInstance> processInstances = Arrays.asList(mock(SAProcessInstance.class));
        assertEquals(1, mockedProcessInstanceService.deleteParentArchivedProcessInstancesAndElements(processInstances));
    }

    @Test
    public void deleteParentArchivedProcessInstancesAndElements_returns_n_when_n_elements_are_deleted() throws Exception {
        final List<SAProcessInstance> processInstances = Arrays.asList(mock(SAProcessInstance.class), mock(SAProcessInstance.class), mock(SAProcessInstance.class));
        assertEquals(3, mockedProcessInstanceService.deleteParentArchivedProcessInstancesAndElements(processInstances));
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
        verify(processInstanceService, times(1)).deleteArchivedFlowNodeInstances(sProcessInstance.getId());
    }

    @Test
    public void getNumberOfProcessInstances_should_call_getNumberOfEntities() throws Exception {
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("processDefinitionId", 45L);
        final SelectOneDescriptor<Long> countDescriptor = new SelectOneDescriptor<Long>("countProcessInstancesOfProcessDefinition", inputParameters,
                SProcessInstance.class);
        when(readPersistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(4L);

        processInstanceService.getNumberOfProcessInstances(45L);

        verify(readPersistenceService).selectOne(argThat(new SelectOneDescriptorMatcher(countDescriptor)));
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfProcessInstances_should_throw_a_read_exception_if_getNumberOfEntities_does_it() throws Exception {
        when(readPersistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException("error"));

        processInstanceService.getNumberOfProcessInstances(45L);
    }

}
