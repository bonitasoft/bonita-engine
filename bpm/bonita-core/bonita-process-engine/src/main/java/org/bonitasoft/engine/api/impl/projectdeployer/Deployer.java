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

import static org.bonitasoft.engine.identity.ImportPolicy.fromName;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.projectdeployer.model.BusinessDataModel;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Organization;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Page;
import org.bonitasoft.engine.api.impl.projectdeployer.model.Process;
import org.bonitasoft.engine.api.impl.projectdeployer.validator.ArtifactValidator;
import org.bonitasoft.engine.api.impl.projectdeployer.validator.InvalidArtifactException;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.DeployerException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.tenant.TenantResourceState;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point to deploy an {@link ApplicationArchive}.
 *
 * @author Baptiste Mesta.
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Deployer {

    private final ApplicationArchiveReader applicationArchiveReader;
    private final ArtifactValidator artifactValidator;
    private final PageAPI pageAPI;
    private final IdentityAPI identityAPI;
    private final TenantAdministrationAPI tenantAdministrationAPI;
    private final ProcessAPI processAPI;

    public void deploy(byte[] applicationArchiveFile) throws DeployerException {
        try (ApplicationArchive applicationArchive = applicationArchiveReader.read(applicationArchiveFile)) {
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive deployment...");
            //            inSession(() -> {
            deployOrganization(applicationArchive);
            //                deployProfiles(applicationArchive);
            deployBDM(applicationArchive);
            //                deployBdmAccessControl(applicationArchive);
            //                deployRestApiExtensions(applicationArchive, restApiConfigurationProperties);
            deployPages(applicationArchive);
            //                deployLayouts(applicationArchive);
            //                deployThemes(applicationArchive);
            //                deployApplications(applicationArchive);
            deployProcesses(applicationArchive);

            log.info("The Application Archive has been deployed successfully in {} ms.",
                    (System.currentTimeMillis() - startPoint));
        } catch (Exception e) {
            throw new DeployerException("The Application Archive deploy operation has been aborted", e);
        }
    }

    //    private void inSession(ClientInteraction clientInteraction) throws ClientException, IOException {
    //        bonitaClient.login(username, password);
    //        try {
    //            clientInteraction.apply();
    //        } finally {
    //            bonitaClient.logout();
    //        }
    //    }
    //
    //    private <T> T inSession(ClientInteractionWithReturn<T> clientInteraction) throws ClientException, IOException {
    //        bonitaClient.login(username, password);
    //        try {
    //            return clientInteraction.apply();
    //        } finally {
    //            bonitaClient.logout();
    //        }
    //    }
    //
    //    public void deployRestApiExtension(File restApiExtension)
    //            throws ClientException, IOException, InvalidArtifactException {
    //        artifactValidator.validateRestApiExtensionType(restApiExtension);
    //        inSession(() -> bonitaClient.importPage(restApiExtension));
    //    }
    //
    //    public void deployPage(File page) throws ClientException, IOException, InvalidArtifactException {
    //        artifactValidator.validatePageType(page);
    //        inSession(() -> bonitaClient.importPage(page));
    //    }
    //
    //    public void deployProfiles(File profiles, ProfileImportPolicy policy)
    //            throws ClientException, IOException, InvalidArtifactException {
    //        artifactValidator.validateProfileType(profiles);
    //        inSession(() -> bonitaClient.importProfiles(profiles, policy));
    //    }
    //
    //    public void deployApplications(File applications, ApplicationImportPolicy policy)
    //            throws ClientException, IOException, InvalidArtifactException {
    //        artifactValidator.validateApplicationType(applications);
    //        inSession(() -> bonitaClient.importApplications(applications, policy));
    //    }
    //
    //    private void deployRestApiExtensions(ApplicationArchive applicationArchive,
    //            Properties configurationProperties)
    //            throws IOException, ClientException {
    //        for (RestAPIExtension restAPIExtension : applicationArchive.getDeploymentDescriptor().getRestAPIExtensions()) {
    //            File file = artifactConfigurator.reconfigure(applicationArchive.getFile(restAPIExtension),
    //                    restAPIExtension.getFilesToReconfigure(), configurationProperties);
    //            bonitaClient.importPage(file);
    //        }
    //    }
    //
    //    private void deployOrganization(ApplicationArchive applicationArchive) throws IOException, ClientException {
    //        Organization organization = applicationArchive.getDeploymentDescriptor().getOrganization();
    //        if (organization != null) {
    //            bonitaClient.importOrganization(applicationArchive.getFile(organization), organization.getPolicy());
    //        }
    //    }
    //
    //    private void deployProfiles(ApplicationArchive applicationArchive) throws IOException, ClientException {
    //        for (Profile profile : applicationArchive.getDeploymentDescriptor().getProfiles()) {
    //            bonitaClient.importProfiles(applicationArchive.getFile(profile), profile.getPolicy());
    //        }
    //    }
    //

    void deployOrganization(ApplicationArchive applicationArchive)
            throws OrganizationImportException, IOException, InvalidArtifactException {
        Organization organization = applicationArchive.getDeploymentDescriptor().getOrganization();
        if (organization != null) {
            File applicationArchiveFile = applicationArchive.getFile(organization);
            artifactValidator.validateOrganizationType(applicationArchiveFile);
            String organizationContent = FileOperations.read(applicationArchiveFile);
            log.info("Deploying organization ...");
            identityAPI.importOrganizationWithWarnings(organizationContent, fromName(organization.getPolicy().name()));
        } else {
            log.warn("There is no organisation file in the archive");
        }
    }

    private void deployPages(ApplicationArchive applicationArchive)
            throws IOException, SearchException, UpdateException, CreationException, InvalidArtifactException {
        for (Page page : applicationArchive.getDeploymentDescriptor().getPages()) {
            final File pageFile = applicationArchive.getFile(page);
            artifactValidator.validatePageType(pageFile);
            final byte[] pageContent = Files.readAllBytes(pageFile.toPath());
            String pageToken = getPageToken(pageFile);
            org.bonitasoft.engine.page.Page existingPage = getPage(pageToken);
            if (existingPage != null) {
                //page already exists, we update it
                log.debug("Updating existing page...");
                pageAPI.updatePageContent(existingPage.getId(), pageContent);
            } else {
                //page do not exists, we create it
                log.debug("Creating new page...");
                pageAPI.createPage(pageToken, pageContent);
            }
        }
    }

    private String getPageToken(File content) throws IOException {
        byte[] pageProperties = FileOperations.getFileFromZip(content, "page.properties");
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(pageProperties));
        String name = properties.getProperty("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(String.format("Invalid page %s, page.properties do not contains a name" +
                    " attribute", content.getPath()));
        }
        return name;
    }

    //
    //    private void deployLayouts(ApplicationArchive applicationArchive) throws IOException, ClientException {
    //        for (Layout layout : applicationArchive.getDeploymentDescriptor().getLayouts()) {
    //            bonitaClient.importPage(applicationArchive.getFile(layout));
    //        }
    //    }
    //
    //    private void deployThemes(ApplicationArchive applicationArchive) throws IOException, ClientException {
    //        for (Theme theme : applicationArchive.getDeploymentDescriptor().getThemes()) {
    //            bonitaClient.importPage(applicationArchive.getFile(theme));
    //        }
    //    }
    //
    //    private void deployApplications(ApplicationArchive applicationArchive)
    //            throws IOException, ClientException {
    //        for (Application application : applicationArchive.getDeploymentDescriptor().getApplications()) {
    //            bonitaClient.importApplications(applicationArchive.getFile(application), application.getPolicy());
    //        }
    //    }

    private void deployProcesses(ApplicationArchive applicationArchive)
            throws InvalidBusinessArchiveFormatException, IOException, ProcessDeployException {

        for (Process process : applicationArchive.getDeploymentDescriptor().getProcesses()) {
            final BusinessArchive businessArchive = BusinessArchiveFactory
                    .readBusinessArchive(applicationArchive.getFile(process));
            final String processName = businessArchive.getProcessDefinition().getName();
            final String processVersion = businessArchive.getProcessDefinition().getVersion();
            ProcessDefinition processDefinition;

            try {
                // Let's try to deploy the process, even if it already exists:
                processDefinition = processAPI.deploy(businessArchive);
            } catch (AlreadyExistsException e) {
                log.info("{} Replacing the process with the new version.", e.getMessage());
                try {
                    // if it already exists, replace it with the new version:
                    final long existingProcessDefinitionId = processAPI.getProcessDefinitionId(processName,
                            processVersion);
                    deleteExistingProcess(existingProcessDefinitionId);
                    processDefinition = processAPI.deploy(businessArchive);
                } catch (ProcessDefinitionNotFoundException | ProcessActivationException | DeletionException
                        | AlreadyExistsException | SearchException ex) {
                    log.info("Cannot properly replace process {} ({}). Skipping.", processName, processVersion);
                    return;
                }
            }

            try {
                // Then let's try to deploy it, if it is resolved:
                final ProcessDeploymentInfo deploymentInfo = processAPI
                        .getProcessDeploymentInfo(processDefinition.getId());
                if (deploymentInfo.getConfigurationState() == ConfigurationState.RESOLVED) {
                    processAPI.enableProcess(processDefinition.getId());
                }
            } catch (ProcessEnablementException | ProcessDefinitionNotFoundException ex) {
                log.info("Failed to activate process {} ({}).", processName, processVersion);
                log.info("This is certainly due to configuration issues, see details below.", ex);
            }

            // TODO: should we return the list of processResolutionProblem ?

        }
    }

    private void deleteExistingProcess(long processDefinitionId)
            throws DeletionException, SearchException, ProcessActivationException, ProcessDefinitionNotFoundException {

        processAPI.disableProcess(processDefinitionId);

        final SearchOptions options = new SearchOptionsBuilder(0, 100)
                .filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId).done();
        List<ProcessInstance> processInstances;
        while (!(processInstances = processAPI.searchProcessInstances(options).getResult()).isEmpty()) {
            for (final ProcessInstance processInstance : processInstances) {
                processAPI.deleteProcessInstance(processInstance.getId());
            }
        }

        final SearchOptions archivedOptions = new SearchOptionsBuilder(0, 100)
                .filter(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId).done();
        List<ArchivedProcessInstance> archivedProcessInstances;
        while (!(archivedProcessInstances = processAPI.searchArchivedProcessInstances(archivedOptions).getResult())
                .isEmpty()) {
            for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
                processAPI.deleteArchivedProcessInstancesInAllStates(archivedProcessInstance.getSourceObjectId());
            }
        }

        processAPI.deleteProcessDefinition(processDefinitionId);

    }

    private void deployBDM(ApplicationArchive applicationArchive)
            throws IOException, BusinessDataRepositoryDeploymentException, UpdateException,
            InvalidBusinessDataModelException {
        BusinessDataModel bdm = applicationArchive.getDeploymentDescriptor().getBusinessDataModel();
        if (bdm != null) {
            try {
                if (!tenantAdministrationAPI.isPaused()) {
                    tenantAdministrationAPI.pause();
                }
                // TODO: in SP, also uninstall BDM Access Control here
                if (tenantAdministrationAPI.getBusinessDataModelResource()
                        .getState() == TenantResourceState.INSTALLED) {
                    tenantAdministrationAPI.uninstallBusinessDataModel();
                }
                tenantAdministrationAPI
                        .installBusinessDataModel(Files.readAllBytes(applicationArchive.getFile(bdm).toPath()));
            } finally {
                if (tenantAdministrationAPI.isPaused()) {
                    tenantAdministrationAPI.resume();
                }
            }
        }
    }
    //
    //    private void deployBdmAccessControl(ApplicationArchive applicationArchive)
    //            throws ClientException, IOException {
    //        BdmAccessControl bdmAccessControl = applicationArchive.getDeploymentDescriptor().getBdmAccessControl();
    //        if (bdmAccessControl != null) {
    //            bonitaClient.importBdmAccessControl(applicationArchive.getFile(bdmAccessControl.getFile()));
    //        }
    //    }
    //
    //    // =================================================================================================================
    //    // Delete methods
    //    // =================================================================================================================
    //
    //    /**
    //     * delete the application identified by this token
    //     *
    //     * @param applicationToken
    //     * @return true if the application was deleted, false otherwise
    //     * @throws UnauthorizedException
    //     * @throws IOException
    //     */
    //    public boolean deleteApplication(String applicationToken) throws ClientException, IOException {
    //        return inSession(() -> bonitaClient.deleteApplication(applicationToken));
    //    }

    //    public boolean deletePage(String token) throws DeletionException, SearchException {
    //        org.bonitasoft.engine.page.Page page = getPage(token);
    //        if (page == null) {
    //            return false;
    //        }
    //        pageAPI.deletePage(page.getId());
    //    }

    org.bonitasoft.engine.page.Page getPage(String urlToken) throws SearchException {
        final SearchResult<org.bonitasoft.engine.page.Page> pages = pageAPI
                .searchPages(new SearchOptionsBuilder(0, 1).filter(PageSearchDescriptor.NAME, urlToken).done());
        if (pages.getCount() == 0) {
            log.debug("Can't find any existing page with the token '{}'.", urlToken);
            return null;
        }
        log.debug("Page '{}' retrieved successfully.", urlToken);
        return pages.getResult().get(0);
    }

    //    public boolean deleteRestApiExtension(String token) throws IOException {
    //        return bonitaClient.deletePage(token);
    //    }

}
