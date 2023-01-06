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

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.bonitasoft.engine.api.result.Status.*;
import static org.bonitasoft.engine.api.result.StatusCode.*;
import static org.bonitasoft.engine.api.result.StatusContext.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.api.result.StatusCode;
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
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.io.FileAndContent;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
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
public class ApplicationInstaller {

    private final ApplicationArchiveReader applicationArchiveReader;
    private final PageAPI pageAPI;
    private final ApplicationAPI livingApplicationAPI;
    private final ProcessAPI processAPI;

    public ExecutionResult install(byte[] applicationArchiveFile) throws ApplicationInstallationException {
        ApplicationArchive applicationArchive = readApplicationArchiveFile(applicationArchiveFile);
        return install(applicationArchive);
    }

    @VisibleForTesting
    ExecutionResult install(ApplicationArchive applicationArchive) throws ApplicationInstallationException {
        try {
            final ExecutionResult executionResult = new ExecutionResult();
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive deployment...");
            installRestApiExtensions(applicationArchive, executionResult);
            installPages(applicationArchive, executionResult);
            installLayouts(applicationArchive, executionResult);
            installThemes(applicationArchive, executionResult);
            installLivingApplications(applicationArchive, executionResult);
            installProcesses(applicationArchive, executionResult);

            log.info("The Application Archive has been installed successfully in {} ms.",
                    (System.currentTimeMillis() - startPoint));
            return executionResult;
        } catch (Exception e) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted", e);
        }
    }

    private ApplicationArchive readApplicationArchiveFile(byte[] applicationArchiveFile)
            throws ApplicationInstallationException {
        ApplicationArchive applicationArchive;
        try {
            applicationArchive = applicationArchiveReader.read(applicationArchiveFile);
        } catch (IOException e) {
            throw new ApplicationInstallationException("Unable to read application archive", e);
        }
        return applicationArchive;
    }

    private void installLivingApplications(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws AlreadyExistsException,
            ImportException {
        List<FileAndContent> applications = applicationArchive.getApplications();
        for (FileAndContent applicationArchiveFile : applications) {
            log.info("Installing / updating Living Application from file '{}'", applicationArchiveFile.getFileName());
            final List<ImportStatus> importStatusList = livingApplicationAPI.importApplications(
                    applicationArchiveFile.getContent(), ApplicationImportPolicy.REPLACE_DUPLICATES);

            convertResultOfLivingApplicationImport(importStatusList, executionResult);
        }
    }

    private void convertResultOfLivingApplicationImport(List<ImportStatus> importStatusList,
            ExecutionResult executionResult) {
        for (ImportStatus status : importStatusList) {
            final Map<String, Serializable> context = new HashMap<>();
            context.put(LIVING_APPLICATION_TOKEN_KEY, status.getName());
            context.put(LIVING_APPLICATION_IMPORT_STATUS_KEY, status.getStatus());
            final List<ImportError> errors = status.getErrors();
            if (errors != null && !errors.isEmpty()) {
                executionResult.addStatus(
                        warningStatus(LIVING_APP_DEPLOYMENT, format("Application '%s' has been %s with warnings",
                                status.getName(), status.getStatus().name().toLowerCase()), context));
                for (ImportError warning : errors) {
                    executionResult.addStatus(buildWarningStatus(warning, status.getName()));
                }
            } else {
                executionResult.addStatus(
                        infoStatus(LIVING_APP_DEPLOYMENT, format("Application '%s' has been %s", status.getName(),
                                status.getStatus().name().toLowerCase()), context));
            }
        }
    }

    private Status buildWarningStatus(ImportError warning, @NonNull String applicationName) {
        StatusCode code = null;
        switch (warning.getType()) {
            case PAGE:
                code = LIVING_APP_REFERENCES_UNKNOWN_PAGE;
                break;
            case PROFILE:
                code = LIVING_APP_REFERENCES_UNKNOWN_PROFILE;
                break;
            case APPLICATION_PAGE:
                code = LIVING_APP_REFERENCES_UNKNOWN_APPLICATION_PAGE;
                break;
            case LAYOUT:
                code = LIVING_APP_REFERENCES_UNKNOWN_LAYOUT;
                break;
            case THEME:
                code = LIVING_APP_REFERENCES_UNKNOWN_THEME;
                break;
            default:
                break;
        }
        final Map<String, Serializable> context = new HashMap<>();
        context.put(LIVING_APPLICATION_TOKEN_KEY, applicationName);
        context.put(LIVING_APPLICATION_INVALID_ELEMENT_NAME, warning.getName());
        context.put(LIVING_APPLICATION_INVALID_ELEMENT_TYPE, warning.getType());
        return warningStatus(
                code,
                String.format("Unknown %s named '%s'", warning.getType().name(), warning.getName()),
                context);
    }

    private void installPages(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (FileAndContent pageFile : applicationArchive.getPages()) {
            installUnitPage(pageFile, "page", executionResult);
        }
    }

    private void installLayouts(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (FileAndContent layoutFile : applicationArchive.getLayouts()) {
            installUnitPage(layoutFile, "layout", executionResult);
        }
    }

    private void installThemes(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (FileAndContent pageFile : applicationArchive.getThemes()) {
            installUnitPage(pageFile, "theme", executionResult);
        }
    }

    private void installRestApiExtensions(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (FileAndContent pageFile : applicationArchive.getRestAPIExtensions()) {
            installUnitPage(pageFile, "REST API extension", executionResult);
        }
    }

    /**
     * From the Engine perspective, all custom pages, layouts, themes, custom Rest APIs are of type <code>Page</code>
     */
    private void installUnitPage(FileAndContent pageFile, String precisePageType, ExecutionResult executionResult)
            throws IOException, BonitaException {
        String pageToken = getPageToken(pageFile);
        org.bonitasoft.engine.page.Page existingPage = getPage(pageToken);

        final Map<String, Serializable> context = new HashMap<>();
        context.put(PAGE_NAME_KEY, pageToken);

        if (existingPage != null) {
            // page already exists, we update it:
            log.info("Updating existing {} '{}'", precisePageType, getPageName(existingPage));
            pageAPI.updatePageContent(existingPage.getId(), pageFile.getContent());

            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_UPDATE_EXISTING,
                    format("Existing %s '%s' has been updated", precisePageType, getPageName(existingPage)),
                    context));
        } else {
            // page does not exist, we create it:
            final Page page = pageAPI.createPage(pageToken, pageFile.getContent());
            log.info("Creating new {} '{}'", precisePageType, getPageName(page));

            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_CREATE_NEW,
                    format("New %s '%s' has been installed", precisePageType, getPageName(page)),
                    context));
        }
    }

    private String getPageName(Page page) {
        return isNotBlank(page.getDisplayName()) ? page.getDisplayName() : page.getName();
    }

    private String getPageToken(FileAndContent fileAndContent) throws IOException {
        byte[] pageProperties = FileOperations.getFileFromZip(new ByteArrayInputStream(fileAndContent.getContent()),
                "page.properties");
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(pageProperties));
        String name = properties.getProperty("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    format("Invalid page %s, page.properties file do not contain mandatory 'name' attribute",
                            fileAndContent.getFileName()));
        }
        return name;
    }

    private void installProcesses(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws InvalidBusinessArchiveFormatException, IOException, ProcessDeployException {

        for (FileAndContent process : applicationArchive.getProcesses()) {
            final BusinessArchive businessArchive = BusinessArchiveFactory
                    .readBusinessArchive(new ByteArrayInputStream(process.getContent()));
            final String processName = businessArchive.getProcessDefinition().getName();
            final String processVersion = businessArchive.getProcessDefinition().getVersion();
            ProcessDefinition processDefinition;

            final Map<String, Serializable> context = new HashMap<>();
            context.put(PROCESS_NAME_KEY, processName);
            context.put(PROCESS_VERSION_KEY, processVersion);

            try {
                // Let's try to deploy the process, even if it already exists:
                processDefinition = processAPI.deploy(businessArchive);
                executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_CREATE_NEW,
                        format("New process %s (%s) has been installed successfully", processName, processVersion),
                        context));
            } catch (AlreadyExistsException e) {
                log.info("{} Replacing the process with the new version.", e.getMessage());
                try {
                    // if it already exists, replace it with the new version:
                    final long existingProcessDefinitionId = processAPI.getProcessDefinitionId(processName,
                            processVersion);
                    deleteExistingProcess(existingProcessDefinitionId, processName, processVersion);
                    processDefinition = processAPI.deploy(businessArchive);
                    log.info("Process {} ({}) has been installed successfully.", processName, processVersion);

                    executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_REPLACE_EXISTING,
                            format("Existing process %s (%s) has been replaced successfully",
                                    processName, processVersion),
                            context));

                } catch (ProcessDefinitionNotFoundException | DeletionException | AlreadyExistsException
                        | SearchException ex) {
                    log.info("Cannot properly replace process {} ({}) because {}. Skipping.", processName,
                            processVersion, ex.getMessage());

                    context.put(PROCESS_DEPLOYMENT_FAILURE_REASON_KEY, e.getMessage());
                    executionResult.addStatus(errorStatus(PROCESS_DEPLOYMENT_REPLACE_EXISTING,
                            format("Failed to replace existing process %s (%s): %s",
                                    processName, processVersion, e.getMessage()),
                            context));
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

                    executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_ENABLEMENT_OK,
                            format("Process %s (%s) has been enabled successfully",
                                    processName, processVersion),
                            context));
                } else {
                    log.info("Process {} ({}) is not resolved and cannot be enabled. Here are the resolution problems:",
                            processName, processVersion);
                    executionResult.addStatus(warningStatus(PROCESS_DEPLOYMENT_IMPOSSIBLE_UNRESOLVED,
                            format("Process %s (%s) cannot be enabled as it is not resolved",
                                    processName, processVersion),
                            context));

                    for (Problem problem : processAPI.getProcessResolutionProblems(processDefinition.getId())) {

                        log.info(problem.getDescription());

                        final Map<String, Serializable> unresolvedProcessContext = new HashMap<>();
                        unresolvedProcessContext.put(PROCESS_NAME_KEY, processName);
                        unresolvedProcessContext.put(PROCESS_VERSION_KEY, processVersion);
                        unresolvedProcessContext.put(PROCESS_RESOLUTION_PROBLEM_RESOURCE_TYPE_KEY,
                                problem.getResource());
                        unresolvedProcessContext.put(PROCESS_RESOLUTION_PROBLEM_RESOURCE_ID_KEY,
                                problem.getResourceId());
                        unresolvedProcessContext.put(PROCESS_RESOLUTION_PROBLEM_DESCRIPTION_KEY,
                                problem.getDescription());
                        executionResult.addStatus(
                                warningStatus(PROCESS_DEPLOYMENT_IMPOSSIBLE_UNRESOLVED,
                                        format("Process %s (%s) is not resolved for the following reasons",
                                                processName, processVersion),
                                        unresolvedProcessContext));
                    }
                }
            } catch (ProcessEnablementException | ProcessDefinitionNotFoundException e) {
                log.info("Failed to enable process {} ({}).", processName, processVersion);
                log.info("This is certainly due to configuration issues, see details below.", e);

                context.put(PROCESS_DEPLOYMENT_FAILURE_REASON_KEY, e.getMessage());
                executionResult.addStatus(warningStatus(PROCESS_DEPLOYMENT_ENABLEMENT_KO,
                        format("Failed to enable process %s (%s): %s",
                                processName, processVersion, e.getMessage()),
                        context));
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

    private org.bonitasoft.engine.page.Page getPage(String urlToken) throws SearchException {
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
