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

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.ProcessDeploymentAPIDelegate;
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
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.business.application.importer.StrategySelector;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.OrganizationImportException;
import org.bonitasoft.engine.io.FileOperations;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * Main entry point to deploy an {@link ApplicationArchive}.
 *
 * @author Baptiste Mesta.
 */

@Slf4j
@Component
@ConditionalOnSingleCandidate(ApplicationInstaller.class)
public class ApplicationInstaller {

    private final BusinessDataModelRepository bdmRepository;
    private final UserTransactionService transactionService;
    private final TenantStateManager tenantStateManager;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    private final Long tenantId;

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
        return PageAPIDelegate.getInstance();
    }

    private OrganizationAPIDelegate getOrganizationImporter() {
        return OrganizationAPIDelegate.getInstance();
    }

    public void install(ApplicationArchive applicationArchive) throws ApplicationInstallationException {
        try {
            final ExecutionResult executionResult = new ExecutionResult();
            final long startPoint = System.currentTimeMillis();
            log.info("Starting Application Archive installation...");
            installBusinessDataModel(applicationArchive);
            inSession(() -> inTransaction(() -> {
                installArtifacts(applicationArchive, executionResult);
                return null;
            }));
            log.info("The Application Archive has been installed successfully in {} ms.",
                    (System.currentTimeMillis() - startPoint));
            logInstallationResult(executionResult);
        } catch (Exception e) {
            throw new ApplicationInstallationException("The Application Archive install operation has been aborted", e);
        }
    }

    protected void installArtifacts(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws Exception {
        installOrganization(applicationArchive, executionResult);
        installRestApiExtensions(applicationArchive, executionResult);
        installPages(applicationArchive, executionResult);
        installLayouts(applicationArchive, executionResult);
        installThemes(applicationArchive, executionResult);
        installLivingApplications(applicationArchive, executionResult);
        installProcesses(applicationArchive, executionResult);
    }

    public void installOrganization(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws OrganizationImportException {
        final List<String> warnings;
        try {
            warnings = getOrganizationImporter().importOrganizationWithWarnings(
                    new String(Files.readAllBytes(applicationArchive.getOrganization().toPath()), UTF_8),
                    ImportPolicy.FAIL_ON_DUPLICATES);
        } catch (IOException e) {
            throw new OrganizationImportException(e);
        }
        for (String warning : warnings) {
            executionResult.addStatus(warningStatus(ORGANIZATION_IMPORT_WARNING, warning));
        }
        executionResult.addStatus(okStatus());
    }

    @VisibleForTesting
    public void installBusinessDataModel(ApplicationArchive applicationArchive) throws Exception {
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

    @VisibleForTesting
    void resumeTenant() throws UpdateException {
        try {
            tenantStateManager.resume();
            transactionService.executeInTransaction(() -> {
                businessArchiveArtifactsManager.resolveDependenciesForAllProcesses(getTenantAccessor());
                return null;
            });
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    @VisibleForTesting
    String updateBusinessDataModel(ApplicationArchive applicationArchive)
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

    @VisibleForTesting
    void pauseTenant() throws UpdateException {
        try {
            tenantStateManager.pause();
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    public void installLivingApplications(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws AlreadyExistsException, ImportException {
        try {
            for (File livingApplicationFile : applicationArchive.getApplications()) {
                log.info("Installing / updating Living Application from file '{}'", livingApplicationFile.getName());
                final List<ImportStatus> importStatusList = importApplications(
                        Files.readAllBytes(livingApplicationFile.toPath()));
                convertResultOfLivingApplicationImport(importStatusList, executionResult);
            }
        } catch (IOException e) {
            throw new ImportException(e);
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

    public void installPages(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File pageFile : applicationArchive.getPages()) {
            installUnitPage(pageFile, "page", executionResult);
        }
    }

    public void installLayouts(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File layoutFile : applicationArchive.getLayouts()) {
            installUnitPage(layoutFile, "layout", executionResult);
        }
    }

    public void installThemes(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File pageFile : applicationArchive.getThemes()) {
            installUnitPage(pageFile, "theme", executionResult);
        }
    }

    public void installRestApiExtensions(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws IOException, BonitaException {
        for (File pageFile : applicationArchive.getRestAPIExtensions()) {
            installUnitPage(pageFile, "REST API extension", executionResult);
        }
    }

    /**
     * From the Engine perspective, all custom pages, layouts, themes, custom Rest APIs are of type <code>Page</code>
     */
    public void installUnitPage(File pageFile, String precisePageType, ExecutionResult executionResult)
            throws IOException, BonitaException {
        byte[] pageContent = Files.readAllBytes(pageFile.toPath());
        String pageToken = getPageToken(pageFile);
        org.bonitasoft.engine.page.Page existingPage = getPage(pageToken);

        final Map<String, Serializable> context = new HashMap<>();
        context.put(PAGE_NAME_KEY, pageToken);

        if (existingPage != null) {
            // page already exists, we update it:
            log.info("Updating existing {} '{}'", precisePageType, getPageName(existingPage));
            updatePageContent(pageContent, existingPage);

            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_UPDATE_EXISTING,
                    format("Existing %s '%s' has been updated", precisePageType, getPageName(existingPage)),
                    context));
        } else {
            // page does not exist, we create it:
            final Page page = createPage(pageContent, pageToken);
            log.info("Creating new {} '{}'", precisePageType, getPageName(page));

            executionResult.addStatus(infoStatus(PAGE_DEPLOYMENT_CREATE_NEW,
                    format("New %s '%s' has been installed", precisePageType, getPageName(page)),
                    context));
        }
    }

    public Page createPage(byte[] pageContent, String pageToken) throws CreationException {
        return getPageAPIDelegate().createPage(pageToken, pageContent, SessionService.SYSTEM_ID);
    }

    void updatePageContent(byte[] pageContent, Page existingPage) throws UpdateException {
        getPageAPIDelegate().updatePageContent(existingPage.getId(), pageContent, SessionService.SYSTEM_ID);
    }

    private String getPageName(Page page) {
        return isNotBlank(page.getDisplayName()) ? page.getDisplayName() : page.getName();
    }

    private String getPageToken(File file) throws IOException {
        byte[] pageProperties = FileOperations.getFileFromZip(new FileInputStream(file),
                "page.properties");
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(pageProperties));
        String name = properties.getProperty("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    format("Invalid page %s, page.properties file do not contain mandatory 'name' attribute",
                            file.getName()));
        }
        return name;
    }

    protected void installProcesses(ApplicationArchive applicationArchive, ExecutionResult executionResult)
            throws InvalidBusinessArchiveFormatException, IOException, ProcessDeployException {
        for (File processFile : applicationArchive.getProcesses()) {
            final BusinessArchive businessArchive = BusinessArchiveFactory
                    .readBusinessArchive(new FileInputStream(processFile));
            final String processName = businessArchive.getProcessDefinition().getName();
            final String processVersion = businessArchive.getProcessDefinition().getVersion();

            final Map<String, Serializable> context = new HashMap<>();
            context.put(PROCESS_NAME_KEY, processName);
            context.put(PROCESS_VERSION_KEY, processVersion);

            try {
                // Let's try to deploy the process, even if it already exists:
                deployAndEnableProcess(businessArchive);
                log.info("Process {} ({}) has been enabled.", processName, processVersion);
                executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_CREATE_NEW,
                        format("New process %s (%s) has been installed successfully", processName, processVersion),
                        context));
                executionResult.addStatus(infoStatus(PROCESS_DEPLOYMENT_ENABLEMENT_OK,
                        format("Process %s (%s) has been enabled successfully", processName, processVersion),
                        context));
            } catch (AlreadyExistsException e) {
                final String message = format("Process %s - %s already exists. Abandoning.", processName,
                        processVersion);
                log.error(message);
                throw new ProcessDeployException(message);
            } catch (ProcessEnablementException e) {
                log.info("Failed to enable process {} ({}).", processName, processVersion);
                log.info("This is certainly due to configuration issues, see details below.", e);
                throw new ProcessDeployException(
                        format("Failed to enable process %s (%s). Process is not resolved",
                                processName, processVersion),
                        e);
            }
        }
    }

    protected ProcessDefinition deployAndEnableProcess(BusinessArchive businessArchive)
            throws AlreadyExistsException, ProcessDeployException, ProcessEnablementException {
        return ProcessDeploymentAPIDelegate.getInstance().deployAndEnableProcess(businessArchive);
    }

    org.bonitasoft.engine.page.Page getPage(String urlToken) throws SearchException {
        final SearchResult<Page> pages = getPageAPIDelegate()
                .searchPages(new SearchOptionsBuilder(0, 1).filter(PageSearchDescriptor.NAME, urlToken).done());
        if (pages.getCount() == 0) {
            log.debug("Can't find any existing page with the token '{}'.", urlToken);
            return null;
        }
        log.debug("Page '{}' retrieved successfully.", urlToken);
        return pages.getResult().get(0);
    }

    @VisibleForTesting
    public <T> T inSession(Callable<T> callable) throws Exception {
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

    public <T> T inTransaction(Callable<T> callable) throws ApplicationInstallationException {
        try {
            return transactionService.executeInTransaction(callable);
        } catch (Exception e) {
            throw new ApplicationInstallationException("Problem installing application", e);
        }
    }

    private TenantServiceAccessor getTenantAccessor() {
        try {
            ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return TenantServiceSingleton.getInstance();
    }

    private static void logInstallationResult(ExecutionResult result) {
        log.info("Result of the installation of the application:");
        for (Status s : result.getAllStatus()) {
            log.info("[{}] - {} - {} - {}", s.getLevel(), s.getCode(), s.getMessage(),
                    s.getContext().toString());
        }
    }
}
