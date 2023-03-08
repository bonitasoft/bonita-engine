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
package org.bonitasoft.engine.api.impl.application.installer;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bonitasoft.engine.api.result.StatusCode.LIVING_APP_REFERENCES_UNKNOWN_PAGE;
import static org.bonitasoft.engine.api.result.StatusCode.PROCESS_DEPLOYMENT_ENABLEMENT_KO;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.IOUtils.createTempFile;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.ProcessDeploymentAPIDelegate;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationInstallerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private UserTransactionService transactionService;

    @Captor
    ArgumentCaptor<Callable<Object>> callableCaptor;

    @InjectMocks
    @Spy
    private ApplicationInstaller applicationInstaller;

    @Before
    public void before() throws Exception {
        // to bypass the transaction-wrapping part:
        doAnswer(inv -> callableCaptor.getValue().call()).when(transactionService)
                .executeInTransaction(callableCaptor.capture());

        // to bypass the session-wrapping part:
        doAnswer(inv -> callableCaptor.getValue().call()).when(applicationInstaller)
                .inSession(callableCaptor.capture());
    }

    @Test
    public void should_install_application_containing_all_kind_of_custom_pages() throws Exception {
        ApplicationArchive applicationArchive = new ApplicationArchive();
        File page = createTempFile("page", "zip", zip(file("page.properties", "name=page")));
        File layout = createTempFile("layout", "zip", zip(file("page.properties", "name=layout")));
        File theme = createTempFile("theme", "zip", zip(file("page.properties", "name=theme")));
        File restAPI = createTempFile("restApiExtension", "zip", zip(file("page.properties", "name=restApiExtension")));
        applicationArchive.addPage(page)
                .addLayout(layout)
                .addTheme(theme)
                .addRestAPIExtension(restAPI);

        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(mock(Page.class)).when(applicationInstaller).createPage(any(), any());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).createPage(any(), eq("page"));
        verify(applicationInstaller).createPage(any(), eq("layout"));
        verify(applicationInstaller).createPage(any(), eq("theme"));
        verify(applicationInstaller).createPage(any(), eq("restApiExtension"));

        //cleanup
        page.delete();
        layout.delete();
        theme.delete();
        restAPI.delete();
    }

    @Test
    public void should_install_application_containing_living_applications() throws Exception {
        File application = createTempFile("application", "xml", "content".getBytes());
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addApplication(application);
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(emptyList()).when(applicationInstaller).importApplications(any());
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).importApplications("content".getBytes());

        //cleanup
        application.delete();
    }

    @Test
    public void should_not_install_living_applications_if_page_missing() throws Exception {
        final ExecutionResult result = new ExecutionResult();
        ImportStatus importStatus = new ImportStatus("application");
        importStatus.addError(new ImportError("page", ImportError.Type.PAGE));
        List<ImportStatus> importStatuses = Collections.singletonList(importStatus);
        File application = createTempFile("application", "xml", "content".getBytes());
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addApplication(application);
        doReturn(importStatuses).when(applicationInstaller).importApplications(any());

        assertThatExceptionOfType(ApplicationInstallationException.class)
                .isThrownBy(() -> applicationInstaller.installLivingApplications(applicationArchive, result))
                .withMessage("At least one application failed to be installed. Canceling installation.");

        verify(applicationInstaller).importApplications("content".getBytes());
        assertThat(result.getAllStatus()).hasSize(1).extracting("code")
                .containsExactly(LIVING_APP_REFERENCES_UNKNOWN_PAGE);
        //cleanup
        application.delete();
    }

    @Test
    public void should_install_and_enable_resolved_process() throws Exception {
        // given
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(process);
        doNothing().when(applicationInstaller).installOrganization(any(), any());

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();

        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        // when
        applicationInstaller.install(applicationArchive);

        // then
        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(processDeploymentAPIDelegate, never()).enableProcess(123L);

        //cleanup
        process.delete();
    }

    @Test
    public void should_install_bdm() throws Exception {
        byte[] bdmZipContent = createValidBDMZipFile();
        File bdm = createTempFile("bdm", "zip", bdmZipContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .setBdm(bdm);
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doNothing().when(applicationInstaller).pauseTenant();
        doReturn("1.0").when(applicationInstaller).updateBusinessDataModel(applicationArchive);
        doNothing().when(applicationInstaller).resumeTenant();
        doReturn(Collections.emptyList()).when(applicationInstaller).installProcesses(any(), any());
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).pauseTenant();
        verify(applicationInstaller).updateBusinessDataModel(applicationArchive);
        verify(applicationInstaller).resumeTenant();

        //cleanup
        bdm.delete();
    }

    @Test
    public void should_call_enable_resolved_processes() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(process);

        doNothing().when(applicationInstaller).installOrganization(any(), any());
        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);

        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(applicationInstaller).enableResolvedProcesses(eq(Collections.singletonList(123L)), any());

        //cleanup
        process.delete();
    }

    @Test
    public void enableResolvedProcesses_should_enable_processes_resolved_and_not_already_enabled() throws Exception {
        // given:
        final ExecutionResult result = new ExecutionResult();
        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        final ProcessDeploymentInfo info = mock(ProcessDeploymentInfo.class);
        final long processDefId = 234L;
        doReturn(Map.of(processDefId, info)).when(processDeploymentAPIDelegate)
                .getProcessDeploymentInfosFromIds(List.of(processDefId));
        doReturn(processDefId).when(info).getProcessId();
        doReturn(ConfigurationState.RESOLVED).when(info).getConfigurationState();
        doReturn(ActivationState.DISABLED).when(info).getActivationState();

        // when:
        applicationInstaller.enableResolvedProcesses(List.of(processDefId), result);

        // then:
        verify(processDeploymentAPIDelegate).enableProcess(processDefId);
    }

    @Test
    public void enableResolvedProcesses_should_throw_exception_on_unresolved_process() throws Exception {
        // given:
        final ExecutionResult result = new ExecutionResult();
        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        final ProcessDeploymentInfo info = mock(ProcessDeploymentInfo.class);
        final long processDefId = 234L;
        doReturn(Map.of(processDefId, info)).when(processDeploymentAPIDelegate)
                .getProcessDeploymentInfosFromIds(List.of(processDefId));
        doReturn(processDefId).when(info).getProcessId();
        doReturn(ConfigurationState.UNRESOLVED).when(info).getConfigurationState();

        // when:
        assertThatExceptionOfType(ProcessDeployException.class)
                .isThrownBy(() -> applicationInstaller.enableResolvedProcesses(List.of(processDefId), result))
                .withMessage("At least one process failed to deploy / enable. Canceling installation.");

        // then:
        verify(processDeploymentAPIDelegate, never()).enableProcess(anyLong());
        assertThat(result.getAllStatus()).hasSize(1).extracting("code")
                .containsExactly(PROCESS_DEPLOYMENT_ENABLEMENT_KO);
    }

    @Test
    public void should_throw_exception_if_already_existing_process() throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(process);

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        ProcessDefinition myProcess = aProcessDefinition(1193L);
        doThrow(new AlreadyExistsException("already exists"))
                .doReturn(myProcess)
                .when(processDeploymentAPIDelegate)
                .deploy(any());

        // when
        assertThatExceptionOfType(ProcessDeployException.class).isThrownBy(
                () -> applicationInstaller.installProcesses(applicationArchive, executionResult))
                .withMessageContaining("Process myProcess - 1.0 already exists");

        verify(applicationInstaller).deployProcess(ArgumentMatchers
                .argThat(b -> b.getProcessDefinition().getName().equals("myProcess")), any());
        verify(applicationInstaller, never()).enableResolvedProcesses(any(), any());

        //cleanup
        process.delete();
    }

    private ProcessDefinitionImpl aProcessDefinition(long id) {
        ProcessDefinitionImpl myProcess = new ProcessDefinitionImpl("myProcess", "1.0");
        myProcess.setId(id);
        return myProcess;
    }

    private byte[] createValidBusinessArchive()
            throws InvalidBusinessArchiveFormatException, InvalidProcessDefinitionException, IOException {
        BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0").done())
                .done();
        File businessArchiveFile = temporaryFolder.newFile();
        assert businessArchiveFile.delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, businessArchiveFile);
        return FileOperations.readFully(businessArchiveFile);
    }

    private byte[] createValidBDMZipFile() throws IOException {
        return zip(file("bom.xml", ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<businessObjectModel xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\" modelVersion=\"1.0\" productVersion=\"8.0.0\" />")
                        .getBytes()));
    }

}
