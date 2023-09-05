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
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.engine.api.result.Status.Level.ERROR;
import static org.bonitasoft.engine.api.result.Status.Level.INFO;
import static org.bonitasoft.engine.api.result.Status.Level.WARNING;
import static org.bonitasoft.engine.api.result.StatusCode.LIVING_APP_REFERENCES_UNKNOWN_PAGE;
import static org.bonitasoft.engine.api.result.StatusCode.PROCESS_DEPLOYMENT_ENABLEMENT_KO;
import static org.bonitasoft.engine.api.result.StatusCode.PROCESS_DEPLOYMENT_SKIP_INSTALL;
import static org.bonitasoft.engine.api.result.StatusContext.PROCESS_NAME_KEY;
import static org.bonitasoft.engine.api.result.StatusContext.PROCESS_VERSION_KEY;
import static org.bonitasoft.engine.business.application.ApplicationImportPolicy.FAIL_ON_DUPLICATES;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.IOUtils.createTempFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.ProcessDeploymentAPIDelegate;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
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
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.service.InstallationService;
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
import org.xml.sax.SAXException;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationInstallerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private UserTransactionService transactionService;

    @Mock
    private InstallationService installationService;

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

        // bypass update version
        doNothing().when(applicationInstaller).updateApplicationVersion("1.0.0");
    }

    @Test
    public void should_install_application_containing_all_kind_of_custom_pages() throws Exception {
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

            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
            doNothing().when(applicationInstaller).installOrganization(any(), any());
            doReturn(mock(Page.class)).when(applicationInstaller).createPage(any(), any());
            doReturn(null).when(applicationInstaller).getPageIfExist(any());

            applicationInstaller.install(applicationArchive, "1.0.0");
        }
        var captor = ArgumentCaptor.forClass(Properties.class);
        verify(applicationInstaller, times(4)).createPage(any(), captor.capture());

        assertThat(captor.getAllValues()).extracting("name", "contentType").contains(tuple("MyPage", "page"),
                tuple("MyLayout", "layout"),
                tuple("MyTheme", "theme"),
                tuple("MyApi", "apiExtension"));
    }

    @Test
    public void should_install_application_containing_living_applications() throws Exception {
        File application = createTempFile("application", "xml", "content".getBytes());
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(emptyList()).when(applicationInstaller).importApplications(any(), eq(FAIL_ON_DUPLICATES));
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addApplication(application);
            applicationInstaller.install(applicationArchive, "1.0.0");
        }

        verify(applicationInstaller).importApplications("content".getBytes(), FAIL_ON_DUPLICATES);
    }

    @Test
    public void should_not_install_living_applications_if_page_missing() throws Exception {

        final ExecutionResult result = new ExecutionResult();
        ImportStatus importStatus = new ImportStatus("application");
        importStatus.addError(new ImportError("page", ImportError.Type.PAGE));
        ImportStatus importStatus2 = new ImportStatus("application");
        importStatus2.addError(new ImportError("test", ImportError.Type.PAGE));
        List<ImportStatus> importStatuses = Arrays.asList(importStatus, importStatus2);
        File application = createTempFile("application", "xml", "content".getBytes());
        doReturn(importStatuses).when(applicationInstaller).importApplications(any(), eq(FAIL_ON_DUPLICATES));

        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addApplication(application);
            assertThatExceptionOfType(ApplicationInstallationException.class)
                    .isThrownBy(() -> applicationInstaller.installLivingApplications(applicationArchive, result,
                            FAIL_ON_DUPLICATES))
                    .withMessage("At least one application failed to be installed. Canceling installation.");
        }
        verify(applicationInstaller).importApplications("content".getBytes(), FAIL_ON_DUPLICATES);

        assertThat(result.getAllStatus()).hasSize(3).extracting("code")
                .containsOnly(LIVING_APP_REFERENCES_UNKNOWN_PAGE);
        assertThat(result.getAllStatus()).extracting("message")
                .containsExactly("Unknown PAGE named 'page'", "Unknown PAGE named 'test'",
                        ApplicationInstaller.WARNING_MISSING_PAGE_MESSAGE);
        assertThat(result.getAllStatus()).extracting("level")
                .containsExactly(ERROR, ERROR, WARNING);
    }

    @Test
    public void should_install_and_enable_resolved_process() throws Exception {
        // given
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        doNothing().when(applicationInstaller).installOrganization(any(), any());

        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);
        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        doReturn(Optional.empty()).when(applicationInstaller).getDeployedProcessId(any(), eq("1.0"));

        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        // when
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.install(applicationArchive, "1.0.0");
        }
        // then
        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(processDeploymentAPIDelegate, never()).enableProcess(123L);

    }

    @Test
    public void should_install_bdm() throws Exception {
        byte[] bdmZipContent = createValidBDMZipFile();
        File bdm = createTempFile("bdm", "zip", bdmZipContent);
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setBdm(bdm);
            doNothing().when(applicationInstaller).installOrganization(any(), any());
            doReturn("1.0").when(applicationInstaller).updateBusinessDataModel(applicationArchive);
            doReturn(Collections.emptyList()).when(applicationInstaller).installProcesses(any(), any());
            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());

            applicationInstaller.install(applicationArchive, "1.0.0");

            verify(applicationInstaller).updateBusinessDataModel(applicationArchive);
        }

    }

    @Test
    public void should_call_enable_resolved_processes() throws Exception {
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);

        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        doReturn(Optional.empty()).when(applicationInstaller).getDeployedProcessId(any(), eq("1.0"));

        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.install(applicationArchive, "1.0.0");
        }

        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(applicationInstaller).enableResolvedProcesses(eq(Collections.singletonList(123L)), any());
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
    public void should_skip_install_process_if_already_existing() throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive();
        File process = createTempFile("process", "bar", barContent);
        doReturn(Optional.of(1L)).when(applicationInstaller).getDeployedProcessId("myProcess", "1.0");

        // when
        try (ApplicationArchive applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationInstaller.installProcesses(applicationArchive, executionResult);
        }

        // verify executionResult
        assertThat(executionResult.hasInfo()).isTrue();
        assertThat(executionResult.getAllStatus().get(0))
                .returns(INFO, from(Status::getLevel))
                .returns(PROCESS_DEPLOYMENT_SKIP_INSTALL, from(Status::getCode))
                .returns("myProcess", from(status -> status.getContext().get(PROCESS_NAME_KEY)))
                .returns("1.0", from(status -> status.getContext().get(PROCESS_VERSION_KEY)));

        // verify deploy never called
        verify(applicationInstaller, never()).deployProcess(
                ArgumentMatchers.argThat(b -> b.getProcessDefinition().getName().equals("myProcess")
                        && b.getProcessDefinition().getVersion().equals("1.0")),
                any());
    }

    @Test
    public void should_throw_exception_if_application_archive_is_empty() throws Exception {
        try (ApplicationArchive applicationArchive = new ApplicationArchive()) {
            assertThatExceptionOfType(ApplicationInstallationException.class)
                    .isThrownBy(() -> applicationInstaller.install(applicationArchive, "1.0.0"))
                    .withMessage("The Application Archive contains no valid artifact to install");
        }
    }

    @Test
    public void should_install_organization() throws Exception {
        File organization = createTempFile("org", "xml", "content".getBytes());
        doReturn(emptyList()).when(applicationInstaller).importOrganization(any(), any());
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setOrganization(organization);
            applicationInstaller.install(applicationArchive, "1.0.0");
        }

        verify(applicationInstaller).importOrganization(organization, ImportPolicy.FAIL_ON_DUPLICATES);
    }

    @Test
    public void should_not_fail_when_no_organization() throws Exception {
        var executionResult = new ExecutionResult();
        try (var applicationArchive = new ApplicationArchive()) {
            applicationInstaller.installOrganization(applicationArchive, executionResult);
        }

        assertThat(executionResult.getInfo()).extracting("message")
                .containsOnly("No organization found. Use the technical user to configure the organization.");
    }

    @Test
    public void install_should_call_install_configuration_file_on_installation_service() throws Exception {
        // given:
        final ExecutionResult executionResult = new ExecutionResult();
        final Optional<InputStream> inputStream = Optional.of(mock(InputStream.class));
        doNothing().when(installationService).install(eq(null), any());

        // when:
        applicationInstaller.installConfigurationFileIfPresent(inputStream, executionResult);

        // then:
        verify(installationService).install(eq(null), any());
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

    private byte[] createValidBDMZipFile() throws IOException, JAXBException, SAXException {
        BusinessObjectModel bom = new BusinessObjectModel();
        BusinessObject businessObject = new BusinessObject("org.bonita.Employee");
        SimpleField field = new SimpleField();
        field.setName("name");
        field.setType(FieldType.STRING);
        businessObject.addField(field);
        bom.addBusinessObject(businessObject);
        return new BusinessObjectModelConverter().marshall(bom);
    }

}
