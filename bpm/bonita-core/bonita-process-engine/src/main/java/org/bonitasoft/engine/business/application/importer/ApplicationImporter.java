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
import org.bonitasoft.engine.commons.TenantLifecycleService;
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
public class ApplicationImporter implements TenantLifecycleService {

    public static final String PROVIDED_APPLICATIONS_PATH = "org/bonitasoft/web/application";
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

    public ImportStatus importApplication(ApplicationNode applicationNode, long createdBy,
            ApplicationImportStrategy strategy)
            throws ImportException, AlreadyExistsException {
        try {
            ImportResult importResult = nodeToApplicationConverter.toSApplication(applicationNode, createdBy);
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

    public List<ImportStatus> importApplications(final byte[] xmlContent, long createdBy,
            ApplicationImportStrategy strategy)
            throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        ArrayList<ImportStatus> importStatus = new ArrayList<>();
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(importApplication(applicationNode, createdBy, strategy));
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

    @Override
    public void init() throws SBonitaException {
        try {
            importProvidedApplicationsFromClasspath();
        } catch (IOException e) {
            log.error("Cannot load provided applications at startup. Root cause: {}",
                    ExceptionUtils.printRootCauseOnly(e));
            log.debug("Full stack : ", e);
        }
    }

    private void importProvidedApplicationsFromClasspath() throws IOException {
        Resource[] resources = cpResourceResolver
                .getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + PROVIDED_APPLICATIONS_PATH + "/*.xml");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                importProvidedApplicationFromResource(resource);
            } else {
                log.warn("A resource {} could not be read when loading default applications",
                        resource.getDescription());
            }
        }
    }

    private void importProvidedApplicationFromResource(Resource resource) {
        String resourceName = resource.getFilename();
        log.debug("Found provided applications '{}' in classpath", resourceName);
        try (InputStream resourceAsStream = resource.getInputStream()) {
            final byte[] content = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
            importApplications(content, SessionService.SYSTEM_ID,
                    new ReplaceDuplicateApplicationImportStrategy(applicationService));
        } catch (IOException | ImportException | AlreadyExistsException e) {
            log.error("Unable to import the application {} because: {}", resourceName,
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }

}
