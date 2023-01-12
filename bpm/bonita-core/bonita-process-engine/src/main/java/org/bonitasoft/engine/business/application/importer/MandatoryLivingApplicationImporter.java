/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.session.SessionService;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Service used to import provided living applications that are mandatory for the platform to start, such as:
 * <ul>
 * <li>Bonita Super Admin Application</li>
 * <li>Bonita Application Directory</li>
 * </ul>
 */
@Component
@Order(4)
@Slf4j
@RequiredArgsConstructor
public class MandatoryLivingApplicationImporter implements TenantLifecycleService {

    private static final String NON_EDITABLE_NON_REMOVABLE_PAGES_PATH = "org/bonitasoft/web/page/final";

    private static final String EDITABLE_NON_REMOVABLE_PAGES_PATH = "org/bonitasoft/web/page/editonly";

    private static final String PROVIDED_FINAL_APPLICATIONS_PATH = "org/bonitasoft/web/application/final";

    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            MandatoryLivingApplicationImporter.class.getClassLoader());

    private final PageService pageService;

    private final ApplicationImporter applicationImporter;

    @Override
    public void init() throws SBonitaException {
        // Step 1: import mandatory pages
        log.info("Importing Bonita mandatory pages");
        importMandatoryPages();

        // Step 2: import mandatory living apps
        log.info("Importing Bonita mandatory applications");
        importMandatoryApplications();
    }

    private void importMandatoryPages() {
        try {
            List<ImportStatus> importStatuses = importProvidedNonRemovableNonEditablePagesFromClasspath();

            boolean firstRun = importStatuses.stream().map(ImportStatus::getStatus)
                    .allMatch(importStatus -> importStatus == ImportStatus.Status.ADDED);
            pageService.setAddRemovableIfMissing(firstRun);

            importStatuses.addAll(importProvidedNonRemovableEditablePagesFromClasspath());

            List<String> createdOrReplaced = importStatuses.stream()
                    .filter(importStatus -> importStatus.getStatus() != ImportStatus.Status.SKIPPED)
                    .map(importStatus -> importStatus.getName() + " " + importStatus.getStatus())
                    .collect(Collectors.toList());
            if (createdOrReplaced.isEmpty()) {
                log.info("No page updated");
            } else {
                log.info("Page updated or created : {}", createdOrReplaced);
            }
        } catch (BonitaException | IOException e) {
            log.error(
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }

    private List<ImportStatus> importProvidedNonRemovableNonEditablePagesFromClasspath()
            throws BonitaException, IOException {
        return importProvidedPagesFromResourcePattern(false, NON_EDITABLE_NON_REMOVABLE_PAGES_PATH);
    }

    private List<ImportStatus> importProvidedNonRemovableEditablePagesFromClasspath()
            throws IOException, BonitaException {
        return importProvidedPagesFromResourcePattern(true, EDITABLE_NON_REMOVABLE_PAGES_PATH);
    }

    private List<ImportStatus> importProvidedPagesFromResourcePattern(boolean editable, String resourcesPath)
            throws IOException, BonitaException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        Resource[] resources = cpResourceResolver
                .getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + resourcesPath + "/*.zip");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    log.debug("Found provided page '{}' in classpath", resourceName);
                    final byte[] content = org.apache.commons.io.IOUtils.toByteArray(resourceAsStream);
                    importStatuses.add(pageService.importProvidedPage(resourceName, content, false, editable, true));
                } catch (IOException | SBonitaException e) {
                    throw new BonitaException("Unable to import the page " + resourceName, e);
                }
            } else {
                throw new BonitaException(
                        "A resource " + resource.getDescription() + " could not be read when loading default pages");
            }
        }
        return importStatuses;
    }

    private void importMandatoryApplications() {
        try {
            List<ImportStatus> importStatuses = importProvidedApplicationsFromClasspath();

            boolean addIfMissing = importStatuses.stream().map(ImportStatus::getStatus)
                    .allMatch(status -> status != ImportStatus.Status.SKIPPED);
            applicationImporter.setAddIfMissing(addIfMissing);

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

    private List<ImportStatus> importProvidedApplicationsFromClasspath()
            throws IOException, ImportException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        Resource[] resources = cpResourceResolver.getResources(
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + PROVIDED_FINAL_APPLICATIONS_PATH + "/*.zip");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                log.debug("Found provided applications '{}' in classpath", resourceName);
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    ApplicationZipContent zipContent = ApplicationZipContent.getApplicationZipContent(resourceName,
                            resourceAsStream);
                    importStatuses.addAll(importDefaultApplications(zipContent.getXmlRaw(), zipContent.getIconRaw(),
                            zipContent.getPngName()));
                } catch (IOException | ImportException | AlreadyExistsException e) {
                    throw new ImportException(e);
                }
            } else {
                throw new ImportException(
                        "A resource " + resource + "could not be read when loading default applications");
            }
        }
        return importStatuses;
    }

    private List<ImportStatus> importDefaultApplications(final byte[] xmlContent, byte[] iconContent,
            String iconMimeType) throws ImportException, AlreadyExistsException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        ApplicationNodeContainer applicationNodeContainer = applicationImporter.getApplicationNodeContainer(xmlContent);
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatuses.add(applicationImporter.importApplication(applicationNode, false, SessionService.SYSTEM_ID,
                    iconContent, iconMimeType, true, new UpdateNewerNonEditableApplicationStrategy()));
        }
        return importStatuses;
    }

}
