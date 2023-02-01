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
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.session.SessionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.DigestUtils;

/**
 * Abstract class to regroup common code used by subclasses to import living applications.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class LivingApplicationImporter implements TenantLifecycleService {

    protected final PageService pageService;

    protected final ApplicationImporter applicationImporter;

    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            LivingApplicationImporter.class.getClassLoader());

    protected List<ImportStatus> importProvidedPagesFromClasspath(final String locationPattern, final boolean removable,
            final boolean editable, final boolean addIfMissing) throws IOException, BonitaException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        Resource[] resources = cpResourceResolver.getResources(locationPattern);
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    log.debug("Found provided page '{}' in classpath", resourceName);
                    final byte[] content = IOUtils.toByteArray(resourceAsStream);
                    importStatuses.add(
                            importProvidedPage(resourceName, content, removable, editable, addIfMissing));
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

    protected ImportStatus importProvidedPage(String pageZipName, final byte[] providedPageContent,
            boolean removable, boolean editable, boolean addIfMissing) throws SBonitaException {

        SPage page = pageService.buildPage(providedPageContent, pageZipName, SessionService.SYSTEM_ID, true, removable,
                editable);
        ImportStatus importStatus = new ImportStatus(page.getName());
        SPage sPageInDb = pageService.checkIfPageAlreadyExists(page);
        if (sPageInDb == null && addIfMissing) {
            log.debug("Provided page {} does not exist yet, importing it.", page.getName());
            page.setPageHash(DigestUtils.md5DigestAsHex(providedPageContent));
            pageService.insertPage(page, providedPageContent);
        } else if (sPageInDb == null) {
            log.debug("Provided page {} has been deleted by the user, and will not be imported", page.getName());
            importStatus.setStatus(ImportStatus.Status.SKIPPED);
        } else if (sPageInDb.isProvided()) {
            String md5Sum = DigestUtils.md5DigestAsHex(providedPageContent);
            if (Objects.equals(sPageInDb.getPageHash(), md5Sum)) {
                log.debug("Provided page exists and is up to date, nothing to do");
                importStatus.setStatus(ImportStatus.Status.SKIPPED);
            } else {
                log.info("Provided page {} exists but the content is not up to date, updating it.", page.getName());
                pageService.updatePageContent(sPageInDb.getId(), providedPageContent, pageZipName);
                importStatus.setStatus(ImportStatus.Status.REPLACED);
            }
        } else {
            log.debug("Page {} was updated by the user, and will not be updated", page.getName());
            importStatus.setStatus(ImportStatus.Status.SKIPPED);
        }
        return importStatus;
    }

    protected List<ImportStatus> importProvidedApplicationsFromClasspath(final String locationPattern,
            final boolean editable, final boolean addIfMissing) throws IOException, ImportException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        Resource[] resources = cpResourceResolver.getResources(locationPattern);
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                log.debug("Found provided applications '{}' in classpath", resourceName);
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    ApplicationZipContent zipContent = ApplicationZipContent.getApplicationZipContent(resourceName,
                            resourceAsStream);
                    importStatuses.addAll(
                            importProvidedApplications(zipContent.getXmlRaw(), zipContent.getIconRaw(),
                                    zipContent.getPngName(), editable, addIfMissing));
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

    protected List<ImportStatus> importProvidedApplications(final byte[] xmlContent, final byte[] iconContent,
            final String iconMimeType, final boolean editable, final boolean addIfMissing)
            throws ImportException, AlreadyExistsException {
        List<ImportStatus> importStatuses = new ArrayList<>();
        ApplicationNodeContainer applicationNodeContainer = applicationImporter.getApplicationNodeContainer(xmlContent);
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            // set the strategy to skip it if a version already exists
            importStatuses.add(
                    applicationImporter.importApplication(applicationNode, editable, SessionService.SYSTEM_ID,
                            iconContent, iconMimeType, addIfMissing, new UpdateNewerNonEditableApplicationStrategy()));
        }
        return importStatuses;
    }

    /**
     * Parses the given list of import statuses of living applications resources to filter those that are marked as
     * SKIPPED and returns the result in a readable format.
     *
     * @param importStatuses list of statuses that result from the import of living applications resources
     * @return a list of non-skipped imported resources in a format that concatenates their name and status
     */
    protected List<String> getNonSkippedImportedResources(final List<ImportStatus> importStatuses) {
        return importStatuses.stream()
                .filter(importStatus -> importStatus.getStatus() != ImportStatus.Status.SKIPPED)
                .map(importStatus -> importStatus.getName() + " " + importStatus.getStatus())
                .collect(Collectors.toList());
    }

}
