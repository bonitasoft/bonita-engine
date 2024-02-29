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
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.api.result.Status.Level.ERROR;
import static org.bonitasoft.engine.api.result.Status.Level.INFO;
import static org.bonitasoft.engine.api.result.StatusCode.*;
import static org.bonitasoft.engine.api.result.StatusContext.PROCESS_NAME_KEY;
import static org.bonitasoft.engine.api.result.StatusContext.PROCESS_VERSION_KEY;
import static org.bonitasoft.engine.business.application.ApplicationImportPolicy.FAIL_ON_DUPLICATES;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.IOUtils.createTempFile;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.business.application.exporter.ApplicationNodeContainerConverter;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder.ApplicationBuilder;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.service.InstallationService;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.xml.sax.SAXException;

/**
 * @author Baptiste Mesta.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationInstallerTest {

    @Mock
    private UserTransactionService transactionService;

    @Mock
    private InstallationService installationService;

    @Mock
    private TenantStateManager tenantStateManager;

    @Captor
    ArgumentCaptor<Callable<Object>> callableCaptor;

    @InjectMocks
    @Spy
    private ApplicationInstaller applicationInstaller;

    @BeforeEach
    void before() throws Exception {
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
    void should_install_application_containing_all_kind_of_custom_pages() throws Exception {
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
            applicationArchive.setVersion("1.0.0");

            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
            doNothing().when(applicationInstaller).installOrganization(any(), any());
            doReturn(mock(Page.class)).when(applicationInstaller).createPage(any(), any());
            doReturn(null).when(applicationInstaller).getPageIfExist(any());

            applicationInstaller.install(applicationArchive);
        }
        var captor = ArgumentCaptor.forClass(Properties.class);
        verify(applicationInstaller, times(4)).createPage(any(), captor.capture());

        assertThat(captor.getAllValues()).extracting("name", "contentType").contains(tuple("MyPage", "page"),
                tuple("MyLayout", "layout"),
                tuple("MyTheme", "theme"),
                tuple("MyApi", "apiExtension"));
    }

    @Test
    void should_install_application_containing_living_applications() throws Exception {
        File application = createTempFile("application", "xml",
                applicationContent(new ApplicationBuilder("myApp", "My App", "1.0").create()));
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        doReturn(new ImportStatus("application")).when(applicationInstaller).importApplication(any(), any(),
                eq(FAIL_ON_DUPLICATES));
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addApplication(application);
            applicationArchive.setVersion("1.0.0");
            applicationInstaller.install(applicationArchive);
        }

        verify(applicationInstaller).importApplication(any(), any(), eq(FAIL_ON_DUPLICATES));
    }

    @Test
    void should_not_install_living_applications_if_page_missing() throws Exception {

        final ExecutionResult result = new ExecutionResult();
        ImportStatus importStatus = new ImportStatus("application");
        importStatus.addError(new ImportError("page", ImportError.Type.PAGE));
        importStatus.addError(new ImportError("test", ImportError.Type.PAGE));
        File applicationFile = createTempFile("application", "xml",
                applicationContent(new ApplicationBuilder("myApp", "My App", "1.0").create()));

        doReturn(importStatus).when(applicationInstaller).importApplication(any(), any(), eq(FAIL_ON_DUPLICATES));

        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addApplication(applicationFile);
            assertThatExceptionOfType(ApplicationInstallationException.class)
                    .isThrownBy(() -> applicationInstaller.installLivingApplications(applicationArchive, result,
                            FAIL_ON_DUPLICATES))
                    .withMessage("At least one application failed to be installed. Canceling installation.");
        }
        verify(applicationInstaller).importApplication(any(), any(), eq(FAIL_ON_DUPLICATES));

        assertThat(result.getAllStatus()).hasSize(2).extracting("code")
                .containsOnly(LIVING_APP_REFERENCES_UNKNOWN_PAGE);
        assertThat(result.getAllStatus()).extracting("message")
                .containsExactly("Unknown PAGE named 'page'", "Unknown PAGE named 'test'");
        assertThat(result.getAllStatus()).extracting("level")
                .containsExactly(ERROR, ERROR);
    }

    private byte[] applicationContent(ApplicationNode application) throws JAXBException, IOException, SAXException {
        var container = new ApplicationNodeContainer();
        container.addApplication(application);
        return new ApplicationNodeContainerConverter().marshallToXML(container);
    }

    @Test
    void should_install_and_enable_resolved_process(@TempDir Path tmpFolder) throws Exception {
        // given
        byte[] barContent = createValidBusinessArchive(tmpFolder);
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
            applicationArchive.setVersion("1.0.0");
            applicationInstaller.install(applicationArchive);
        }
        // then
        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(processDeploymentAPIDelegate, never()).enableProcess(123L);

    }

    @Test
    void should_install_bdm_in_maintenance_mode() throws Exception {
        byte[] bdmZipContent = createValidBDMZipFile();
        File bdm = createTempFile("bdm", "zip", bdmZipContent);
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setBdm(bdm);
            doNothing().when(applicationInstaller).installOrganization(any(), any());
            doReturn("1.0").when(applicationInstaller).updateBusinessDataModel(applicationArchive);
            doReturn(false).when(applicationInstaller).sameBdmContentDeployed(any());
            doReturn(Collections.emptyList()).when(applicationInstaller).installProcesses(any(), any());
            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
            doNothing().when(applicationInstaller).pauseTenantInSession();
            doNothing().when(applicationInstaller).resumeTenantInSession();
            applicationArchive.setVersion("1.0.0");
            applicationInstaller.install(applicationArchive);

            verify(applicationInstaller).pauseTenantInSession();
            verify(applicationInstaller).updateBusinessDataModel(applicationArchive);
            verify(applicationInstaller).resumeTenantInSession();
        }

    }

    @Test
    void should_keep_maintenance_mode_when_bdm_update_fails() throws Exception {
        byte[] bdmZipContent = createValidBDMZipFile();
        File bdm = createTempFile("bdm", "zip", bdmZipContent);
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setBdm(bdm);
            doNothing().when(applicationInstaller).installOrganization(any(), any());
            doReturn("1.0").when(applicationInstaller).updateBusinessDataModel(applicationArchive);
            doReturn(false).when(applicationInstaller).sameBdmContentDeployed(any());
            doReturn(Collections.emptyList()).when(applicationInstaller).installProcesses(any(), any());
            doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
            doNothing().when(applicationInstaller).pauseTenantInSession();
            doNothing().when(applicationInstaller).resumeTenantInSession();
            applicationArchive.setVersion("1.0.0");
            doThrow(BusinessDataRepositoryDeploymentException.class).when(applicationInstaller)
                    .updateBusinessDataModel(any());

            Assertions.assertThrows(ApplicationInstallationException.class,
                    () -> applicationInstaller.install(applicationArchive));

            verify(applicationInstaller).pauseTenantInSession();
            verify(applicationInstaller).updateBusinessDataModel(applicationArchive);
            verify(applicationInstaller, never()).resumeTenantInSession();
        }

    }

    @Test
    void should_call_enable_resolved_processes(@TempDir Path tmpFolder) throws Exception {
        byte[] barContent = createValidBusinessArchive(tmpFolder);
        File process = createTempFile("process", "bar", barContent);
        doNothing().when(applicationInstaller).installOrganization(any(), any());
        ProcessDeploymentAPIDelegate processDeploymentAPIDelegate = mock(ProcessDeploymentAPIDelegate.class);

        doReturn(processDeploymentAPIDelegate).when(applicationInstaller).getProcessDeploymentAPIDelegate();
        doReturn(Optional.empty()).when(applicationInstaller).getDeployedProcessId(any(), eq("1.0"));

        ProcessDefinition myProcess = aProcessDefinition(123L);
        doReturn(myProcess).when(processDeploymentAPIDelegate).deploy(any());

        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.addProcess(process);
            applicationArchive.setVersion("1.0.0");
            applicationInstaller.install(applicationArchive);
        }

        verify(applicationInstaller).deployProcess(any(), any());
        verify(processDeploymentAPIDelegate).deploy(any());
        verify(applicationInstaller).enableResolvedProcesses(eq(Collections.singletonList(123L)), any());
    }

    @Test
    void enableResolvedProcesses_should_enable_processes_resolved_and_not_already_enabled() throws Exception {
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
    void enableResolvedProcesses_should_throw_exception_on_unresolved_process() throws Exception {
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
    void should_skip_install_process_if_already_existing(@TempDir Path tmpFolder) throws Exception {
        final ExecutionResult executionResult = new ExecutionResult();
        byte[] barContent = createValidBusinessArchive(tmpFolder);
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
    void should_throw_exception_if_application_archive_is_empty() throws Exception {
        try (ApplicationArchive applicationArchive = new ApplicationArchive()) {
            applicationArchive.setVersion("1.0.0");
            assertThatExceptionOfType(ApplicationInstallationException.class)
                    .isThrownBy(() -> applicationInstaller.install(applicationArchive))
                    .withMessage("The Application Archive contains no valid artifact to install");
        }
    }

    @Test
    void should_install_organization() throws Exception {
        File organization = createTempFile("org", "xml", "content".getBytes());
        doReturn(emptyList()).when(applicationInstaller).importOrganization(any(), any());
        doNothing().when(applicationInstaller).enableResolvedProcesses(any(), any());
        try (var applicationArchive = new ApplicationArchive()) {
            applicationArchive.setOrganization(organization);
            applicationArchive.setVersion("1.0.0");
            applicationInstaller.install(applicationArchive);
        }

        verify(applicationInstaller).importOrganization(organization, ImportPolicy.FAIL_ON_DUPLICATES);
    }

    @Test
    void should_not_fail_when_no_organization() throws Exception {
        var executionResult = new ExecutionResult();
        try (var applicationArchive = new ApplicationArchive()) {
            applicationInstaller.installOrganization(applicationArchive, executionResult);
        }

        assertThat(executionResult.getInfo()).extracting("message")
                .containsOnly("No organization found. Use the technical user to configure the organization.");
    }

    @Test
    void install_should_call_install_configuration_file_on_installation_service() throws Exception {
        // given:
        final ExecutionResult executionResult = new ExecutionResult();
        doNothing().when(installationService).install(eq(null), any());

        // when:
        applicationInstaller.installConfiguration(
                new File(ApplicationInstaller.class.getResource("/RequestLoan_conf_with_null_params.bconf").getFile()),
                executionResult);

        // then:
        verify(installationService).install(eq(null), any());
    }

    private ProcessDefinitionImpl aProcessDefinition(long id) {
        ProcessDefinitionImpl myProcess = new ProcessDefinitionImpl("myProcess", "1.0");
        myProcess.setId(id);
        return myProcess;
    }

    private byte[] createValidBusinessArchive(Path tmpFolder)
            throws InvalidBusinessArchiveFormatException, InvalidProcessDefinitionException, IOException {
        BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0").done())
                .done();
        Path businessArchiveFile = Files.createFile(tmpFolder.resolve("tmpBar.bar"));
        assert businessArchiveFile.toFile().delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, businessArchiveFile.toFile());
        return Files.readAllBytes(businessArchiveFile);
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
