/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessFormServiceTest {

    @Spy
    ProcessFormService processFormService;

    @Mock
    ProcessAPI processAPI;

    @Mock
    APISession apiSession;

    @Before
    public void beforeEach() throws Exception {
        doReturn(processAPI).when(processFormService).getProcessAPI(apiSession);
        when(apiSession.getUserId()).thenReturn(1L);
    }

    @Test
    public void ensureProcessDefinitionId_with_processDefinitionId() throws Exception {
        assertEquals(42L, processFormService.ensureProcessDefinitionId(apiSession, 42L, -1L, -1L));
    }

    @Test
    public void ensureProcessDefinitionId_with_processInstanceId() throws Exception {
        final ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstance.getProcessDefinitionId()).thenReturn(1L);
        when(processAPI.getProcessInstance(42L)).thenReturn(processInstance);
        assertEquals(1L, processFormService.ensureProcessDefinitionId(apiSession, -1L, 42L, -1L));
    }

    @Test
    public void ensureProcessDefinitionId_with_processInstanceId_on_archived_instance() throws Exception {
        final ArchivedProcessInstance archivedProcessInstance = mock(ArchivedProcessInstance.class);
        when(archivedProcessInstance.getProcessDefinitionId()).thenReturn(1L);
        when(processAPI.getProcessInstance(42L)).thenThrow(ProcessInstanceNotFoundException.class);
        final List<ArchivedProcessInstance> result = new ArrayList<>();
        result.add(archivedProcessInstance);
        final SearchResult<ArchivedProcessInstance> searchResult = mock(SearchResult.class);
        when(searchResult.getCount()).thenReturn(1L);
        when(searchResult.getResult()).thenReturn(result);
        when(processAPI.searchArchivedProcessInstancesInAllStates(any(SearchOptions.class))).thenReturn(searchResult);
        assertEquals(1L, processFormService.ensureProcessDefinitionId(apiSession, -1L, 42L, -1L));
    }

    @Test
    public void ensureProcessDefinitionId_with_taskInstanceId() throws Exception {
        final ActivityInstance activityInstance = mock(ActivityInstance.class);
        when(activityInstance.getProcessDefinitionId()).thenReturn(1L);
        when(processAPI.getActivityInstance(42L)).thenReturn(activityInstance);
        assertEquals(1L, processFormService.ensureProcessDefinitionId(apiSession, -1L, -1L, 42L));
    }

    @Test
    public void ensureProcessDefinitionId_with_taskInstanceId_on_archived_instance() throws Exception {
        final ArchivedActivityInstance archivedActivityInstance = mock(ArchivedActivityInstance.class);
        when(archivedActivityInstance.getProcessDefinitionId()).thenReturn(1L);
        when(processAPI.getActivityInstance(42L)).thenThrow(ActivityInstanceNotFoundException.class);
        when(processAPI.getArchivedActivityInstance(42L)).thenReturn(archivedActivityInstance);
        assertEquals(1L, processFormService.ensureProcessDefinitionId(apiSession, -1L, -1L, 42L));
    }

    @Test
    public void getTaskName_with_taskInstanceId() throws Exception {
        final ActivityInstance activityInstance = mock(ActivityInstance.class);
        when(activityInstance.getName()).thenReturn("myTask");
        when(processAPI.getActivityInstance(42L)).thenReturn(activityInstance);
        assertEquals("myTask", processFormService.getTaskName(apiSession, 42L));
    }

    @Test
    public void getTaskName_with_taskInstanceId_on_archived_instance() throws Exception {
        final ArchivedActivityInstance archivedActivityInstance = mock(ArchivedActivityInstance.class);
        when(archivedActivityInstance.getName()).thenReturn("myTask");
        when(processAPI.getActivityInstance(42L)).thenThrow(ActivityInstanceNotFoundException.class);
        when(processAPI.getArchivedActivityInstance(42L)).thenReturn(archivedActivityInstance);
        assertEquals("myTask", processFormService.getTaskName(apiSession, 42L));
    }

    @Test
    public void getTaskInstanceId_with_taskInstanceId() throws Exception {
        final HumanTaskInstance humanTaskInstance = mock(HumanTaskInstance.class);
        when(humanTaskInstance.getId()).thenReturn(1L);
        final List<HumanTaskInstance> result = new ArrayList<>();
        result.add(humanTaskInstance);
        final SearchResult<HumanTaskInstance> searchResult = mock(SearchResult.class);
        when(searchResult.getCount()).thenReturn(1L);
        when(searchResult.getResult()).thenReturn(result);
        when(processAPI.searchMyAvailableHumanTasks(anyLong(), any(SearchOptions.class))).thenReturn(searchResult);
        assertEquals(1L, processFormService.getTaskInstanceId(apiSession, 42L, "myTask", -1L));
    }

    @Test
    public void getTaskInstanceId_with_taskInstanceId_on_archived_instance() throws Exception {
        when(processAPI.searchMyAvailableHumanTasks(anyLong(), any(SearchOptions.class)))
                .thenReturn(mock(SearchResult.class));
        final ArchivedHumanTaskInstance archivedHumanTaskInstance = mock(ArchivedHumanTaskInstance.class);
        when(archivedHumanTaskInstance.getSourceObjectId()).thenReturn(1L);
        final List<ArchivedHumanTaskInstance> result = new ArrayList<>();
        result.add(archivedHumanTaskInstance);
        final SearchResult<ArchivedHumanTaskInstance> searchResult = mock(SearchResult.class);
        when(searchResult.getCount()).thenReturn(1L);
        when(searchResult.getResult()).thenReturn(result);
        when(processAPI.searchArchivedHumanTasks(any(SearchOptions.class))).thenReturn(searchResult);
        assertEquals(1L, processFormService.getTaskInstanceId(apiSession, 42L, "myTask", -1L));
    }

    @Test
    public void getProcessDefinitionId_with_no_version() throws Exception {
        assertEquals(-1L, processFormService.getProcessDefinitionId(apiSession, "myProcess", null));
    }

    @Test
    public void getProcessDefinitionId_with_no_name_and_version() throws Exception {
        assertEquals(-1L, processFormService.getProcessDefinitionId(apiSession, null, null));
    }

    @Test
    public void getProcessDefinitionUUID_should_return_valid_UUID() throws Exception {
        final ProcessDeploymentInfo processDeploymentInfo = mock(ProcessDeploymentInfo.class);
        when(processDeploymentInfo.getName()).thenReturn("processName");
        when(processDeploymentInfo.getVersion()).thenReturn("processVersion");
        when(processAPI.getProcessDeploymentInfo(1L)).thenReturn(processDeploymentInfo);

        final String uuid = processFormService.getProcessPath(apiSession, 1L);

        assertEquals("processName/processVersion", uuid);
    }

    @Test
    public void getProcessDefinitionUUID_should_return_valid_UUID_with_special_characters() throws Exception {
        final ProcessDeploymentInfo processDeploymentInfo = mock(ProcessDeploymentInfo.class);
        when(processDeploymentInfo.getName()).thenReturn("process Name/é");
        when(processDeploymentInfo.getVersion()).thenReturn("process Version ø");
        when(processAPI.getProcessDeploymentInfo(1L)).thenReturn(processDeploymentInfo);

        final String uuid = processFormService.getProcessPath(apiSession, 1L);

        assertEquals("process%20Name/%C3%A9/process%20Version%20%C3%B8", uuid);
    }
}
