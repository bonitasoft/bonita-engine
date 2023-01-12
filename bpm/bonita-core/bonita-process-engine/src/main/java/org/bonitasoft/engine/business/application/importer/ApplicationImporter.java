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
package org.bonitasoft.engine.business.application.importer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.io.IOUtils;
import org.bonitasoft.engine.session.SessionService;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
@Slf4j
//must be initialized after PageService
@Order(6)
public class ApplicationImporter implements TenantLifecycleService {

    public static final String PROVIDED_APPLICATIONS_PATH = "org/bonitasoft/web/application";
    private final ApplicationService applicationService;
    private final NodeToApplicationConverter nodeToApplicationConverter;
    private final ApplicationPageImporter applicationPageImporter;
    private final ApplicationMenuImporter applicationMenuImporter;
    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            ApplicationImporter.class.getClassLoader());

    /**
     * Boolean used to import provided editable applications if there are missing (mostly when it is a first install)
     */
    @Setter
    private boolean addIfMissing;

    public ApplicationImporter(ApplicationService applicationService,
            NodeToApplicationConverter nodeToApplicationConverter, ApplicationPageImporter applicationPageImporter,
            ApplicationMenuImporter applicationMenuImporter) {
        this.applicationService = applicationService;
        this.nodeToApplicationConverter = nodeToApplicationConverter;
        this.applicationPageImporter = applicationPageImporter;
        this.applicationMenuImporter = applicationMenuImporter;
    }

    private void updateHomePage(final SApplicationWithIcon application, final ApplicationNode applicationNode,
            final long createdBy, final ImportResult importResult) throws SBonitaException {
        if (applicationNode.getHomePage() != null) {
            try {
                SApplicationPage homePage = applicationService.getApplicationPage(applicationNode.getToken(),
                        applicationNode.getHomePage());
                SApplicationUpdateBuilder updateBuilder = new SApplicationUpdateBuilder(createdBy);
                updateBuilder.updateHomePageId(homePage.getId());
                applicationService.updateApplication(application, updateBuilder.done());
            } catch (SObjectNotFoundException e) {
                importResult.getImportStatus()
                        .addErrorsIfNotExists(List.of(
                                new ImportError(applicationNode.getHomePage(), ImportError.Type.APPLICATION_PAGE)));
            }
        }
    }

    ImportStatus importApplication(ApplicationNode applicationNode, boolean editable,
            long createdBy, byte[] iconContent,
            String iconMimeType, boolean addIfMissing, ApplicationImportStrategy strategy)
            throws ImportException, AlreadyExistsException {
        try {
            ImportResult importResult = nodeToApplicationConverter.toSApplication(applicationNode, iconContent,
                    iconMimeType, createdBy);
            SApplicationWithIcon applicationToBeImported = importResult.getApplication();
            applicationToBeImported.setEditable(editable);
            ImportStatus importStatus = importResult.getImportStatus();
            SApplication conflictingApplication = applicationService
                    .getApplicationByToken(applicationToBeImported.getToken());
            importStatus.setStatus(ImportStatus.Status.ADDED);
            if (conflictingApplication != null) {
                switch (strategy.whenApplicationExists(conflictingApplication, applicationToBeImported)) {
                    case FAIL:
                        throw new AlreadyExistsException(
                                "An application with token '" + conflictingApplication.getToken() + "' already exists",
                                conflictingApplication.getToken());
                    case REPLACE:
                        importStatus.setStatus(ImportStatus.Status.REPLACED);
                        applicationService.forceDeleteApplication(conflictingApplication);
                        break;
                    case SKIP:
                        importStatus.setStatus(ImportStatus.Status.SKIPPED);
                        break;
                }
            }

            if (!addIfMissing) {
                importStatus.setStatus(ImportStatus.Status.SKIPPED);
            }

            if (importStatus.getStatus() != ImportStatus.Status.SKIPPED) {
                applicationService.createApplication(applicationToBeImported);
                importStatus.addErrorsIfNotExists(applicationPageImporter
                        .importApplicationPages(applicationNode.getApplicationPages(), applicationToBeImported));
                importStatus
                        .addErrorsIfNotExists(
                                applicationMenuImporter.importApplicationMenus(applicationNode.getApplicationMenus(),
                                        applicationToBeImported));
                updateHomePage(applicationToBeImported, applicationNode, createdBy, importResult);
            }
            return importStatus;
        } catch (SBonitaException e) {
            throw new ImportException(e);
        }
    }

    public List<ImportStatus> importApplications(final byte[] xmlContent, byte[] iconContent, String iconMimeType,
            long createdBy, ApplicationImportStrategy strategy) throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        List<ImportStatus> importStatus = new ArrayList<>();
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(
                    importApplication(applicationNode, true, createdBy, iconContent, iconMimeType, true, strategy));
        }
        return importStatus;
    }

    private List<ImportStatus> importProvidedEditableApplications(final byte[] xmlContent, byte[] iconContent,
            String iconMimeType, boolean addIfMissing) throws ImportException, AlreadyExistsException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            // import the provided application as editable and set the strategy to skip it if a version already exists
            importStatuses.add(
                    importApplication(applicationNode, true, SessionService.SYSTEM_ID, iconContent, iconMimeType,
                            addIfMissing,
                            new UpdateNewerNonEditableApplicationStrategy()));
        }
        return importStatuses;
    }

    public ApplicationNodeContainer getApplicationNodeContainer(byte[] xmlContent) throws ImportException {
        ApplicationNodeContainer result;
        final URL resource = ApplicationNodeContainer.class.getResource("/application.xsd");
        try {
            result = IOUtils.unmarshallXMLtoObject(xmlContent, ApplicationNodeContainer.class, resource);
        } catch (final Exception e) {
            throw new ImportException(e);
        }
        return result;
    }

    @Override
    public void init() throws SBonitaException {
        try {
            List<ImportStatus> importStatuses = importProvidedEditableApplicationsFromClasspath();

            List<String> createdOrReplaced = importStatuses.stream()
                    .filter(importStatus -> importStatus.getStatus() != ImportStatus.Status.SKIPPED)
                    .map(importStatus -> importStatus.getName() + " " + importStatus.getStatus())
                    .collect(Collectors.toList());
            if (createdOrReplaced.isEmpty()) {
                log.info("No applications updated");
            } else {
                log.info("Application updated or created : {}", createdOrReplaced);
            }
        } catch (Exception e) {
            log.error("Cannot load provided applications at startup. Root cause: {}",
                    ExceptionUtils.printRootCauseOnly(e));
            log.debug("Full stack : ", e);
        }
    }

    private List<ImportStatus> importProvidedEditableApplicationsFromClasspath() throws IOException, ImportException {
        if (addIfMissing) {
            log.info("Detected a first run since a Bonita update, a Bonita upgrade, " +
                    "a tenant creation or an installation from scratch. Importing default applications");
        }
        List<ImportStatus> importStatuses = new ArrayList<>();
        Resource[] resources = cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + PROVIDED_APPLICATIONS_PATH + "/*.zip");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                log.debug("Found provided applications '{}' in classpath", resourceName);
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    ApplicationZipContent zipContent = ApplicationZipContent.getApplicationZipContent(resourceName,
                            resourceAsStream);
                    importStatuses
                            .addAll(importProvidedEditableApplications(zipContent.getXmlRaw(), zipContent.getIconRaw(),
                                    zipContent.getPngName(), addIfMissing));
                } catch (IOException | ImportException | AlreadyExistsException e) {
                    throw new ImportException(e);
                }
            } else {
                throw new ImportException(
                        "A resource " + resource + " could not be read when loading default applications");
            }
        }
        return importStatuses;
    }
}
