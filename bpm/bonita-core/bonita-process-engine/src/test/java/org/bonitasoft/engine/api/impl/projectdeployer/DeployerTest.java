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
package org.bonitasoft.engine.api.impl.projectdeployer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.FileOperations.asInputStream;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.impl.projectdeployer.descriptor.DeploymentDescriptor;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Application;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Organization;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Page;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Profile;
import org.bonitasoft.engine.api.impl.projectdeployer.model.RestAPIExtension;
import org.bonitasoft.engine.api.impl.projectdeployer.policies.ApplicationImportPolicy;
import org.bonitasoft.engine.api.impl.projectdeployer.policies.OrganizationImportPolicy;
import org.bonitasoft.engine.api.impl.projectdeployer.policies.ProfileImportPolicy;
import org.bonitasoft.engine.api.impl.projectdeployer.validator.ArtifactValidator;
import org.bonitasoft.engine.api.impl.projectdeployer.validator.InvalidArtifactException;
import org.bonitasoft.engine.exception.DeployerException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeployerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ApplicationArchiveReader applicationArchiveReader = spy(new ApplicationArchiveReader(
            mock(ArtifactValidator.class)));

    @Mock
    private PageAPI pageAPI;

    @Mock
    private IdentityAPI identityAPI;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    @Captor
    private ArgumentCaptor<ProfileImportPolicy> profilePolicyArgumentCaptor;

    @Captor
    private ArgumentCaptor<ApplicationImportPolicy> applicationImportPolicyArgumentCaptor;

    @Captor
    private ArgumentCaptor<OrganizationImportPolicy> organizationImportPolicyArgumentCaptor;

    private Deployer deployer;

    @Before
    public void before() throws Exception {
        //                applicationArchiveFile = temporaryFolder.newFile();

        deployer = spy(new Deployer.DeployerBuilder()
                .pageAPI(pageAPI)
                .identityAPI(identityAPI)
                .applicationArchiveReader(applicationArchiveReader)
                .artifactValidator(mock(ArtifactValidator.class))
                .build());
        doReturn(null).when(deployer).getPage(anyString());
    }

    @Test
    public void should_deploy_application_containing_1_page() throws Exception {
        String deployJsonContent = "{\n" +
                " \"name\":\"LeaveRequest\",\n" +
                " \"version\":\"1.0.0-SNAPSHOT\",\n" +
                " \"description\":\"Description of foo is bar\",\n" +
                " \"targetVersion\":\"7.10.0\",\n" +
                " \"pages\":[\n" +
                "  {\n" +
                "   \"file\":\"pages/myCustomPage.zip\"\n" +
                "  }\n" +
                " ],\n" +
                " \"modelVersion\":\"0.1\"\n" +
                "}";
        final byte[] customPageZipFile = zip(
                file("page.properties", "name=custompage_test\ncontentType=page"));
        byte[] zip = zip(
                file("deploy.json", deployJsonContent),
                file("pages/myCustomPage.zip", asInputStream(customPageZipFile)));

        deployer.deploy(zip);

        verify(pageAPI).createPage("custompage_test", customPageZipFile);
    }

    @Test
    public void should_throw_a_wrapped_FileNotFound_if_page_is_not_present()
            throws IOException, InvalidArtifactException {
        // given
        ApplicationArchive applicationArchive = new ApplicationArchive();
        applicationArchive.setDeploymentDescriptor(new DeploymentDescriptor());
        doReturn(applicationArchive).when(applicationArchiveReader).read(any(byte[].class));
        applicationArchive.getDeploymentDescriptor().add(aPage("path_of_the_page"));

        // when
        Throwable thrown = catchThrowable(() -> deployer.deploy(new byte[] {}));

        // then
        assertThat(thrown).isInstanceOf(DeployerException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class)
                .hasMessageStartingWith(
                        "The Application Archive deploy operation has been aborted - cause: FileNotFoundException - ")
                .hasMessageEndingWith("path_of_the_page");
    }

    //    @Test
    //    public void should_deploy_profiles_of_archive() throws Exception {
    //        applicationArchive.getDeploymentDescriptor().add(aProfile("path_of_the_profile"));
    //        applicationArchive.addFile("path_of_the_profile", asInputStream("content of the profile xml"));
    //
    //        deployer.deploy(applicationArchiveFile);
    //
    //        verify(bonitaClient).importProfiles(fileCaptor.capture(), profilePolicyArgumentCaptor.capture());
    //        assertThat(fileCaptor.getValue()).hasName("path_of_the_profile");
    //        assertThat(profilePolicyArgumentCaptor.getValue()).isEqualTo(ProfileImportPolicy.REPLACE_DUPLICATES);
    //    }

    @Test
    public void should_deploy_organization() throws Exception {
        ApplicationArchive applicationArchive = new ApplicationArchive();
        applicationArchive.setDeploymentDescriptor(new DeploymentDescriptor());
        applicationArchive.getDeploymentDescriptor().setOrganization(anOrganization("path_to_organization"));
        applicationArchive.addFile("path_to_organization", asInputStream("organization content"));

        deployer.deployOrganization(applicationArchive);

        verify(identityAPI).importOrganizationWithWarnings("organization content", ImportPolicy.MERGE_DUPLICATES);
    }

    //    @Test
    //    public void should_deploy_restApi_extension() throws Exception {
    //        applicationArchive.getDeploymentDescriptor().add(aRestAPIExtension("path_to_restApiExtension"));
    //        applicationArchive.addFile("path_to_restApiExtension", asInputStream("restApiExtension content"));
    //
    //        deployer.deploy(applicationArchiveFile);
    //
    //        verify(bonitaClient).importPage(fileCaptor.capture());
    //        assertThat(fileCaptor.getValue()).hasName("path_to_restApiExtension");
    //    }
    //
    //    @Test
    //    public void should_deploy_artifacts_in_expected_order() throws Exception {
    //        applicationArchive.addFile("path_to_organization", asInputStream("organization content"));
    //        applicationArchive.addFile("path_of_the_profile", asInputStream("content of the profile xml"));
    //        applicationArchive.addFile("path_to_restApiExtension", asInputStream("restApiExtension content"));
    //        applicationArchive.addFile("theApplicationXmlPath", asInputStream("content of the xml file"));
    //        applicationArchive.addFile("path_of_the_page", asInputStream("content of the page zip"));
    //
    //        applicationArchive.setDeploymentDescriptor(
    //                DeploymentDescriptor.builder()
    //                        .profile(aProfile("path_of_the_profile"))
    //                        .restAPIExtension(aRestAPIExtension("path_to_restApiExtension"))
    //                        .application(anApplication("theApplicationXmlPath"))
    //                        .page(aPage("path_of_the_page"))
    //                        .organization(anOrganization("path_to_organization"))
    //                        .build());
    //
    //        deployer.deploy(applicationArchiveFile);
    //
    //        InOrder inOrder = inOrder(bonitaClient);
    //        inOrder.verify(bonitaClient).importOrganization(fileCaptor.capture(),
    //                organizationImportPolicyArgumentCaptor.capture());
    //        inOrder.verify(bonitaClient).importProfiles(fileCaptor.capture(), profilePolicyArgumentCaptor.capture());
    //        inOrder.verify(bonitaClient, times(2)).importPage(fileCaptor.capture());
    //        inOrder.verify(bonitaClient).importApplications(fileCaptor.capture(),
    //                applicationImportPolicyArgumentCaptor.capture());
    //
    //        assertThat(fileCaptor.getAllValues()).extracting(File::getName).containsExactlyInAnyOrder(
    //                "path_to_organization",
    //                "path_of_the_profile",
    //                "path_to_restApiExtension",
    //                "path_of_the_page",
    //                "theApplicationXmlPath");
    //    }
    //
    //    @Test
    //    public void should_import_single_api_extension_file() throws Exception {
    //        File restApiExtension = new File("");
    //        InOrder inOrder = inOrder(bonitaClient);
    //
    //        deployer.deployRestApiExtension(restApiExtension);
    //
    //        inOrder.verify(bonitaClient).login(USERNAME, PASSWORD);
    //        inOrder.verify(bonitaClient).importPage(eq(restApiExtension));
    //        inOrder.verify(bonitaClient).logout();
    //    }
    //
    //    @Test(expected = IOException.class)
    //    public void should_throw_exception_when_importing_single_api_extension_file() throws Exception {
    //        File restApiExtension = new File("");
    //        doThrow(new IOException()).when(bonitaClient).importPage(any());
    //
    //        deployer.deployRestApiExtension(restApiExtension);
    //    }
    //
    //    @Test
    //    public void should_import_single_page_file() throws Exception {
    //        File page = new File("");
    //        InOrder inOrder = inOrder(bonitaClient);
    //
    //        deployer.deployPage(page);
    //
    //        inOrder.verify(bonitaClient).login(USERNAME, PASSWORD);
    //        inOrder.verify(bonitaClient).importPage(eq(page));
    //        inOrder.verify(bonitaClient).logout();
    //    }
    //
    //    @Test
    //    public void should_import_single_profiles_file() throws Exception {
    //        File profiles = new File("");
    //        InOrder inOrder = inOrder(bonitaClient);
    //
    //        deployer.deployProfiles(profiles, ProfileImportPolicy.REPLACE_DUPLICATES);
    //
    //        inOrder.verify(bonitaClient).login(USERNAME, PASSWORD);
    //        inOrder.verify(bonitaClient).importProfiles(profiles, ProfileImportPolicy.REPLACE_DUPLICATES);
    //        inOrder.verify(bonitaClient).logout();
    //    }
    //
    //    @Test
    //    public void should_import_single_organization_file() throws Exception {
    //        File organization = new File("");
    //        InOrder inOrder = inOrder(bonitaClient);
    //
    //        deployer.deployOrganization(organization, OrganizationImportPolicy.MERGE_DUPLICATES);
    //
    //        inOrder.verify(bonitaClient).login(USERNAME, PASSWORD);
    //        inOrder.verify(bonitaClient).importOrganization(organization, OrganizationImportPolicy.MERGE_DUPLICATES);
    //        inOrder.verify(bonitaClient).logout();
    //    }
    //
    //    @Test
    //    public void should_import_single_applications_file() throws Exception {
    //        File applications = new File("");
    //        InOrder inOrder = inOrder(bonitaClient);
    //
    //        deployer.deployApplications(applications, ApplicationImportPolicy.REPLACE_DUPLICATES);
    //
    //        inOrder.verify(bonitaClient).login(USERNAME, PASSWORD);
    //        inOrder.verify(bonitaClient).importApplications(applications, ApplicationImportPolicy.REPLACE_DUPLICATES);
    //        inOrder.verify(bonitaClient).logout();
    //    }
    //
    //    @Test
    //    public void should_deploy_application_configuration() throws IOException {
    //        //given:
    //        Deployer spyDeployer = spy(deployer);
    //        final File applicationConfigurationFile = temporaryFolder.newFile("application.bconf");
    //        ApplicationDeployment applicationDeployment = ApplicationDeployment.builder()
    //                .applicationArchiveFile(null)
    //                .applicationConfigurationFile(applicationConfigurationFile)
    //                .build();
    //
    //        //when:
    //        spyDeployer.deploy(applicationDeployment);
    //
    //        //then:
    //        verify(spyDeployer).doBonitaConfigurationDeployment(applicationConfigurationFile);
    //    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static Organization anOrganization(String path) {
        Organization organization = new Organization();
        organization.setFile(path);
        return organization;
    }

    private static Profile aProfile(String path) {
        Profile profile = new Profile();
        profile.setFile(path);
        return profile;
    }

    private static Page aPage(String path) {
        Page page = new Page();
        page.setFile(path);
        return page;
    }

    private static RestAPIExtension aRestAPIExtension(String path) {
        RestAPIExtension restAPIExtension = new RestAPIExtension();
        restAPIExtension.setFile(path);
        return restAPIExtension;
    }

    private static Application anApplication(String path) {
        Application application = new Application();
        application.setFile(path);
        return application;
    }

}
