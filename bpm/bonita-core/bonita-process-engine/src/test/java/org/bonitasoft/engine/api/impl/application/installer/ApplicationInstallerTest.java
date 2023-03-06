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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.IOUtils.createTempFile;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.exception.AlreadyExistsException;
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

        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(mock(Page.class)).when(applicationInstaller).createPage(any(), any());
        doReturn(null).when(applicationInstaller).getPage(anyString());

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

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).importApplications("content".getBytes());

        //cleanup
        application.delete();
    }

    @Test
    public void should_install_and_enable_resolved_process() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(process);
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(applicationInstaller).deployAndEnableProcess(any());

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).deployAndEnableProcess(any());

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

        applicationInstaller.install(applicationArchive);

        verify(applicationInstaller).pauseTenant();
        verify(applicationInstaller).updateBusinessDataModel(applicationArchive);
        verify(applicationInstaller).resumeTenant();

        //cleanup
        bdm.delete();
    }

    @Test
    public void should_fail_to_install_unresolved_process() throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(process);
        doThrow(new ProcessEnablementException("Process not resolved")).when(applicationInstaller)
                .deployAndEnableProcess(any());

        assertThatExceptionOfType(ProcessDeployException.class)
                .isThrownBy(() -> applicationInstaller.installProcesses(applicationArchive, executionResult))
                .withMessageContaining("Failed to enable process");

        verify(applicationInstaller).deployAndEnableProcess(any());

        //cleanup
        process.delete();
    }

    @Test
    public void should_throw_exception_if_already_existing_process() throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        ApplicationArchive applicationArchive = new ApplicationArchive()
                .addProcess(process);
        ProcessDefinition myProcess = aProcessDefinition(1193L);
        doThrow(new AlreadyExistsException("already exists"))
                .doReturn(myProcess)
                .when(applicationInstaller)
                .deployAndEnableProcess(any(BusinessArchive.class));

        // when
        assertThatExceptionOfType(ProcessDeployException.class).isThrownBy(
                () -> applicationInstaller.installProcesses(applicationArchive, executionResult))
                .withMessageContaining("Process myProcess - 1.0 already exists");

        verify(applicationInstaller).deployAndEnableProcess(ArgumentMatchers
                .argThat(b -> b.getProcessDefinition().getName().equals("myProcess")));

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
