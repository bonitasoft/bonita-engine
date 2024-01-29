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
package org.bonitasoft.engine.service.impl.installation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.parameter.ParameterService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InstallationServiceImplTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ParameterService parameterService;
    @Mock
    private BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    @Mock
    private EventsHandler eventsHandler;

    @Spy
    @InjectMocks
    private InstallationServiceImpl service;

    @Test
    public void should_merge_parameters_for_each_process_configuration_and_enable_the_process() throws Exception {
        // Given
        when(processDefinitionService.getProcessDefinitionId("RequestLoan", "1.3.1")).thenReturn(123L);
        when(processDefinitionService.getProcessDefinitionId("LoanRequestBot", "1.3.1")).thenReturn(321L);
        when(processDefinitionService.getProcessDefinition(123L))
                .thenReturn(new SProcessDefinitionImpl("RequestLoan", "1.3.1"));
        when(processDefinitionService.getProcessDefinition(321L))
                .thenReturn(new SProcessDefinitionImpl("LoanRequestBot", "1.3.1"));
        final SProcessDefinitionDeployInfo info = mock(SProcessDefinitionDeployInfo.class);
        doReturn(info).when(processDefinitionService).getProcessDeploymentInfo(123L);
        doReturn(info).when(processDefinitionService).getProcessDeploymentInfo(321L);
        doReturn("DISABLED").when(info).getActivationState();

        // When
        try (InputStream is = InstallationServiceImplTest.class.getResourceAsStream("/test.bconf")) {
            service.install(null, is.readAllBytes());
        }

        // Then
        ArgumentCaptor<Map> parametersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(parameterService).merge(eq(123L), (java.util.Map<String, String>) parametersCaptor.capture());
        assertThat(parametersCaptor.getValue()).contains(entry("botActivated", "true"),
                entry("smtpPassword", "dfghj65789)IU"));
        parametersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(parameterService).merge(eq(321L), (java.util.Map<String, String>) parametersCaptor.capture());
        assertThat(parametersCaptor.getValue()).contains(entry("validateLoanMinTime", "36000000"),
                entry("completeLoanMinTime", "600000"));
    }

    @Test
    public void should_check_process_resolution_and_enable_process() throws Exception {
        // Given
        when(processDefinitionService.getProcessDefinitionId("RequestLoan", "1.3.1")).thenReturn(123L);
        when(processDefinitionService.getProcessDefinitionId("LoanRequestBot", "1.3.1")).thenReturn(779L);
        final SProcessDefinition processDefinition1 = mock(SProcessDefinition.class);
        doReturn(new SFlowElementContainerDefinitionImpl()).when(processDefinition1).getProcessContainer();
        doReturn(processDefinition1).when(processDefinitionService).getProcessDefinition(123L);
        final SProcessDefinition processDefinition2 = mock(SProcessDefinition.class);
        doReturn(new SFlowElementContainerDefinitionImpl()).when(processDefinition2).getProcessContainer();
        doReturn(processDefinition2).when(processDefinitionService).getProcessDefinition(779L);
        // no need to mock the return of checkResolution, as it is empty list by default.
        final SProcessDefinitionDeployInfo info = mock(SProcessDefinitionDeployInfo.class);
        doReturn(info).when(processDefinitionService).getProcessDeploymentInfo(123L);
        doReturn(info).when(processDefinitionService).getProcessDeploymentInfo(779L);
        doReturn("DISABLED").when(info).getActivationState();

        // When
        try (InputStream is = InstallationServiceImplTest.class.getResourceAsStream("/test.bconf")) {
            service.install(null, is.readAllBytes());
        }

        // Then
        verify(businessArchiveArtifactsManager).getProcessResolutionProblems(processDefinition1);
        verify(businessArchiveArtifactsManager).getProcessResolutionProblems(processDefinition2);
        verify(businessArchiveArtifactsManager).changeResolutionStatus(123L, processDefinitionService, true);
        verify(processDefinitionService).enableProcess(123L, false);
        verify(businessArchiveArtifactsManager).changeResolutionStatus(779L, processDefinitionService, true);
        verify(processDefinitionService).enableProcess(779L, false);

        verify(processDefinitionService, times(0)).disableProcess(anyLong(), anyBoolean());
    }

    @Test
    public void should_check_process_resolution_and_disable_process_if_unresolved() throws Exception {
        // Given
        when(processDefinitionService.getProcessDefinitionId("RequestLoan", "1.3.1")).thenReturn(123L);
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(processDefinition).when(processDefinitionService).getProcessDefinition(123L);
        doReturn(Collections.singletonList(mock(Problem.class))).when(businessArchiveArtifactsManager)
                .getProcessResolutionProblems(processDefinition);
        final SProcessDefinitionDeployInfo info = mock(SProcessDefinitionDeployInfo.class);
        doReturn(info).when(processDefinitionService).getProcessDeploymentInfo(123L);
        doReturn("ENABLED").when(info).getActivationState();

        // When
        try (InputStream is = this.getClass().getResourceAsStream("/RequestLoan_conf_with_null_params.bconf")) {
            service.install(null, is.readAllBytes());
        }

        // Then
        verify(businessArchiveArtifactsManager).getProcessResolutionProblems(processDefinition);
        verify(businessArchiveArtifactsManager).changeResolutionStatus(123L, processDefinitionService, false);
        verify(processDefinitionService).disableProcess(123L, false);

        verify(processDefinitionService, times(0)).enableProcess(anyLong(), anyBoolean());
    }

    @Test
    public void should_not_throw_an_InstallationFailedException_if_process_def_is_not_found() throws Exception {
        // Given
        doThrow(new SProcessDefinitionNotFoundException("RequestLoan"))
                .when(processDefinitionService).getProcessDefinitionId("RequestLoan", "1.3.1");
        final long processDefinitionId = 987L;
        doReturn(processDefinitionId).when(processDefinitionService).getProcessDefinitionId("LoanRequestBot", "1.3.1");
        doReturn(new SProcessDefinitionImpl("LoanRequestBot", "1.3.1")).when(processDefinitionService)
                .getProcessDefinition(processDefinitionId);
        final SProcessDefinitionDeployInfo info = mock(SProcessDefinitionDeployInfo.class);
        doReturn(info).when(processDefinitionService).getProcessDeploymentInfo(processDefinitionId);
        doReturn("ENABLED").when(info).getActivationState();

        systemOutRule.clearLog();
        // When
        try (InputStream is = InstallationServiceImplTest.class.getResourceAsStream("/test.bconf")) {
            service.install(null, is.readAllBytes());
        }

        assertThat(systemOutRule.getLog()).containsPattern("WARN.*.BCONF file for non existing process ");
    }

    @Test
    public void should_throw_an_IllegalStateException_if_a_binaries_archive_is_given() throws Exception {
        try (InputStream is = InstallationServiceImplTest.class.getResourceAsStream("/test.bconf")) {
            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> service.install(is.readAllBytes(), null));
        }
    }

}
