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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
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
    private ProcessInstanceServiceImpl processInstanceService;

    private final long processInstanceId = 574815189L;

    private final long archivedProcessInstanceId = 11223344L;

    @Mock
    private SProcessInstance processInstance;

    @Mock
    private SAProcessInstance aProcessInstance;

    @Before
    public void setUp() throws SBonitaException {
        doCallRealMethod().when(processInstanceService).deleteParentProcessInstanceAndElements(anyList());
        doCallRealMethod().when(processInstanceService).deleteParentProcessInstanceAndElements(any(SProcessInstance.class));

        doCallRealMethod().when(processInstanceService).deleteParentArchivedProcessInstancesAndElements(anyList());
        doCallRealMethod().when(processInstanceService).deleteParentArchivedProcessInstanceAndElements(any(SAProcessInstance.class));

        when(processInstance.getId()).thenReturn(processInstanceId);
        when(aProcessInstance.getId()).thenReturn(archivedProcessInstanceId);
        when(aProcessInstance.getSourceObjectId()).thenReturn(processInstanceId);
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
        doThrow(SProcessInstanceModificationException.class).when(processInstanceService).deleteArchivedProcessInstanceElements(anyLong(), anyLong());
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService).getArchivedProcessInstance(archivedProcessInstanceId);
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
}
