/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.importer;

import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationImporter {

    private final ApplicationService applicationService;
    private final ApplicationImportStrategy strategy;
    private ApplicationNodeConverter applicationNodeConverter;
    private ApplicationPageImporter applicationPageImporter;
    private ApplicationMenuImporter applicationMenuImporter;

    public ApplicationImporter(ApplicationService applicationService, ApplicationImportStrategy strategy, ApplicationNodeConverter applicationNodeConverter,
            ApplicationPageImporter applicationPageImporter, ApplicationMenuImporter applicationMenuImporter) {
        this.applicationService = applicationService;
        this.strategy = strategy;
        this.applicationNodeConverter = applicationNodeConverter;
        this.applicationPageImporter = applicationPageImporter;
        this.applicationMenuImporter = applicationMenuImporter;
    }

    public ImportStatus importApplication(ApplicationNode applicationNode, long createdBy) throws ImportException, AlreadyExistsException {
        ImportResult importResult = applicationNodeConverter.toSApplication(applicationNode, createdBy);
        try {
            SApplication application = importApplication(importResult.getApplication());
            importApplicationPages(applicationNode, importResult, application);
            importApplicationMenus(applicationNode, importResult, application);
            updateHomePage(application, applicationNode, createdBy, importResult);
            return importResult.getImportStatus();
        } catch (SBonitaException e) {
            throw new ImportException(e);
        }
    }

    private void updateHomePage(final SApplication application, final ApplicationNode applicationNode, final long createdBy, final ImportResult importResult)
            throws SBonitaException {
        if (applicationNode.getHomePage() != null) {
            try {
                SApplicationPage homePage = applicationService.getApplicationPage(applicationNode.getToken(), applicationNode.getHomePage());
                SApplicationUpdateBuilder updateBuilder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(createdBy);
                updateBuilder.updateHomePageId(homePage.getId());
                applicationService.updateApplication(application, updateBuilder.done());
            } catch (SObjectNotFoundException e) {
                addError(importResult.getImportStatus(), new ImportError(applicationNode.getHomePage(), ImportError.Type.APPLICATION_PAGE));
            }
        }
    }

    private void importApplicationMenus(final ApplicationNode applicationNode, final ImportResult importResult, final SApplication application)
            throws ImportException {
        for (ApplicationMenuNode applicationMenuNode : applicationNode.getApplicationMenus()) {
            List<ImportError> importErrors = applicationMenuImporter.importApplicationMenu(applicationMenuNode, application, null);
            for (ImportError importError : importErrors) {
                addError(importResult.getImportStatus(), importError);
            }
        }
    }

    private void importApplicationPages(final ApplicationNode applicationNode, final ImportResult importResult, final SApplication application)
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

    private SApplication importApplication(SApplication applicationToBeImported) throws SBonitaException, AlreadyExistsException {
        SApplication conflictingApplication = applicationService.getApplicationByToken(applicationToBeImported.getToken());
        if (conflictingApplication != null) {
            strategy.whenApplicationExists(conflictingApplication, applicationToBeImported);
        }
        return applicationService.createApplication(applicationToBeImported);
    }

}
