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
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import com.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import com.bonitasoft.engine.business.application.xml.ApplicationNode;
import com.bonitasoft.engine.business.application.xml.ApplicationPageNode;

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
                importResult.getImportStatus().addError(new ImportError(applicationNode.getHomePage(), ImportError.Type.APPLICATION_PAGE));
            }
        }
    }

    private void importApplicationMenus(final ApplicationNode applicationNode, final ImportResult importResult, final SApplication application)
            throws ImportException {
        for (ApplicationMenuNode applicationMenuNode : applicationNode.getApplicationMenus()) {
            List<ImportError> importErrors = applicationMenuImporter.importApplicationMenu(applicationMenuNode, application, null);
            importResult.getImportStatus().addErrors(importErrors);
        }
    }

    private void importApplicationPages(final ApplicationNode applicationNode, final ImportResult importResult, final SApplication application)
            throws ImportException {
        for (ApplicationPageNode applicationPageNode : applicationNode.getApplicationPages()) {
            ImportError importError = applicationPageImporter.importApplicationPage(applicationPageNode, application);
            if (importError != null) {
                importResult.getImportStatus().addError(importError);
            }
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
