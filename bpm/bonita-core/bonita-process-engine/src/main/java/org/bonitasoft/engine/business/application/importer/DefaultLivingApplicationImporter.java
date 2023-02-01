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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.PageService;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Service used to import default provided living applications at startup, such as:
 * <ul>
 * <li>Bonita Admin Application</li>
 * <li>Bonita User Application</li>
 * </ul>
 */
@Component
@Slf4j
public class DefaultLivingApplicationImporter extends LivingApplicationImporter {

    private static final String EDITABLE_REMOVABLE_PAGES_PATH = "org/bonitasoft/web/page";

    private static final String PROVIDED_REMOVABLE_APPLICATIONS_PATH = "org/bonitasoft/web/application";

    /** Boolean used to import provided removable pages if there are missing (mostly when it is a first install) */
    @Setter
    private boolean addRemovablePagesIfMissing;

    /**
     * Boolean used to import provided editable applications if there are missing (mostly when it is a first install)
     */
    @Setter
    private boolean addEditableApplicationsIfMissing;

    public DefaultLivingApplicationImporter(final PageService pageService,
            final ApplicationImporter applicationImporter) {
        super(pageService, applicationImporter);
    }

    @Override
    public void init() throws SBonitaException {
        // Step 1: import default pages
        log.info("Importing Bonita default pages");
        importDefaultPages();
        log.info("Import of Bonita default pages completed");

        // Step 2: import default living apps
        log.info("Importing Bonita default applications");
        importDefaultApplications();
        log.info("Import of Bonita default applications completed");
    }

    private void importDefaultPages() {
        try {
            if (addRemovablePagesIfMissing) {
                log.info("Detected a first run (a tenant creation or an installation from scratch), "
                        + "importing provided removable pages");
            } else {
                log.info("Updating provided removable pages if they exist and are outdated");
            }
            // import the provided pages as removable and editable
            List<ImportStatus> importStatuses = importProvidedPagesFromClasspath(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + EDITABLE_REMOVABLE_PAGES_PATH + "/*.zip",
                    true, true, addRemovablePagesIfMissing);

            List<String> createdOrReplaced = getNonSkippedImportedResources(importStatuses);
            if (createdOrReplaced.isEmpty()) {
                log.info("No default pages updated");
            } else {
                log.info("Default pages updated or created: {}", createdOrReplaced);
            }
        } catch (BonitaException | IOException e) {
            log.error(ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Stacktrace of the import issue is:", e);
        }
    }

    private void importDefaultApplications() {
        try {
            if (addEditableApplicationsIfMissing) {
                log.info("Detected a first run since a Bonita update, a Bonita upgrade, " +
                        "a tenant creation or an installation from scratch. Importing default applications");
            } else {
                log.info("Updating provided default applications if they exist and are outdated");
            }
            // import the provided applications as editable
            List<ImportStatus> importStatuses = importProvidedApplicationsFromClasspath(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/"
                            + PROVIDED_REMOVABLE_APPLICATIONS_PATH + "/*.zip",
                    true, addEditableApplicationsIfMissing);

            List<String> createdOrReplaced = getNonSkippedImportedResources(importStatuses);
            if (createdOrReplaced.isEmpty()) {
                log.info("No default applications updated");
            } else {
                log.info("Default applications updated or created: {}", createdOrReplaced);
            }
        } catch (Exception e) {
            log.error("Cannot load provided default applications at startup. Root cause: {}",
                    ExceptionUtils.printRootCauseOnly(e));
            log.debug("Full stack:", e);
        }
    }
}
