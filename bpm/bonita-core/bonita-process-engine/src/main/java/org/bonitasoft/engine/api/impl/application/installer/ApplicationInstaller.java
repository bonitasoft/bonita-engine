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
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.bonitasoft.engine.api.result.Status.*;
import static org.bonitasoft.engine.api.result.StatusCode.*;
import static org.bonitasoft.engine.api.result.StatusContext.*;
import static org.bonitasoft.engine.bpm.process.ActivationState.DISABLED;
import static org.bonitasoft.engine.bpm.process.ConfigurationState.RESOLVED;
import static org.bonitasoft.engine.business.application.ApplicationImportPolicy.FAIL_ON_DUPLICATES;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.ProcessDeploymentAPIDelegate;
import org.bonitasoft.engine.api.impl.ProcessManagementAPIImplDelegate;
import org.bonitasoft.engine.api.impl.organization.OrganizationAPIDelegate;
import org.bonitasoft.engine.api.impl.page.PageAPIDelegate;
import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.business.application.exporter.ApplicationNodeContainerConverter;
import org.bonitasoft.engine.business.application.importer.ApplicationImporter;
import org.bonitasoft.engine.business.application.importer.StrategySelector;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.data.*;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.service.InstallationFailedException;
import org.bonitasoft.engine.service.InstallationService;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.ApplicationVersionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 * Main entry point to deploy an {@link ApplicationArchive}.
 *
 * @author Baptiste Mesta.
 * @author Danila Mazour
 * @author Haroun El Alami
 */

@Slf4j
@Component
@ConditionalOnSingleCandidate(ApplicationInstaller.class)
public class ApplicationInstaller {

    private static final String PAGE_TOKEN_PROPERTY = "name";
    private static final String PAGE_DISPLAY_NAME_PROPERTY = "displayName";
    private static final String PAGE_DESCRIPTION_PROPERTY = "description";
    private static final String PAGE_CONTENT_TYPE_PROPERTY = "contentType";

    private final InstallationService installationService;
    private final BusinessDataModelRepository bdmRepository;
    private final UserTransactionService transactionService;
    private final TenantStateManager tenantStateManager;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final ApplicationVersionService applicationVersionService;
    private final BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    private final ApplicationImporter applicationImporter;
    private final Long tenantId;
    private final ApplicationNodeContainerConverter appXmlConverter = new ApplicationNodeContainerConverter();

    public ApplicationInstaller(InstallationService installationService,
            @Qualifier("businessDataModelRepository") BusinessDataModelRepository bdmRepository,
            UserTransactionService transactionService, @Value("${tenantId}") Long tenantId,
            SessionAccessor sessionAccessor, SessionService sessionService,
            ApplicationVersionService applicationVersionService, TenantStateManager tenantStateManager,
            @Qualifier("dependencyResolver") BusinessArchiveArtifactsManager businessArchiveArtifactsManager,
            ApplicationImporter applicationImporter) {
        this.installationService = installationService;
        this.bdmRepository = bdmRepository;
        this.transactionService = transactionService;
        this.tenantId = tenantId;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.applicationVersionService = applicationVersionService;
        this.tenantStateManager = tenantStateManager;
        this.businessArchiveArtifactsManager = businessArchiveArtifactsManager;
        this.applicationImporter = applicationImporter;
    }

    private PageAPIDelegate getPageAPIDelegate() {
        return PageAPIDelegate.getInstance();
    }

    private OrganizationAPIDelegate getOrganizationImporter() {
        return OrganizationAPIDelegate.getInstance();
    }

