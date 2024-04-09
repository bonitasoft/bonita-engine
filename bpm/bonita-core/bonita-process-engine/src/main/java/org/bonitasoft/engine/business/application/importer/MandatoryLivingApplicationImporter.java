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
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.PageService;
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
@Slf4j
public class MandatoryLivingApplicationImporter extends LivingApplicationImporter implements TenantLifecycleService {

    private static final String NON_EDITABLE_NON_REMOVABLE_PAGES_PATH = "org/bonitasoft/web/page/final";

    private static final String EDITABLE_NON_REMOVABLE_PAGES_PATH = "org/bonitasoft/web/page/editonly";

    private static final String PROVIDED_FINAL_APPLICATIONS_PATH = "org/bonitasoft/web/application/final";

    private final DefaultLivingApplicationImporter defaultLivingApplicationImporter;

    @Getter
    private boolean firstRun;

    public MandatoryLivingApplicationImporter(final PageService pageService,
            final ApplicationImporter applicationImporter,
            final DefaultLivingApplicationImporter defaultLivingApplicationImporter) {
        super(pageService, applicationImporter);
        this.defaultLivingApplicationImporter = defaultLivingApplicationImporter;
    }

    @Override
    public void init() throws SBonitaException {
        // Step 1: import mandatory pages
        log.info("Importing Bonita mandatory pages");
        importMandatoryPages();
        log.info("Import of Bonita mandatory pages completed");

        // Step 2: import mandatory living apps
        log.info("Importing Bonita mandatory applications");
        importMandatoryApplications();
        log.info("Import of Bonita mandatory applications completed");
    }

    private void importMandatoryPages() {
        try {
            List<ImportStatus> importStatuses = importProvidedNonRemovableNonEditablePagesFromClasspath();

            boolean firstRun = importStatuses.stream().map(ImportStatus::getStatus)
                    .allMatch(importStatus -> importStatus == ImportStatus.Status.ADDED);
            defaultLivingApplicationImporter.setAddRemovablePagesIfMissing(firstRun);

            this.firstRun = firstRun;

            importStatuses.addAll(importProvidedNonRemovableEditablePagesFromClasspath());

            List<String> createdOrReplaced = getNonSkippedImportedResources(importStatuses);
            if (createdOrReplaced.isEmpty()) {
                log.info("No mandatory pages updated");
            } else {
                log.info("Mandatory pages updated or created: {}", createdOrReplaced);
            }
        } catch (BonitaException | IOException e) {
            log.error(ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }

    private List<ImportStatus> importProvidedNonRemovableNonEditablePagesFromClasspath()
            throws BonitaException, IOException {
        // import the provided pages as non-removable, non-editable and add them if they are missing
        return importProvidedPagesFromClasspath(
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/"
                        + NON_EDITABLE_NON_REMOVABLE_PAGES_PATH + "/*.zip",
                false, false, true);
    }

    private List<ImportStatus> importProvidedNonRemovableEditablePagesFromClasspath()
            throws IOException, BonitaException {
        // import the provided pages as non-removable, editable and add them if they are missing
        return importProvidedPagesFromClasspath(
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/"
                        + EDITABLE_NON_REMOVABLE_PAGES_PATH + "/*.zip",
                false, true, true);
    }

    private void importMandatoryApplications() {
        try {
            // import the provided applications as non-editable and add them if they are missing
            List<ImportStatus> importStatuses = importProvidedApplicationsFromClasspath(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/"
                            + PROVIDED_FINAL_APPLICATIONS_PATH + "/*.zip",
                    false, true);

            boolean firstRun = importStatuses.stream().map(ImportStatus::getStatus)
                    .allMatch(status -> status != ImportStatus.Status.SKIPPED);
            defaultLivingApplicationImporter.setAddEditableApplicationsIfMissing(firstRun);

            List<String> createdOrReplaced = getNonSkippedImportedResources(importStatuses);
            if (createdOrReplaced.isEmpty()) {
                log.info("No mandatory applications updated");
            } else {
                log.info("Mandatory applications updated or created: {}", createdOrReplaced);
            }
        } catch (Exception e) {
            log.error("Cannot load provided mandatory applications at startup. Root cause: {}",
                    ExceptionUtils.printRootCauseOnly(e));
            log.debug("Full stack:", e);
        }
    }
}
