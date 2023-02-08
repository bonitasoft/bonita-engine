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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.bonitasoft.engine.api.result.Status.*;
import static org.bonitasoft.engine.api.result.StatusCode.*;
import static org.bonitasoft.engine.api.result.StatusContext.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.organization.OrganizationAPIDelegate;
import org.bonitasoft.engine.api.impl.page.PageAPIDelegate;
import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.api.impl.transaction.process.EnableProcess;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.bar.BusinessArchiveService;
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
import org.bonitasoft.engine.bpm.process.V6FormDeployException;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.business.application.importer.StrategySelector;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.commons.exceptions.SAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SV6FormsDeployException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.io.FileAndContent;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Main entry point to deploy an {@link ApplicationArchive}.
 *
 * @author Baptiste Mesta.
 */
@Builder
@AllArgsConstructor
@Slf4j
@Component
public class ApplicationInstaller {

    private final ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader();
    private PageAPIDelegate pageAPIDelegate;
    private OrganizationAPIDelegate organizationImporter;
    private BusinessDataModelRepository bdmRepository;
    private UserTransactionService transactionService;
    private TenantStateManager tenantStateManager;
    private SessionAccessor sessionAccessor;
    private SessionService sessionService;
    private BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    private Long tenantId;

    @Autowired
    public ApplicationInstaller(@Qualifier("businessDataModelRepository") BusinessDataModelRepository bdmRepository,
            UserTransactionService transactionService, @Value("${tenantId}") Long tenantId,
            SessionAccessor sessionAccessor, SessionService sessionService, TenantStateManager tenantStateManager,
            @Qualifier("dependencyResolver") BusinessArchiveArtifactsManager businessArchiveArtifactsManager) {
        this.bdmRepository = bdmRepository;
        this.transactionService = transactionService;
        this.tenantId = tenantId;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.tenantStateManager = tenantStateManager;
        this.businessArchiveArtifactsManager = businessArchiveArtifactsManager;
    }

    private PageAPIDelegate getPageAPIDelegate() {
        if (pageAPIDelegate == null) {
            pageAPIDelegate = new PageAPIDelegate(getTenantAccessor(), SessionService.SYSTEM_ID);
        }
        return pageAPIDelegate;
    }

    private OrganizationAPIDelegate getOrganizationImporter() {
        if (organizationImporter == null) {
            organizationImporter = new OrganizationAPIDelegate(getTenantAccessor());
        }
        return organizationImporter;
    }

    public ExecutionResult install(InputStream applicationZipFileStream) throws ApplicationInstallationException {
        final ExecutionResult result = install(readApplicationArchiveFile(applicationZipFileStream));
        logInstallationResult(result);
        return result;
    }

    public ExecutionResult install(byte[] applicationArchiveFile) throws ApplicationInstallationException {
        return install(readApplicationArchiveFile(applicationArchiveFile));
    }

    private static void logInstallationResult(ExecutionResult result) {
        log.info("Result of the installation of the application:");
        for (Status s : result.getAllStatus()) {
            log.info("[{}] - {} - {} - {}", s.getLevel(), s.getCode(), s.getMessage(),
                    s.getContext().toString());
        }
    }