    public void install(ApplicationArchive applicationArchive) throws ApplicationInstallationException {
        if (applicationArchive.isEmpty()) {
            throw new ApplicationInstallationException("The Application Archive contains no valid artifact to install");
        }
        final ExecutionResult executionResult = new ExecutionResult();
        try {
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive installation...");
            installBusinessDataModel(applicationArchive);
            inSession(() -> inTransaction(() -> {
                var newlyInstalledProcessIds = installArtifacts(applicationArchive, executionResult);
                enableResolvedProcesses(newlyInstalledProcessIds, executionResult);
                updateApplicationVersion(applicationArchive.getVersion());
                return null;
            }));
            log.info("The Application Archive (version {}) has been installed successfully in {} ms.",
                    applicationArchive.getVersion(), (System.currentTimeMillis() - startPoint));
        } catch (Exception e) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted", e);
        } finally {
            logInstallationResult(executionResult);
        }
        if (executionResult.hasErrors()) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted");
        }
    }

    protected List<Long> installArtifacts(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws Exception {
        installOrganization(applicationArchive, executionResult);
        installOrUpdateRestApiExtensions(applicationArchive, executionResult);
        installOrUpdatePages(applicationArchive, executionResult);
        installOrUpdateLayouts(applicationArchive, executionResult);
        installOrUpdateThemes(applicationArchive, executionResult);
        installLivingApplications(applicationArchive, executionResult, FAIL_ON_DUPLICATES);
        var installedProcessIds = installProcesses(applicationArchive, executionResult);
        applicationArchive.getConfigurationFile().ifPresent(configFile -> installConfiguration(configFile,
                executionResult));
        return installedProcessIds;
    }

    public void update(ApplicationArchive applicationArchive) throws ApplicationInstallationException {
        if (applicationArchive.isEmpty()) {
            throw new ApplicationInstallationException("The Application Archive contains no valid artifact to install");
        }
        final ExecutionResult executionResult = new ExecutionResult();
        try {
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive installation...");
            installBusinessDataModel(applicationArchive);
            inSession(() -> inTransaction(() -> {
                List<Long> newlyInstalledProcessIds = updateArtifacts(applicationArchive, executionResult);
                disableOldProcesses(newlyInstalledProcessIds, executionResult);
                enableResolvedProcesses(newlyInstalledProcessIds, executionResult);
                updateApplicationVersion(applicationArchive.getVersion());
                return null;
            }));
            log.info("The Application Archive has been installed successfully in {} ms.",
                    (System.currentTimeMillis() - startPoint));
        } catch (Exception e) {
            throw new ApplicationInstallationException("The Application Archive update operation has been aborted", e);
        } finally {
            logInstallationResult(executionResult);
        }
        if (executionResult.hasErrors()) {
            throw new ApplicationInstallationException("The Application Archive update operation has been aborted");
        }
    }

    @VisibleForTesting
    public void resumeTenantInSession() throws Exception {
        inSession(() -> {
            try {
                if (Objects.equals(STenant.PAUSED, tenantStateManager.getStatus())) {
                    tenantStateManager.resume();
                    transactionService.executeInTransaction(() -> {
                        businessArchiveArtifactsManager.resolveDependenciesForAllProcesses(getServiceAccessor());
                        return null;
                    });
                }
            } catch (Exception e) {
                throw new UpdateException(e);
            }
            return null;
        });
    }

    @VisibleForTesting
    public void pauseTenantInSession() throws Exception {
        inSession(() -> {
            try {
                String status = tenantStateManager.getStatus();
                if (STenant.ACTIVATED.equals(status)) {
                    tenantStateManager.pause();
                } else if (!STenant.PAUSED.equals(status)) {
                    throw new UpdateException(
                            "The default tenant is in state " + status + " and cannot be paused. Aborting.");
                }
            } catch (Exception e) {
                throw new UpdateException(e);
            }
            return null;
        });
    }

    protected List<Long> updateArtifacts(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws Exception {
        updateOrganization(applicationArchive, executionResult);

        installOrUpdateRestApiExtensions(applicationArchive, executionResult);
        installOrUpdatePages(applicationArchive, executionResult);
        installOrUpdateLayouts(applicationArchive, executionResult);
        installOrUpdateThemes(applicationArchive, executionResult);
        installLivingApplications(applicationArchive, executionResult, ApplicationImportPolicy.REPLACE_DUPLICATES);

        List<Long> newlyInstalledProcessIds = installProcesses(applicationArchive, executionResult);
        applicationArchive.getConfigurationFile().ifPresent(configFile -> installConfiguration(configFile,
                executionResult));
        return newlyInstalledProcessIds;
    }

    @VisibleForTesting
    public void updateApplicationVersion(String version) throws PlatformException {
        applicationVersionService.updateApplicationVersion(version);
    }

    @VisibleForTesting
    void disableProcess(long processDefinitionId) throws SBonitaException {
        getProcessManagementAPIDelegate().disableProcess(processDefinitionId);
    }

    @VisibleForTesting
    List<Long> getDeployedProcessIds() throws SearchException {
        SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE);
        return getProcessDeploymentAPIDelegate()
                .searchProcessDeploymentInfos(optsBuilder.done()).getResult()
                .stream().map(ProcessDeploymentInfo::getProcessId).collect(Collectors.toList());
    }

    @VisibleForTesting
    Optional<Long> getDeployedProcessId(String name, String version) {
        try {
            return Optional.of(getProcessDeploymentAPIDelegate().getProcessDefinitionId(name, version));
        } catch (ProcessDefinitionNotFoundException e) {
            return Optional.empty();
        }
    }

    public void disableOldProcesses(List<Long> installedProcessIds, ExecutionResult executionResult)
            throws SearchException, SBonitaException, ProcessDefinitionNotFoundException {
        List<Long> deployedProcessIds = getDeployedProcessIds();
        // remove installed process ids
        deployedProcessIds.removeAll(installedProcessIds);
        // disable all processes
        for (Long processId : deployedProcessIds) {
            // get process Info
            ProcessDeploymentInfo info = getProcessDeploymentInfo(processId);
            if (info.getActivationState() == ActivationState.ENABLED) {
                disableProcess(processId);
                executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_DISABLEMENT_OK,
                        format("Process %s (%s) has been disabled successfully",
                                info.getDisplayName(), info.getVersion())));
            }
        }
    }

    @VisibleForTesting
    ProcessDeploymentInfo getProcessDeploymentInfo(Long processId) throws ProcessDefinitionNotFoundException {
        return getProcessDeploymentAPIDelegate().getProcessDeploymentInfo(processId);
    }

    public void enableResolvedProcesses(List<Long> processDefinitionIds, ExecutionResult executionResult)
            throws ProcessDeployException {
        Collection<ProcessDeploymentInfo> processDeploymentInfos = getProcessDeploymentAPIDelegate()
                .getProcessDeploymentInfosFromIds(processDefinitionIds)
                .values();

        boolean atLeastOneBlockingProblem = false;
        // for all deployed process
        // if resolved and not already enabled,
        // enable it => if exception, add error status
        // if enablement ok, add info status Ok
        // if not resolved, add error status and list resolution problems
        // At the end, if at least one process disabled, throw Exception to cancel deployment and startup
        for (ProcessDeploymentInfo info : processDeploymentInfos) {
            if (info.getConfigurationState() == RESOLVED) {
                if (info.getActivationState() == DISABLED) {
                    try {
                        getProcessDeploymentAPIDelegate().enableProcess(info.getProcessId());
                    } catch (ProcessDefinitionNotFoundException | ProcessEnablementException e) {
                        final Map<String, Serializable> context = new HashMap<>();
                        context.put(PROCESS_NAME_KEY, info.getName());
                        context.put(PROCESS_VERSION_KEY, info.getVersion());
                        executionResult.addStatus(errorStatus(PROCESS_DEPLOYMENT_ENABLEMENT_KO,
                                format("Process %s (%s) could not be enabled", info.getName(), info.getVersion()),
                                context));
                        atLeastOneBlockingProblem = true;
                        continue;
                    }
                }
                executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_ENABLEMENT_OK,
                        format("Process %s (%s) has been enabled successfully",
                                info.getDisplayName(), info.getVersion())));
            } else {
                try {
                    atLeastOneBlockingProblem = true;
                    List<Problem> problems = getProcessDeploymentAPIDelegate()
                            .getProcessResolutionProblems(info.getProcessId());
                    String message = format(
                            "Process '%s' (%s) is unresolved. It cannot be enabled for now.",
                            info.getDisplayName(), info.getVersion());
                    String description = message + lineSeparator()
                            + problems.stream().map(Problem::getDescription)
                                    .collect(joining(lineSeparator()));
                    executionResult.addStatus(errorStatus(PROCESS_DEPLOYMENT_ENABLEMENT_KO, description));
                } catch (ProcessDefinitionNotFoundException e) {
                    executionResult
                            .addStatus(errorStatus(PROCESS_DEPLOYMENT_ENABLEMENT_KO, "Process definition not found"));
                }
            }
        }

        if (atLeastOneBlockingProblem) {
            throw new ProcessDeployException("At least one process failed to deploy / enable. Canceling installation.");
        }
    }

    @VisibleForTesting
    List<String> importOrganization(File organization, ImportPolicy failOnDuplicates)
            throws OrganizationImportException, IOException {
        return getOrganizationImporter().importOrganizationWithWarnings(
                Files.readString(organization.toPath()),
                failOnDuplicates);
    }

    protected void installOrganization(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws Exception {
        final List<String> warnings;
        if (applicationArchive.getOrganization() == null) {
            executionResult.addStatus(Status.infoStatus(ORGANIZATION_IMPORT_WARNING,
                    "No organization found. Use the technical user to configure the organization."));
            return;
        }
        try {
            warnings = importOrganization(applicationArchive.getOrganization(), ImportPolicy.FAIL_ON_DUPLICATES);
        } catch (IOException e) {
            throw new OrganizationImportException(e);
        }
        for (String warning : warnings) {
            executionResult.addStatus(warningStatus(ORGANIZATION_IMPORT_WARNING, warning));
        }
    }

    protected void updateOrganization(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws Exception {
        final List<String> warnings;
        if (applicationArchive.getOrganization() == null) {
            log.info("There is no organization file in the archive. Ignoring the organization update step.");
            return;
        }
        try {
            warnings = importOrganization(applicationArchive.getOrganization(), ImportPolicy.IGNORE_DUPLICATES);
        } catch (IOException e) {
            throw new OrganizationImportException(e);
        }
        for (String warning : warnings) {
            executionResult.addStatus(warningStatus(ORGANIZATION_IMPORT_WARNING, warning));
        }
    }

    protected void installBusinessDataModel(ApplicationArchive applicationArchive) throws Exception {
        if (applicationArchive.getBdm() != null) {
            var alreadyDeployed = sameBdmContentDeployed(applicationArchive.getBdm());
            if (alreadyDeployed) {
                log.info("Installed and current BDM are equivalent. No BDM update required.");
                return;
            }
            log.info("BDM must be installed or updated...");
            pauseTenantInSession();
            try {
                final String bdmVersion = inSession(
                        () -> inTransaction(() -> updateBusinessDataModel(applicationArchive)));
                log.info("BDM successfully installed (version({})", bdmVersion);
            } finally {
                resumeTenantInSession();
            }
        }
    }

    boolean sameBdmContentDeployed(File bdmArchive) throws Exception {
        return inSession(() -> inTransaction(() -> {
            log.info("Comparing BDM to install with current BDM...");
            return bdmRepository
                    .isDeployed(Files.readAllBytes(bdmArchive.toPath()));
        }));
    }

    protected String updateBusinessDataModel(ApplicationArchive applicationArchive)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        String bdmVersion;
        try {
            uninstallBusinessDataModel();
            bdmVersion = installBusinessDataModel(Files.readAllBytes(applicationArchive.getBdm().toPath()));
        } catch (IOException e) {
            log.warn("Cannot read the BDM file on disk");
            log.warn(
                    "Caught an error when installing/updating the BDM, the transaction will be reverted and the previous BDM restored.");
            throw new BusinessDataRepositoryDeploymentException(e);
        } catch (Exception e) {
            log.warn(
                    "Caught an error when installing/updating the BDM, the transaction will be reverted and the previous BDM restored.");
            throw e;
        }
        log.info("Update operation completed, the BDM was successfully updated");
        return bdmVersion;
    }

    protected void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
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

    protected String installBusinessDataModel(final byte[] zip)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        log.info("Starting the installation of the BDM.");
        try {
            String bdmVersion = tenantStateManager.executeTenantManagementOperation("BDM Installation",
                    () -> bdmRepository.install(zip, SessionService.SYSTEM_ID));
            log.info("Installation of the BDM completed.");
            return bdmVersion;
        } catch (final SBusinessDataRepositoryDeploymentException e) {
            throw new BusinessDataRepositoryDeploymentException(e);
        } catch (final InvalidBusinessDataModelException e) {
            throw e;
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected void installLivingApplications(ApplicationArchive applicationArchive, ExecutionResult executionResult,
            ApplicationImportPolicy policy)
            throws AlreadyExistsException, ImportException, ApplicationInstallationException {
        try {
            boolean atLeastOneBlockingProblem = false;
            for (File livingApplicationFile : applicationArchive.getApplications()) {
                log.info("Installing Living Application from file '{}'", livingApplicationFile.getName());
                var appContainer = appXmlConverter
                        .unmarshallFromXML(Files.readAllBytes(livingApplicationFile.toPath()));
                for (var application : appContainer.getApplications()) {
                    var status = importApplication(application,
                            getIconContent(application, applicationArchive), policy);
                    final Map<String, Serializable> context = new HashMap<>();
                    context.put(LIVING_APPLICATION_TOKEN_KEY, status.getName());
                    context.put(LIVING_APPLICATION_IMPORT_STATUS_KEY, status.getStatus());
                    final List<ImportError> errors = status.getErrors();
                    if (errors != null && !errors.isEmpty()) {
                        errors.forEach(error -> {
                            Status errorStatus = buildErrorStatus(error, livingApplicationFile.getName());
                            executionResult.addStatus(errorStatus);
                        });

                        atLeastOneBlockingProblem = true;
                        continue;
                    }

                    executionResult.addStatus(
                            infoStatus(LIVING_APP_DEPLOYMENT,
                                    format("Application '%s' has been %s", status.getName(),
                                            status.getStatus().name().toLowerCase()),
                                    context));
                }
            }

            if (atLeastOneBlockingProblem) {
                throw new ApplicationInstallationException(
                        "At least one application failed to be installed. Canceling installation.");
            }
        } catch (IOException | JAXBException | SAXException e) {
            throw new ImportException(e);
        }
    }

    private IconContent getIconContent(ApplicationNode application, ApplicationArchive applicationArchive) {
        var iconPath = application.getIconPath();
        if (iconPath != null && !iconPath.isBlank()) {
            var icon = applicationArchive.getApplicationIcons().stream()
                    .filter(iconFile -> Objects.equals(iconPath, iconFile.getName()))
                    .findFirst()
                    .map(File::toPath)
                    .orElse(null);
            try {
                if (icon != null) {
                    log.info("Application icon {} found for {}", icon.getFileName().toString(),
                            application.getDisplayName());
                    var bytes = Files.readAllBytes(icon);
                    return IconContent.builder()
                            .bytes(bytes)
                            .mimeType(URLConnection.guessContentTypeFromName(icon.getFileName().toString()))
                            .build();
                }
            } catch (IOException e) {
                log.warn("Failed to read icon {}", icon, e);
            }
        }
        return IconContent.builder().build();
    }

    private Status buildErrorStatus(ImportError importError, @NonNull String applicationName) {
        StatusCode code = null;
        switch (importError.getType()) {
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
        context.put(LIVING_APPLICATION_INVALID_ELEMENT_NAME, importError.getName());
        context.put(LIVING_APPLICATION_INVALID_ELEMENT_TYPE, importError.getType());
        return errorStatus(
                code,
                String.format("Unknown %s named '%s'", importError.getType().name(), importError.getName()),
                context);
    }

    ImportStatus importApplication(final ApplicationNode application, IconContent iconContent,
            ApplicationImportPolicy policy)
            throws ImportException, AlreadyExistsException {
        return applicationImporter.importApplication(application, true, SessionService.SYSTEM_ID,
                iconContent.getBytes(), iconContent.getMimeType(),
                true,
                new StrategySelector().selectStrategy(policy));
    }

    protected void installOrUpdatePages(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File pageFile : applicationArchive.getPages()) {
            installUnitPage(pageFile, "page", executionResult);
        }
    }

    protected void installOrUpdateLayouts(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File layoutFile : applicationArchive.getLayouts()) {
            installUnitPage(layoutFile, "layout", executionResult);
        }
    }

    protected void installOrUpdateThemes(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File pageFile : applicationArchive.getThemes()) {
            installUnitPage(pageFile, "theme", executionResult);
        }
    }

    protected void installOrUpdateRestApiExtensions(ApplicationArchive applicationArchive,
            ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File pageFile : applicationArchive.getRestAPIExtensions()) {
            installUnitPage(pageFile, "REST API extension", executionResult);
        }
    }

    /**
     * From the Engine perspective, all custom pages, layouts, themes, custom Rest APIs are of type <code>Page</code>
     */
    protected void installUnitPage(File pageFile, String precisePageType, ExecutionResult executionResult)
            throws IOException, BonitaException {
        var pageProperties = loadPageProperties(pageFile);
        var pageToken = pageProperties.getProperty(PAGE_TOKEN_PROPERTY);
        final Map<String, Serializable> context = new HashMap<>();
        context.put(PAGE_NAME_KEY, pageToken);
        Page existingPage = getPageIfExist(pageToken);
        if (existingPage == null) {
            final Page page = createPage(pageFile, pageProperties);
            log.info("Creating new {} '{}'", precisePageType, getPageName(page));
            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_CREATE_NEW,
                    format("New %s '%s' has been installed", precisePageType, getPageName(page)),
                    context));
        } else {
            updatePageContent(pageFile, existingPage.getId());
        }
    }

    @VisibleForTesting
    void updatePageContent(File pageFile, long pageId) throws UpdateException, IOException, AlreadyExistsException {
        getPageAPIDelegate().updatePageContent(pageId, Files.readAllBytes(pageFile.toPath()), SessionService.SYSTEM_ID);
        // update content name
        final PageUpdater pageUpdater = new PageUpdater();
        pageUpdater.setContentName(pageFile.getName());
        getPageAPIDelegate().updatePage(pageId, pageUpdater, SessionService.SYSTEM_ID);
    }

    @VisibleForTesting
    Page getPageIfExist(String pageToken) throws SearchException {
        List<Page> pageResearch = getPageAPIDelegate().searchPages(new SearchOptionsBuilder(0, 1)
                .filter(PageSearchDescriptor.NAME, pageToken).done()).getResult();
        if (!pageResearch.isEmpty()) {
            return pageResearch.get(0);
        }
        return null;
    }

    Page createPage(File pageFile, Properties pageProperties) throws CreationException {
        try {
            var pageCreator = new PageCreator(pageProperties.getProperty(PAGE_TOKEN_PROPERTY), pageFile.getName())
                    .setContentType(pageProperties.getProperty(PAGE_CONTENT_TYPE_PROPERTY))
                    .setDisplayName(pageProperties.getProperty(PAGE_DISPLAY_NAME_PROPERTY))
                    .setDescription(pageProperties.getProperty(PAGE_DESCRIPTION_PROPERTY));
            return getPageAPIDelegate().createPage(pageCreator,
                    Files.readAllBytes(pageFile.toPath()),
                    SessionService.SYSTEM_ID);
        } catch (IOException e) {
            throw new CreationException("Failed to read custom page content", e);
        }
    }

    private String getPageName(Page page) {
        return isNotBlank(page.getDisplayName()) ? page.getDisplayName() : page.getName();
    }

    private Properties loadPageProperties(File zipFile) throws IOException {
        var properties = new Properties();
        try (var pagePropertiesIs = new ByteArrayInputStream(
                FileOperations.getFileFromZip(zipFile, "page.properties"))) {
            properties.load(pagePropertiesIs);
            return validatePageProperties(zipFile, properties);
        }
    }

    private Properties validatePageProperties(File file, Properties properties) {
        String name = properties.getProperty(PAGE_TOKEN_PROPERTY);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    format("Invalid page %s, page.properties file do not contain mandatory '%s' attribute",
                            file.getName(), PAGE_TOKEN_PROPERTY));
        }
        String type = properties.getProperty(PAGE_CONTENT_TYPE_PROPERTY);
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException(
                    format("Invalid page %s, page.properties file do not contain mandatory '%s' attribute",
                            file.getName(), PAGE_CONTENT_TYPE_PROPERTY));
        }
        return properties;
    }

    protected List<Long> installProcesses(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws InvalidBusinessArchiveFormatException, IOException, ProcessDeployException {
        List<Long> processDefinitionIds = new ArrayList<>();
        for (File processFile : applicationArchive.getProcesses()) {
            try (var is = new FileInputStream(processFile)) {
                final BusinessArchive businessArchive = BusinessArchiveFactory
                        .readBusinessArchive(is);
                String name = businessArchive.getProcessDefinition().getName();
                String version = businessArchive.getProcessDefinition().getVersion();

                final Map<String, Serializable> context = new HashMap<>();
                context.put(PROCESS_NAME_KEY, name);
                context.put(PROCESS_VERSION_KEY, version);

                Optional<Long> deployedProcessId = getDeployedProcessId(name, version);
                if (deployedProcessId.isPresent()) {
                    // skip install
                    processDefinitionIds.add(deployedProcessId.get());
                    executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_SKIP_INSTALL,
                            format("Process %s (%s) already exists in the database. Skipping installation.", name,
                                    version),
                            context));
                } else {
                    processDefinitionIds.add(deployProcess(businessArchive, executionResult));
                }
            }
        }
        return processDefinitionIds;
    }

    /**
     * Must be called in a transaction with active session
     *
     * @param configurationFileArchive
     * @param executionResult
     * @throws ApplicationInstallationException
     */
    void installConfiguration(File configurationFileArchive,
            ExecutionResult executionResult)
            throws ApplicationInstallationException {
        try (var is = Files.newInputStream(configurationFileArchive.toPath())) {
            log.info("Installing application configuration from file");
            installationService.install(null, is.readAllBytes());
            executionResult.addStatus(Status.infoStatus(Status.okStatus().getCode(),
                    "Configuration file has been imported"));
        } catch (IOException | InstallationFailedException e) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted", e);
        }
    }

    /**
     * Update configuration with the given bconf file
     *
     * @param configurationFileArchive A bconf file
     * @param executionResult
     * @throws Exception
     */
    public void updateConfiguration(File configurationFileArchive, ExecutionResult executionResult) throws Exception {
        inSession(() -> inTransaction(() -> {
            installConfiguration(configurationFileArchive, executionResult);
            return null;
        }));
    }

    protected Long deployProcess(BusinessArchive businessArchive, ExecutionResult executionResult)
            throws ProcessDeployException {
        final String processName = businessArchive.getProcessDefinition().getName();
        final String processVersion = businessArchive.getProcessDefinition().getVersion();
        long processDefinitionId;

        final Map<String, Serializable> context = new HashMap<>();
        context.put(PROCESS_NAME_KEY, processName);
        context.put(PROCESS_VERSION_KEY, processVersion);
        try {
            // Let's try to deploy the process, even if it already exists:
            processDefinitionId = getProcessDeploymentAPIDelegate().deploy(businessArchive).getId();
            executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_CREATE_NEW,
                    format("New process %s (%s) has been installed successfully", processName, processVersion),
                    context));

        } catch (AlreadyExistsException e) {
            final String message = format("Process %s - %s already exists. Abandoning.", processName, processVersion);
            log.error(message);
            throw new ProcessDeployException(message);
        }
        return processDefinitionId;
    }

    @VisibleForTesting
    ProcessDeploymentAPIDelegate getProcessDeploymentAPIDelegate() {
        return ProcessDeploymentAPIDelegate.getInstance();
    }

    @VisibleForTesting
    ProcessManagementAPIImplDelegate getProcessManagementAPIDelegate() {
        return ProcessManagementAPIImplDelegate.getInstance();
    }

    @VisibleForTesting
    public <T> T inSession(Callable<T> callable) throws Exception {
        final SSession session = sessionService.createSession(tenantId, SessionService.SYSTEM);
        final long sessionId = session.getId();
        log.trace("New session created with id {}", sessionId);
        try {
            sessionAccessor.setSessionInfo(sessionId, tenantId);
            return callable.call();
        } finally {
            sessionAccessor.deleteSessionId();
            sessionAccessor.deleteTenantId();
        }
    }

    protected <T> T inTransaction(Callable<T> callable) throws ApplicationInstallationException {
        try {
            return transactionService.executeInTransaction(callable);
        } catch (Exception e) {
            throw new ApplicationInstallationException("Problem installing application", e);
        }
    }

    private ServiceAccessor getServiceAccessor() {
        return ServiceAccessorSingleton.getInstance();
    }

    void logInstallationResult(ExecutionResult result) {
        log.info("Result of the installation of the application:");
        for (Status s : result.getAllStatus()) {
            var message = s.getContext() != null && !s.getContext().isEmpty()
                    ? String.format("%s - %s - %s", s.getCode(), s.getMessage(),
                            s.getContext().toString())
                    : String.format("%s - %s", s.getCode(), s.getMessage());
            switch (s.getLevel()) {
                case ERROR:
                    log.error(message);
                    break;
                case WARNING:
                    log.warn(message);
                    break;
                case INFO:
                case OK:
                default:
                    log.info(message);
                    break;
            }
        }
    }

    @Builder
    @Data
    static class IconContent {

        private byte[] bytes;
        private String mimeType;
    }

}
