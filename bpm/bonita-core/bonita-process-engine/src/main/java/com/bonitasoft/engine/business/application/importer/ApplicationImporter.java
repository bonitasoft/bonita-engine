/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;

import com.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import com.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import com.bonitasoft.engine.business.application.xml.ApplicationNode;
import com.bonitasoft.engine.business.application.xml.ApplicationPageNode;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationImporter {

    private final ApplicationService applicationService;
    private final ApplicationImportStrategy strategy;
    private final ApplicationNodeConverter applicationNodeConverter;
    private final ApplicationPageImporter applicationPageImporter;
    private final ApplicationMenuImporter applicationMenuImporter;

    public ApplicationImporter(final ApplicationService applicationService, final ApplicationImportStrategy strategy,
            final ApplicationNodeConverter applicationNodeConverter,
            final ApplicationPageImporter applicationPageImporter, final ApplicationMenuImporter applicationMenuImporter) {
        this.applicationService = applicationService;
        this.strategy = strategy;
        this.applicationNodeConverter = applicationNodeConverter;
        this.applicationPageImporter = applicationPageImporter;
        this.applicationMenuImporter = applicationMenuImporter;
    }

    public ImportStatus importApplication(final ApplicationNode applicationNode, final long createdBy) throws ImportException, AlreadyExistsException {
        final ImportResult importResult = applicationNodeConverter.toSApplication(applicationNode, createdBy);
        try {
            final SApplication application = importApplication(importResult.getApplication());
            importApplicationPages(applicationNode, importResult, application);
            importApplicationMenus(applicationNode, importResult, application);
            updateHomePage(application, applicationNode, createdBy, importResult);
            return importResult.getImportStatus();
        } catch (final SBonitaException e) {
            throw new ImportException(e);
        }
    }

    private void updateHomePage(final SApplication application, final ApplicationNode applicationNode, final long createdBy, final ImportResult importResult)
            throws SBonitaException {
        if (applicationNode.getHomePage() != null) {
            try {
                final SApplicationPage homePage = applicationService.getApplicationPage(applicationNode.getToken(), applicationNode.getHomePage());
                final SApplicationUpdateBuilder updateBuilder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(createdBy);
                updateBuilder.updateHomePageId(homePage.getId());
                applicationService.updateApplication(application, updateBuilder.done());
            } catch (final SObjectNotFoundException e) {
                addError(importResult.getImportStatus(), new ImportError(applicationNode.getHomePage(), ImportError.Type.APPLICATION_PAGE));
            }
        }
    }

    private void importApplicationMenus(final ApplicationNode applicationNode, final ImportResult importResult, final SApplication application)
            throws ImportException {
        for (final ApplicationMenuNode applicationMenuNode : applicationNode.getApplicationMenus()) {
            final List<ImportError> importErrors = applicationMenuImporter.importApplicationMenu(applicationMenuNode, application, null);
            for (final ImportError importError : importErrors) {
                addError(importResult.getImportStatus(), importError);
            }
        }
    }

    private void importApplicationPages(final ApplicationNode applicationNode, final ImportResult importResult, final SApplication application)
            throws ImportException {
        for (final ApplicationPageNode applicationPageNode : applicationNode.getApplicationPages()) {
            final ImportError importError = applicationPageImporter.importApplicationPage(applicationPageNode, application);
            addError(importResult.getImportStatus(), importError);
        }
    }

    private void addError(final ImportStatus importStatus, final ImportError importError) {
        if (importError != null && !importStatus.getErrors().contains(importError)) {
            importStatus.addError(importError);
        }
    }

    private SApplication importApplication(final SApplication applicationToBeImported) throws SBonitaException, AlreadyExistsException {
        final SApplication conflictingApplication = applicationService.getApplicationByToken(applicationToBeImported.getToken());
        if (conflictingApplication != null) {
            strategy.whenApplicationExists(conflictingApplication, applicationToBeImported);
        }
        return applicationService.createApplication(applicationToBeImported);
    }

}
