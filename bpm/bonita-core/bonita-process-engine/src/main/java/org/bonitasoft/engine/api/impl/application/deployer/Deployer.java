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
package org.bonitasoft.engine.api.impl.application.deployer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.DeployerException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.io.FileAndContent;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

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
    private final PageAPI pageAPI;
    private final ApplicationAPI livingApplicationAPI;
    private final ProcessAPI processAPI;

    public ExecutionResult deploy(byte[] applicationArchiveFile) throws DeployerException {
        ApplicationArchive applicationArchive = readApplicationArchiveFile(applicationArchiveFile);
        return deploy(applicationArchive);
    }

    @VisibleForTesting
    ExecutionResult deploy(ApplicationArchive applicationArchive) {
        try {
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive deployment...");
            deployPages(applicationArchive);
            deployLayouts(applicationArchive);
            deployThemes(applicationArchive);
            deployRestApiExtensions(applicationArchive);
            deployLivingApplications(applicationArchive);
            deployProcesses(applicationArchive);

            log.info("The Application Archive has been deployed successfully in {} ms.",
                    (System.currentTimeMillis() - startPoint));
        } catch (Exception e) {
            throw new DeployerException("The Application Archive deploy operation has been aborted", e);
        }
        return ExecutionResult.OK;
    }

    private ApplicationArchive readApplicationArchiveFile(byte[] applicationArchiveFile) {
        ApplicationArchive applicationArchive;
        try {
            applicationArchive = applicationArchiveReader.read(applicationArchiveFile);
        } catch (IOException e) {
            throw new DeployerException("Unable to read application archive", e);
        }
        return applicationArchive;
    }

    private void deployLivingApplications(ApplicationArchive applicationArchive)
            throws AlreadyExistsException,
            ImportException {
        List<FileAndContent> applications = applicationArchive.getApplications();
        for (FileAndContent applicationArchiveFile : applications) {
            log.info("Deploying application from file {}", applicationArchiveFile.getFileName());
            livingApplicationAPI.importApplications(applicationArchiveFile.getContent(), ApplicationImportPolicy.REPLACE_DUPLICATES);
        }
    }

    private void deployPages(ApplicationArchive applicationArchive)
            throws IOException, BonitaException {
        for (FileAndContent pageFile : applicationArchive.getPages()) {
            deployUnitPage(pageFile);
        }
    }

    private void deployLayouts(ApplicationArchive applicationArchive)
            throws IOException, BonitaException {
        for (FileAndContent layoutFile : applicationArchive.getLayouts()) {
            deployUnitPage(layoutFile);
        }
    }

    private void deployThemes(ApplicationArchive applicationArchive)
            throws IOException, BonitaException {
        for (FileAndContent pageFile : applicationArchive.getThemes()) {
            deployUnitPage(pageFile);
        }
    }

    private void deployRestApiExtensions(ApplicationArchive applicationArchive)
            throws IOException, BonitaException {
        for (FileAndContent pageFile : applicationArchive.getRestAPIExtensions()) {
            deployUnitPage(pageFile);
        }
    }

    /**
     * From the Engine perspective, all custom pages, layouts, themes, custom Rest APIs are of type <code>Page</code>
     */
    private void deployUnitPage(FileAndContent pageFile) throws IOException, BonitaException {
        String pageToken = getPageToken(pageFile);
        org.bonitasoft.engine.page.Page existingPage = getPage(pageToken);
        if (existingPage != null) {
            //page already exists, we update it
            log.info("Updating existing page '{}'", existingPage.getName());
            pageAPI.updatePageContent(existingPage.getId(), pageFile.getContent());
        } else {
            //page do not exists, we create it
            log.info("Creating new page '{}'", pageToken);
            pageAPI.createPage(pageToken, pageFile.getContent());
        }
    }

    private String getPageToken(FileAndContent fileAndContent) throws IOException {
        byte[] pageProperties = FileOperations.getFileFromZip(new ByteArrayInputStream(fileAndContent.getContent()), "page.properties");
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(pageProperties));
        String name = properties.getProperty("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(String.format("Invalid page %s, page.properties do not contains a name" +
                    " attribute", fileAndContent.getFileName()));
        }
        return name;
    }

    private void deployProcesses(ApplicationArchive applicationArchive)
            throws InvalidBusinessArchiveFormatException, IOException, ProcessDeployException {

        for (FileAndContent process : applicationArchive.getProcesses()) {
            final BusinessArchive businessArchive = BusinessArchiveFactory
                    .readBusinessArchive(new ByteArrayInputStream(process.getContent()));
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
                    deleteExistingProcess(existingProcessDefinitionId, processName, processVersion);
                    processDefinition = processAPI.deploy(businessArchive);
                    log.info("Process {} ({}) has been deployed successfully.", processName, processVersion);
                } catch (ProcessDefinitionNotFoundException | DeletionException | AlreadyExistsException
                        | SearchException ex) {
                    log.info("Cannot properly replace process {} ({}) because {}. Skipping.", processName,
                            processVersion, ex.getMessage());
                    return;
                }
            }

            try {
                // Then let's try to deploy it, if it is resolved:
                final ProcessDeploymentInfo deploymentInfo = processAPI
                        .getProcessDeploymentInfo(processDefinition.getId());
                if (deploymentInfo.getConfigurationState() == ConfigurationState.RESOLVED) {
                    processAPI.enableProcess(processDefinition.getId());
                    log.info("Process {} ({}) has been enabled.", processName, processVersion);
                } else {
                    log.info("Process {} ({}) is not resolved and cannot be enabled. Here are the resolution problems:",
                            processName, processVersion);
                    for (Problem problem : processAPI.getProcessResolutionProblems(processDefinition.getId())) {
                        log.info(problem.getDescription());
                    }
                }
            } catch (ProcessEnablementException | ProcessDefinitionNotFoundException ex) {
                log.info("Failed to enable process {} ({}).", processName, processVersion);
                log.info("This is certainly due to configuration issues, see details below.", ex);
            }

            // TODO: should we return the list of processResolutionProblem ?

        }
    }

    private void deleteExistingProcess(long processDefinitionId, String processName, String processVersion)
            throws DeletionException, SearchException, ProcessDefinitionNotFoundException {
        try {
            processAPI.disableProcess(processDefinitionId);
        } catch (ProcessActivationException e) {
            log.debug("Process {} ({}) is disabled.", processName, processVersion);
        }

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

}
