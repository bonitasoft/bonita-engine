/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.business.application.ApplicationImportPolicy.REPLACE_DUPLICATES;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.IOUtils.createTempFile;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.assertj.core.util.Lists;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.ProcessDeploymentAPIDelegate;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.business.application.exporter.ApplicationNodeContainerConverter;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder.ApplicationBuilder;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.identity.ImportPolicy;
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
import org.xml.sax.SAXException;

/**
 * @author Danila Mazour
 * @author Haroun El Alami
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationInstallerUpdateTest {

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

        // trigger update behaviour
        Page samplePage = spy(Page.class);
        doReturn(1L).when(samplePage).getId();

        doReturn(samplePage).when(applicationInstaller).getPageIfExist(anyString());
        doNothing().when(applicationInstaller).updatePageContent(any(), eq(1L));

        // bypass update version
        doNothing().when(applicationInstaller).updateApplicationVersion("1.0.1");
        doNothing().when(applicationInstaller).pauseTenantInSession();
        doNothing().when(applicationInstaller).resumeTenantInSession();
    }

    @Test
    public void should_update_application_containing_all_kind_of_custom_pages() throws Exception {
        File page = createTempFile("page", "zip", zip(file("page.properties", "name=MyPage\ncontentType=page")));
        File layout = createTempFile("layout", "zip",
                zip(file("page.properties", "name=MyLayout\ncontentType=layout")));
        File theme = createTempFile("theme", "zip", zip(file("page.properties", "name=MyTheme\ncontentType=theme")));
        File restAPI = createTempFile("restApiExtension", "zip",
                zip(file("page.properties", "name=MyApi\ncontentType=apiExtension")));
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addPage(page)
                    .addLayout(layout)
                    .addTheme(theme)
                    .addRestAPIExtension(restAPI);
            doNothing().when(applicationInstaller).disableOldProcesses(any(), any());
            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());

            applicationInstaller.update(applicationArchive, "1.0.1");
        }
        var captor = ArgumentCaptor.forClass(File.class);
        verify(applicationInstaller, times(4)).updatePageContent(captor.capture(), eq(1L));

        assertThat(
                captor.getAllValues().stream().allMatch(file -> Stream.of("page", "layout", "theme", "restApiExtension")
                        .anyMatch(name -> file.getName().startsWith(name)))).isTrue();
    }

    @Test
    public void should_update_application_containing_living_applications() throws Exception {
        File application = createTempFile("application", "xml",
                applicationContent(new ApplicationBuilder("myApp", "My App", "1.0").create()));
        doReturn(new ImportStatus("application")).when(applicationInstaller).importApplication(any(), any(),
                eq(REPLACE_DUPLICATES));
        doNothing().when(applicationInstaller).disableOldProcesses(any(), any());
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addApplication(application);
            applicationInstaller.update(applicationArchive, "1.0.1");
        }

        verify(applicationInstaller).importApplication(any(), any(), eq(REPLACE_DUPLICATES));
    }

    private byte[] applicationContent(ApplicationNode application) throws JAXBException, IOException, SAXException {
        var container = new ApplicationNodeContainer();
        container.addApplication(application);
        return new ApplicationNodeContainerConverter().marshallToXML(container);
    }

    @Test
    public void should_install_and_enable_resolved_process() throws Exception {
        // given
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        doReturn(Optional.empty()).when(applicationInstaller).getDeployedProcessId("myProcess", "1.0");
        long newProcessId = 123L;
        doReturn(Lists.newArrayList(newProcessId)).when(applicationInstaller).getDeployedProcessIds();

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();

        final ProcessDeploymentInfo info = mock(ProcessDeploymentInfo.class);
        doReturn(newProcessId).when(info).getProcessId();
        doReturn(ConfigurationState.RESOLVED).when(info).getConfigurationState();
        doReturn(ActivationState.DISABLED).when(info).getActivationState();
        doReturn(Map.of(newProcessId, info)).when(processDeploymentAPIDelegate)
                .getProcessDeploymentInfosFromIds(List.of(newProcessId));

        ProcessDefinition myProcess = aProcessDefinition(newProcessId);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        // when
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.update(applicationArchive, "1.0.1");
        }
        // then
        verify(applicationInstaller).deployProcess(
                ArgumentMatchers.argThat(b -> b.getProcessDefinition().getName().equals("myProcess")
                        && b.getProcessDefinition().getVersion().equals("1.0")),
                any());
        verify(processDeploymentAPIDelegate).enableProcess(newProcessId);
    }

    @Test
    public void should_not_install_process_if_process_with_same_version_exists() throws Exception {
        // given
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);

        doReturn(Optional.of(1L)).when(applicationInstaller).getDeployedProcessId(anyString(), eq("1.0"));
        doReturn(Lists.newArrayList(1L)).when(applicationInstaller).getDeployedProcessIds();

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();

        // when
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.update(applicationArchive, "1.0.1");
        }
        // then
        verify(applicationInstaller, never()).deployProcess(any(), any());
        verify(applicationInstaller, never()).disableProcess(anyLong());
    }

    @Test
    public void should_install_and_enable_resolved_process_and_disable_previous_version_existing() throws Exception {
        // given
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);

        doReturn(Lists.newArrayList(1L, 2L, 123L)).when(applicationInstaller).getDeployedProcessIds();
        doNothing().when(applicationInstaller).disableProcess(anyLong());

        ProcessDeploymentInfo deployedProcess1 = spy(ProcessDeploymentInfo.class);
        doReturn("1.1").when(deployedProcess1).getVersion();
        doReturn(ActivationState.ENABLED).when(deployedProcess1).getActivationState();

        ProcessDeploymentInfo deployedProcess2 = spy(ProcessDeploymentInfo.class);
        doReturn("1.2").when(deployedProcess2).getVersion();
        doReturn(ActivationState.ENABLED).when(deployedProcess2).getActivationState();

        doReturn(deployedProcess1).when(applicationInstaller).getProcessDeploymentInfo(1L);
        doReturn(deployedProcess2).when(applicationInstaller).getProcessDeploymentInfo(2L);

        doReturn(Optional.empty()).when(applicationInstaller)
                .getDeployedProcessId(anyString(), any());

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();

        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        // when
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.update(applicationArchive, "1.0.1");
        }
        // then
        verify(applicationInstaller).deployProcess(any(), any());
        verify(applicationInstaller, times(2)).disableProcess(anyLong());
        verify(applicationInstaller, times(1)).enableResolvedProcesses(eq(List.of(myProcess.getId())), any());
    }

    @Test
    public void should_update_bdm() throws Exception {
        byte[] bdmZipContent = createValidBDMZipFile();
        File bdm = createTempFile("bdm", "zip", bdmZipContent);
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setBdm(bdm);
            doReturn("1.0").when(applicationInstaller).updateBusinessDataModel(applicationArchive);
            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
            doNothing().when(applicationInstaller).disableOldProcesses(any(), any());

            applicationInstaller.update(applicationArchive, "1.0.1");

            verify(applicationInstaller).updateBusinessDataModel(applicationArchive);
        }
    }

    @Test
    public void should_call_enable_resolved_processes() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);

        doReturn(Lists.newArrayList(123L)).when(applicationInstaller).getDeployedProcessIds();
        doReturn(Optional.empty()).when(applicationInstaller).getDeployedProcessId(anyString(), eq("1.0"));

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();

        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.update(applicationArchive, "1.0.1");
        }

        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(applicationInstaller).enableResolvedProcesses(eq(Collections.singletonList(123L)), any());
    }

    @Test
    public void should_throw_exception_if_application_archive_is_empty() throws Exception {
        try (ApplicationArchive applicationArchive = new ApplicationArchive()) {
            assertThatExceptionOfType(ApplicationInstallationException.class)
                    .isThrownBy(() -> applicationInstaller.update(applicationArchive, "1.0.1"))
                    .withMessage("The Application Archive contains no valid artifact to install");
        }
    }

    @Test
    public void should_update_organisation() throws Exception {
        File organization = createTempFile("org", "xml", "content".getBytes());
        doReturn(emptyList()).when(applicationInstaller).importOrganization(any(), any());
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        doNothing().when(applicationInstaller).disableOldProcesses(any(), any());
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setOrganization(organization);
            applicationInstaller.update(applicationArchive, "1.0.1");
        }

        verify(applicationInstaller).importOrganization(organization, ImportPolicy.IGNORE_DUPLICATES);
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
