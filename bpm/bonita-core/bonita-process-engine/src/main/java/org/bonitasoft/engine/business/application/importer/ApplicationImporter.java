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

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.exporter.ApplicationNodeContainerConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.business.application.xml.AbstractApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
@Slf4j
public class ApplicationImporter {

    private final ApplicationService applicationService;
    private final NodeToApplicationConverter nodeToApplicationConverter;
    private final ApplicationPageImporter applicationPageImporter;
    private final ApplicationMenuImporter applicationMenuImporter;

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

    public ImportStatus importApplication(AbstractApplicationNode applicationNode, boolean editable, long createdBy,
            byte[] iconContent, String iconMimeType, boolean addIfMissing, ApplicationImportStrategy strategy)
            throws ImportException, AlreadyExistsException {
        try {
            ImportResult importResult = nodeToApplicationConverter.toSApplication(applicationNode, iconContent,
                    iconMimeType, createdBy, editable);

            SApplicationWithIcon applicationToBeImported = importResult.getApplication();
            ImportStatus importStatus = importResult.getImportStatus();

            importStatus.setStatus(ImportStatus.Status.ADDED);

            // import status will change depending on the chosen strategy, or will throw exception
            applyStrategyWhenApplicationExists(strategy, applicationToBeImported, importStatus);

            if (!addIfMissing) {
                importStatus.setStatus(ImportStatus.Status.SKIPPED);
            }

            if (importStatus.getStatus() != ImportStatus.Status.SKIPPED) {
                applicationService.createApplication(applicationToBeImported);

                // import more elements for applications built with legacy UID
                if (applicationNode instanceof ApplicationNode legacy) {
                    importStatus.addErrorsIfNotExists(
                            applicationPageImporter
                                    .importApplicationPages(legacy.getApplicationPages(), applicationToBeImported));
                    importStatus.addErrorsIfNotExists(
                            applicationMenuImporter
                                    .importApplicationMenus(legacy.getApplicationMenus(), applicationToBeImported));
                    updateHomePage(applicationToBeImported, legacy, createdBy, importResult);
                }
            }

            return importStatus;
        } catch (SBonitaException e) {
            throw new ImportException(e);
        }
    }

    private void applyStrategyWhenApplicationExists(ApplicationImportStrategy strategy,
            SApplicationWithIcon applicationToBeImported, ImportStatus importStatus)
            throws SBonitaReadException, AlreadyExistsException, SObjectModificationException {
        SApplication conflictingApplication = applicationService
                .getApplicationByToken(applicationToBeImported.getToken());
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
    }

    public List<ImportStatus> importApplications(final byte[] xmlContent, byte[] iconContent, String iconMimeType,
            long createdBy, ApplicationImportStrategy strategy) throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = getApplicationNodeContainer(xmlContent);
        List<ImportStatus> importStatus = new ArrayList<>();
        for (AbstractApplicationNode applicationNode : applicationNodeContainer.getAllApplications()) {
            importStatus.add(
                    importApplication(applicationNode, true, createdBy, iconContent, iconMimeType, true, strategy));
        }
        return importStatus;
    }

    public ApplicationNodeContainer getApplicationNodeContainer(byte[] xmlContent) throws ImportException {
        try {
            return new ApplicationNodeContainerConverter().unmarshallFromXML(xmlContent);
        } catch (final Exception e) {
            throw new ImportException(e);
        }
    }
}
