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
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.io.FileAndContent;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
        applicationArchive.addPage(new FileAndContent("page.zip", zip(file("page.properties", "name=page"))))
                .addLayout(new FileAndContent("layout.zip", zip(file("page.properties", "name=layout"))))
                .addTheme(new FileAndContent("theme.zip", zip(file("page.properties", "name=theme"))))
                .addRestAPIExtension(new FileAndContent("restApiExtension.zip",
                        zip(file("page.properties", "name=restApiExtension"))));

        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(mock(Page.class)).when(applicationInstaller).createPage(any(), any());
        doReturn(null).when(applicationInstaller).getPage(anyString());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).createPage(any(), eq("page"));
        verify(applicationInstaller).createPage(any(), eq("layout"));
        verify(applicationInstaller).createPage(any(), eq("theme"));
        verify(applicationInstaller).createPage(any(), eq("restApiExtension"));
    }

    @Test
    public void should_install_application_containing_living_applications() throws Exception {
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addApplication(new FileAndContent("application.xml", "content".getBytes()));
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(emptyList()).when(applicationInstaller).importApplications(any());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).importApplications("content".getBytes());
    }

    @Test
    public void should_install_and_enable_resolved_process() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(new FileAndContent("process.bar", barContent));
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(applicationInstaller).deployProcess(any());
        doNothing().when(applicationInstaller).enableProcess(myProcess);
        mockConfigurationState(myProcess, ConfigurationState.RESOLVED);

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).deployProcess(any());
        verify(applicationInstaller).enableProcess(myProcess);
    }

    @Test
    public void should_install_only_unresolved_process() throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive();
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(new FileAndContent("process.bar", barContent));
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(applicationInstaller).deployProcess(any());
        mockConfigurationState(myProcess, ConfigurationState.UNRESOLVED);
        final List<ProblemImpl> problems = List.of(new ProblemImpl(Problem.Level.ERROR, "", "", ""));
        doReturn(problems).when(applicationInstaller).getProcessResolutionProblems(myProcess);

        applicationInstaller.installProcesses(applicationArchive, executionResult);

        verify(applicationInstaller).deployProcess(any());
        verify(applicationInstaller, never()).enableProcess(any());
        assertThat(executionResult.getAllStatus()).anyMatch((status) -> status.getLevel() == Status.Level.WARNING);
    }

    @Test
    public void should_replace_any_already_existing_process() throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive();
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(new FileAndContent("process.bar", barContent));
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doThrow(new AlreadyExistsException("already exists"))
                .doReturn(myProcess)
                .when(applicationInstaller)
                .deployProcess(any(BusinessArchive.class));
        doReturn(456L).when(applicationInstaller).getProcessDefinitionId("myProcess", "1.0");
        doReturn(emptyList()).when(applicationInstaller).getExistingProcessInstances(any());
        doReturn(emptyList()).when(applicationInstaller).getExistingArchivedProcessInstances(any());
        mockConfigurationState(myProcess, ConfigurationState.UNRESOLVED);
        doReturn(emptyList()).when(applicationInstaller).getProcessResolutionProblems(myProcess);

        applicationInstaller.installProcesses(applicationArchive, executionResult);

        verify(applicationInstaller).disableProcess(456L);
        verify(applicationInstaller).deleteProcessDefinition(456L);
        verify(applicationInstaller, times(2)).deployProcess(ArgumentMatchers
                .argThat(b -> b.getProcessDefinition().getName().equals("myProcess")));
    }

    private ProcessDefinitionImpl aProcessDefinition(long id) {
        ProcessDefinitionImpl myProcess = new ProcessDefinitionImpl("myProcess", "1.0");
        myProcess.setId(id);
        return myProcess;
    }

    private void mockConfigurationState(ProcessDefinition myProcess, ConfigurationState state)
            throws ProcessDefinitionNotFoundException {
        ProcessDeploymentInfo processDeploymentInfo = mock(ProcessDeploymentInfo.class);
        doReturn(state).when(processDeploymentInfo).getConfigurationState();
        doReturn(processDeploymentInfo).when(applicationInstaller).getProcessDeploymentInfo(myProcess);
    }

    private byte[] createValidBusinessArchive()
            throws InvalidBusinessArchiveFormatException, InvalidProcessDefinitionException, IOException {
        BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0").done())
                .done();
        File businessArchiveFile = temporaryFolder.newFile();
        businessArchiveFile.delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, businessArchiveFile);
        return FileOperations.readFully(businessArchiveFile);
    }

    private byte[] createValidBDMZipFile() throws IOException {
        return zip(file("bom.xml", ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<businessObjectModel xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\" modelVersion=\"1.0\" productVersion=\"8.0.0\" />")
                        .getBytes()));
    }

}
