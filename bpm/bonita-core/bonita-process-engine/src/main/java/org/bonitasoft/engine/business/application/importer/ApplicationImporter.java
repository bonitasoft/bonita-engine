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

import static org.bonitasoft.engine.commons.io.IOUtil.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@Order(5)
public class ApplicationImporter implements TenantLifecycleService {

    public static final String PROVIDED_FINAL_APPLICATIONS_PATH = "org/bonitasoft/web/application/final";
    public static final String PROVIDED_APPLICATIONS_PATH = "org/bonitasoft/web/application/";
    private final ApplicationService applicationService;
    private final NodeToApplicationConverter nodeToApplicationConverter;
    private final ApplicationPageImporter applicationPageImporter;
    private final ApplicationMenuImporter applicationMenuImporter;
    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            ApplicationImporter.class.getClassLoader());

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
                        .addErrorsIfNotExists(Arrays.asList(
                                new ImportError(applicationNode.getHomePage(), ImportError.Type.APPLICATION_PAGE)));
            }
        }
    }

    ImportStatus importApplication(ApplicationNode applicationNode, boolean editable,
            long createdBy, byte[] iconContent,
            String iconMimeType, ApplicationImportStrategy strategy)
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
        ArrayList<ImportStatus> importStatus = new ArrayList<>();
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(importApplication(applicationNode, true, createdBy, iconContent, iconMimeType, strategy));
        }
        return importStatus;
    }

    private void importDefaultApplications(final byte[] xmlContent, byte[] iconContent, String iconMimeType,
            boolean editable) throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importApplication(applicationNode, editable, SessionService.SYSTEM_ID, iconContent, iconMimeType,
                    new UpdateNewerNonEditableApplicationStrategy());
        }
    }

    private ApplicationNodeContainer getApplicationNodeContainer(byte[] xmlContent) throws ImportException {
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
            importProvidedApplicationsFromClasspath(PROVIDED_APPLICATIONS_PATH, true);
            importProvidedApplicationsFromClasspath(PROVIDED_FINAL_APPLICATIONS_PATH, false);
        } catch (IOException e) {
            log.error("Cannot load provided applications at startup. Root cause: {}",
                    ExceptionUtils.printRootCauseOnly(e));
            log.debug("Full stack : ", e);
        }
    }

    private void importProvidedApplicationsFromClasspath(String path, boolean editable) throws IOException {
        Resource[] resources = cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + path + "/*.zip");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                importProvidedApplicationFromResource(resource, editable);
            } else {
                log.warn("A resource {} could not be read when loading default applications",
                        resource.getDescription());
            }
        }
    }

    private void importProvidedApplicationFromResource(Resource resource, boolean editable) {
        String resourceName = resource.getFilename();
        log.debug("Found provided applications '{}' in classpath", resourceName);
        try (InputStream resourceAsStream = resource.getInputStream()) {
            final byte[] content = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
            Map<String, byte[]> zipContent = unzip(content);
            List<String> pngFileNamesList = zipContent.keySet().stream().filter(l -> l.endsWith(".png"))
                    .collect(Collectors.toList());
            List<String> xmlFileNamesList = zipContent.keySet().stream().filter(l -> l.endsWith(".xml"))
                    .collect(Collectors.toList());
            if (xmlFileNamesList.size() > 1) {
                throw new ImportException("The application zip " + resourceName
                        + " contains more than one xml descriptor, and therefore has an invalid format");
            } else if (pngFileNamesList.size() > 1) {
                throw new ImportException("The application zip " + resourceName
                        + " contains more than one icon file, and therefore has an invalid format");
            }
            String pngName = pngFileNamesList.get(0);
            String xmlName = xmlFileNamesList.get(0);
            byte[] iconRaw = zipContent.get(pngName);
            byte[] xmlRaw = zipContent.get(xmlName);
            importDefaultApplications(xmlRaw, iconRaw, pngName, editable);
        } catch (IOException | ImportException | AlreadyExistsException e) {
            log.error("Unable to import the application {} because: {}", resourceName,
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }

}