    @VisibleForTesting
    ExecutionResult install(ApplicationArchive applicationArchive) throws ApplicationInstallationException {
        try {
            final ExecutionResult executionResult = new ExecutionResult();
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive installation...");
            installBusinessDataModel(applicationArchive);
            inSession(() -> inTransaction(() -> {
                installOrganization(applicationArchive, executionResult);
                // Move to SP: installProfiles(applicationArchive, executionResult);
                installRestApiExtensions(applicationArchive, executionResult);
                installPages(applicationArchive, executionResult);
                installLayouts(applicationArchive, executionResult);
                installThemes(applicationArchive, executionResult);
                installLivingApplications(applicationArchive, executionResult);
                installProcesses(applicationArchive, executionResult);
                return null;
            }));
            log.info("The Application Archive has been installed successfully in {} ms.",
                    (System.currentTimeMillis() - startPoint));
            return executionResult;
        } catch (Exception e) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted", e);
        }
    }

    void installOrganization(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws OrganizationImportException {
        final List<String> warnings = getOrganizationImporter().importOrganizationWithWarnings(
                new String(applicationArchive.getOrganization().getContent(), UTF_8),
                ImportPolicy.MERGE_DUPLICATES);
        for (String warning : warnings) {
            executionResult.addStatus(warningStatus(ORGANIZATION_IMPORT_WARNING, warning));
        }
        executionResult.addStatus(okStatus());
    }

    void installBusinessDataModel(ApplicationArchive applicationArchive) throws Exception {
        if (applicationArchive.getBdm() != null) {
            inSession(() -> {
                pauseTenant();
                return null;
            });
            final String bdmVersion = inSession(() -> inTransaction(() -> updateBusinessDataModel(applicationArchive)));
            log.info("BDM successfully installed (version({})", bdmVersion);
            inSession(() -> {
                resumeTenant();
                return null;
            });
        }
    }

    private void resumeTenant() throws UpdateException {
        try {
            tenantStateManager.resume();
            transactionService.executeInTransaction(() -> {
                resolveDependenciesForAllProcesses();
                return null;
            });
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    private void resolveDependenciesForAllProcesses() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        businessArchiveArtifactsManager.resolveDependenciesForAllProcesses(tenantAccessor);
    }

    private String updateBusinessDataModel(ApplicationArchive applicationArchive)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        String bdmVersion;
        try {
            uninstallBusinessDataModel();
            bdmVersion = installBusinessDataModel(applicationArchive.getBdm().getContent());
        } catch (Exception e) {
            log.warn(
                    "Caught an error when installing/updating the BDM, the transaction will be reverted and the previous BDM restored.");
            throw e;
        }
        log.info("Update operation completed, the BDM was successfully updated");
        return bdmVersion;
    }

    public void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        log.info("Uninstalling the currently deployed BDM");
        try {
            tenantStateManager.executeTenantManagementOperation("BDM Uninstallation", () -> {
                bdmRepository.uninstall(tenantId);
                return null;
            });
            log.info("BDM successfully uninstalled");
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public String installBusinessDataModel(final byte[] zip)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        log.info("Starting the installation of the BDM.");
        try {
            String bdm_version = tenantStateManager.executeTenantManagementOperation("BDM Installation",
                    () -> bdmRepository.install(zip, SessionService.SYSTEM_ID));
            log.info("Installation of the BDM completed.");
            return bdm_version;
        } catch (final SBusinessDataRepositoryDeploymentException e) {
            throw new BusinessDataRepositoryDeploymentException(e);
        } catch (final InvalidBusinessDataModelException e) {
            throw e;
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private void pauseTenant() throws UpdateException {
        try {
            tenantStateManager.pause();
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    private ApplicationArchive readApplicationArchiveFile(InputStream applicationArchiveFile)
            throws ApplicationInstallationException {
        ApplicationArchive applicationArchive;
        try {
            applicationArchive = applicationArchiveReader.read(applicationArchiveFile);
        } catch (IOException e) {
            throw new ApplicationInstallationException("Unable to read application archive", e);
        }
        return applicationArchive;
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
            throws AlreadyExistsException, ImportException {
        List<FileAndContent> applications = applicationArchive.getApplications();
        for (FileAndContent applicationArchiveFile : applications) {
            log.info("Installing / updating Living Application from file '{}'", applicationArchiveFile.getFileName());
            final List<ImportStatus> importStatusList = importApplications(applicationArchiveFile.getContent());
            convertResultOfLivingApplicationImport(importStatusList, executionResult);
        }
    }

    List<ImportStatus> importApplications(final byte[] xmlContent)
            throws ImportException, AlreadyExistsException {
        return getTenantAccessor().getApplicationImporter().importApplications(xmlContent, null, null,
                SessionService.SYSTEM_ID,
                new StrategySelector().selectStrategy(ApplicationImportPolicy.REPLACE_DUPLICATES));
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
            updatePageContent(pageFile, existingPage);

            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_UPDATE_EXISTING,
                    format("Existing %s '%s' has been updated", precisePageType, getPageName(existingPage)),
                    context));
        } else {
            // page does not exist, we create it:
            final Page page = createPage(pageFile, pageToken);
            log.info("Creating new {} '{}'", precisePageType, getPageName(page));

            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_CREATE_NEW,
                    format("New %s '%s' has been installed", precisePageType, getPageName(page)),
                    context));
        }
    }

    Page createPage(FileAndContent pageFile, String pageToken) throws CreationException {
        return getPageAPIDelegate().createPage(pageToken, pageFile.getContent());
    }

    void updatePageContent(FileAndContent pageFile, Page existingPage) throws UpdateException {
        getPageAPIDelegate().updatePageContent(existingPage.getId(), pageFile.getContent());
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

    void installProcesses(ApplicationArchive applicationArchive, ExecutionResult executionResult)
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
                processDefinition = deployProcess(businessArchive);
                executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_CREATE_NEW,
                        format("New process %s (%s) has been installed successfully", processName, processVersion),
                        context));
            } catch (AlreadyExistsException e) {
                log.info("{} Replacing the process with the new version.", e.getMessage());
                try {
                    // if it already exists, replace it with the new version:
                    final long existingProcessDefinitionId = getProcessDefinitionId(processName, processVersion);
                    deleteExistingProcess(existingProcessDefinitionId, processName, processVersion);
                    processDefinition = deployProcess(businessArchive);
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
                // Then let's try to enable it, if it is resolved:
                final ProcessDeploymentInfo deploymentInfo = getProcessDeploymentInfo(processDefinition);
                if (deploymentInfo.getConfigurationState() == ConfigurationState.RESOLVED) {
                    enableProcess(processDefinition);
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

                    for (Problem problem : getProcessResolutionProblems(processDefinition)) {

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
        }
    }

    protected ProcessDeploymentInfo getProcessDeploymentInfo(ProcessDefinition processDefinition)
            throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            return ModelConvertor.toProcessDeploymentInfo(
                    processDefinitionService.getProcessDeploymentInfo(processDefinition.getId()));
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    protected void enableProcess(ProcessDefinition processDefinition)
            throws ProcessDefinitionNotFoundException, ProcessEnablementException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        try {
            new EnableProcess(processDefinitionService,
                    processDefinition.getId(), eventsHandler, SessionService.SYSTEM).execute();
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final Exception e) {
            throw new ProcessEnablementException(e);
        }
    }

    List<Problem> getProcessResolutionProblems(ProcessDefinition processDefinition)
            throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            SProcessDefinition sProcessDefinition = processDefinitionService
                    .getProcessDefinition(processDefinition.getId());
            return businessArchiveArtifactsManager.getProcessResolutionProblems(sProcessDefinition);
        } catch (final SProcessDefinitionNotFoundException | SBonitaReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    long getProcessDefinitionId(String processName, String processVersion)
            throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            return processDefinitionService.getProcessDefinitionId(processName, processVersion);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    protected ProcessDefinition deployProcess(BusinessArchive businessArchive)
            throws AlreadyExistsException, ProcessDeployException {
        validateBusinessArchive(businessArchive);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final BusinessArchiveService businessArchiveService = tenantAccessor.getBusinessArchiveService();
        try {
            return ModelConvertor.toProcessDefinition(businessArchiveService.deploy(businessArchive));
        } catch (SV6FormsDeployException e) {
            throw new V6FormDeployException(e);
        } catch (SObjectCreationException e) {
            throw new ProcessDeployException(e);
        } catch (SAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        }
    }

    void validateBusinessArchive(BusinessArchive businessArchive) throws ProcessDeployException {
        for (Map.Entry<String, byte[]> resource : businessArchive.getResources().entrySet()) {
            final byte[] resourceContent = resource.getValue();
            if (resourceContent == null || resourceContent.length == 0) {
                throw new ProcessDeployException("The BAR file you are trying to deploy contains an empty file: "
                        + resource.getKey() + ". The process cannot be deployed. Fix it or remove it from the BAR.");
            }
        }
    }

    private void deleteExistingProcess(long processDefinitionId, String processName, String processVersion)
            throws DeletionException, SearchException, ProcessDefinitionNotFoundException {
        try {
            disableProcess(processDefinitionId);
        } catch (ProcessActivationException e) {
            log.debug("Process {} ({}) is disabled.", processName, processVersion);
        }

        final SearchOptions options = new SearchOptionsBuilder(0, 100)
                .filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId).done();
        List<ProcessInstance> processInstances;
        while (!(processInstances = getExistingProcessInstances(options)).isEmpty()) {
            for (final ProcessInstance processInstance : processInstances) {
                deleteProcessInstance(processInstance);
            }
        }

        final SearchOptions archivedOptions = new SearchOptionsBuilder(0, 100)
                .filter(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinitionId).done();
        List<ArchivedProcessInstance> archivedProcessInstances;
        while (!(archivedProcessInstances = getExistingArchivedProcessInstances(archivedOptions))
                .isEmpty()) {
            for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
                deleteArchivedProcessInstancesInAllStates(archivedProcessInstance);
            }
        }

        deleteProcessDefinition(processDefinitionId);

    }

    void deleteProcessDefinition(long processDefinitionId) throws DeletionException {
        //        processAPI.deleteProcessDefinition(processDefinitionId);
    }

    private void deleteArchivedProcessInstancesInAllStates(ArchivedProcessInstance archivedProcessInstance)
            throws DeletionException {
        if (archivedProcessInstance == null) {
            throw new IllegalArgumentException(
                    "The identifier of the archived process instances to deleted are missing !!");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();

        try {
            processInstanceService.deleteArchivedProcessInstances(List.of(archivedProcessInstance.getSourceObjectId()));
        } catch (final SProcessInstanceHierarchicalDeletionException e) {
            throw new ProcessInstanceHierarchicalDeletionException(e.getMessage(), e.getProcessInstanceId());
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    List<ArchivedProcessInstance> getExistingArchivedProcessInstances(SearchOptions archivedOptions)
            throws SearchException {
        return null; // processAPI.searchArchivedProcessInstances(archivedOptions).getResult();
    }

    private void deleteProcessInstance(ProcessInstance processInstance) throws DeletionException {
        //        processAPI.deleteProcessInstance(processInstance.getId());
    }

    List<ProcessInstance> getExistingProcessInstances(SearchOptions options) throws SearchException {
        return null; // processAPI.searchProcessInstances(options).getResult();
    }

    void disableProcess(long processDefinitionId)
            throws ProcessDefinitionNotFoundException, ProcessActivationException {
        //        processAPI.disableProcess(processDefinitionId);
    }

    org.bonitasoft.engine.page.Page getPage(String urlToken) throws SearchException {
        final SearchResult<org.bonitasoft.engine.page.Page> pages = getPageAPIDelegate()
                .searchPages(new SearchOptionsBuilder(0, 1).filter(PageSearchDescriptor.NAME, urlToken).done());
        if (pages.getCount() == 0) {
            log.debug("Can't find any existing page with the token '{}'.", urlToken);
            return null;
        }
        log.debug("Page '{}' retrieved successfully.", urlToken);
        return pages.getResult().get(0);
    }

    private TenantServiceAccessor getTenantAccessor() {
        try {
            ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return TenantServiceSingleton.getInstance();
    }

    @VisibleForTesting
    protected <T> T inSession(Callable<T> callable) throws Exception {
        final SSession session = sessionService.createSession(tenantId, SessionService.SYSTEM);
        final long sessionId = session.getId();
        log.info("Created new session with id {}", sessionId);
        try {
            sessionAccessor.setSessionInfo(sessionId, tenantId);
            final T result = callable.call();
            // sessionService.deleteSession(sessionId);
            return result;
        } finally {
            sessionAccessor.deleteSessionId();
            sessionAccessor.deleteTenantId();
        }
    }

    private <T> T inTransaction(Callable<T> callable) throws ApplicationInstallationException {
        try {
            return transactionService.executeInTransaction(callable);
        } catch (Exception e) {
            throw new ApplicationInstallationException("Problem installing application", e);
        }
    }

}
