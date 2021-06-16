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
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.io.IOUtils;
import org.bonitasoft.engine.session.SessionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
@Slf4j
public class ApplicationImporter {

    public static final String PROVIDED_FINAL_APPLICATIONS_PATH = "org/bonitasoft/web/application/final";
    public static final String PROVIDED_APPLICATIONS_PATH = "org/bonitasoft/web/application/";
    private final ApplicationService applicationService;
    private final NodeToApplicationConverter nodeToApplicationConverter;
    private final ApplicationPageImporter applicationPageImporter;
    private final ApplicationMenuImporter applicationMenuImporter;
    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            ApplicationImporter.class.getClassLoader());

    public ApplicationImporter(ApplicationService applicationService,
            NodeToApplicationConverter nodeToApplicationConverter,
            ApplicationPageImporter applicationPageImporter, ApplicationMenuImporter applicationMenuImporter) {
        this.applicationService = applicationService;
        this.nodeToApplicationConverter = nodeToApplicationConverter;
        this.applicationPageImporter = applicationPageImporter;
        this.applicationMenuImporter = applicationMenuImporter;
    }

    public ImportStatus importApplication(ApplicationNode applicationNode, byte[] iconContent, String iconMimeType,
            long createdBy,
            ApplicationImportStrategy strategy)
            throws ImportException, AlreadyExistsException {
        return importApplicationSetEditable(applicationNode, iconContent, iconMimeType, createdBy, strategy, true);
    }

    ImportStatus importApplicationSetEditable(ApplicationNode applicationNode, byte[] iconContent, String iconMimeType,
            long createdBy,
            ApplicationImportStrategy strategy, boolean editable)
            throws ImportException, AlreadyExistsException {
        try {
            ImportResult importResult = nodeToApplicationConverter.toSApplication(applicationNode, iconContent,
                    iconMimeType, createdBy);
            importResult.getApplication().setEditable(editable);
            SApplicationWithIcon application = importApplication(importResult.getApplication(), importResult, strategy);
            importApplicationPages(applicationNode, importResult, application);
            importApplicationMenus(applicationNode, importResult, application);
            updateHomePage(application, applicationNode, createdBy, importResult);
            return importResult.getImportStatus();
        } catch (SBonitaException e) {
            throw new ImportException(e);
        }
    }

    private void updateHomePage(final SApplicationWithIcon application, final ApplicationNode applicationNode,
            final long createdBy, final ImportResult importResult)
            throws SBonitaException {
        if (applicationNode.getHomePage() != null) {
            try {
                SApplicationPage homePage = applicationService.getApplicationPage(applicationNode.getToken(),
                        applicationNode.getHomePage());
                SApplicationUpdateBuilder updateBuilder = new SApplicationUpdateBuilder(createdBy);
                updateBuilder.updateHomePageId(homePage.getId());
                applicationService.updateApplication(application, updateBuilder.done());
            } catch (SObjectNotFoundException e) {
                addError(importResult.getImportStatus(),
                        new ImportError(applicationNode.getHomePage(), ImportError.Type.APPLICATION_PAGE));
            }
        }
    }

    private void importApplicationMenus(final ApplicationNode applicationNode, final ImportResult importResult,
            final SApplicationWithIcon application)
            throws ImportException {
        for (ApplicationMenuNode applicationMenuNode : applicationNode.getApplicationMenus()) {
            List<ImportError> importErrors = applicationMenuImporter.importApplicationMenu(applicationMenuNode,
                    application, null);
            for (ImportError importError : importErrors) {
                addError(importResult.getImportStatus(), importError);
            }
        }
    }

    private void importApplicationPages(final ApplicationNode applicationNode, final ImportResult importResult,
            final SApplicationWithIcon application)
            throws ImportException {
        for (ApplicationPageNode applicationPageNode : applicationNode.getApplicationPages()) {
            ImportError importError = applicationPageImporter.importApplicationPage(applicationPageNode, application);
            addError(importResult.getImportStatus(), importError);
        }
    }

    private void addError(final ImportStatus importStatus, final ImportError importError) {
        if (importError != null && !importStatus.getErrors().contains(importError)) {
            importStatus.addError(importError);
        }
    }

    private SApplicationWithIcon importApplication(SApplicationWithIcon applicationToBeImported,
            ImportResult importResult, ApplicationImportStrategy strategy)
            throws SBonitaException, AlreadyExistsException {
        SApplication conflictingApplication = applicationService
                .getApplicationByToken(applicationToBeImported.getToken());
        if (conflictingApplication != null) {
            strategy.whenApplicationExists(conflictingApplication, applicationToBeImported);
            // if no exception is thrown, this is a replacement:
            importResult.getImportStatus().setStatus(ImportStatus.Status.REPLACED);
        }
        return applicationService.createApplication(applicationToBeImported);
    }

    public List<ImportStatus> importApplications(final byte[] xmlContent, byte[] iconContent, String iconMimeType,
            long createdBy,
            ApplicationImportStrategy strategy)
            throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        ArrayList<ImportStatus> importStatus = new ArrayList<>();
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(importApplication(applicationNode, iconContent, iconMimeType, createdBy, strategy));
        }
        return importStatus;
    }

    private List<ImportStatus> importDefaultApplications(final byte[] xmlContent, byte[] iconContent,
            String iconMimeType,
            long createdBy,
            ApplicationImportStrategy strategy, boolean editable)
            throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        ArrayList<ImportStatus> importStatus = new ArrayList<>();
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(importApplicationSetEditable(applicationNode, iconContent, iconMimeType, createdBy,
                    strategy, editable));
        }
        return importStatus;
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
            importDefaultApplications(xmlRaw, iconRaw, pngName, SessionService.SYSTEM_ID,
                    new ReplaceDuplicateApplicationImportStrategy(applicationService), editable);
        } catch (IOException | ImportException | AlreadyExistsException e) {
            log.error("Unable to import the application {} because: {}", resourceName,
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }
}
