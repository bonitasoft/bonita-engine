/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ExecutionException;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
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

    public ApplicationImporter(ApplicationService applicationService, ApplicationImportStrategy strategy, ApplicationNodeConverter applicationNodeConverter,
            ApplicationPageImporter applicationPageImporter) {
        this.applicationService = applicationService;
        this.strategy = strategy;
        this.applicationNodeConverter = applicationNodeConverter;
        this.applicationPageImporter = applicationPageImporter;
    }

    public ImportStatus importApplication(ApplicationNode applicationNode, long createdBy) throws ExecutionException {
        ImportResult importResult = applicationNodeConverter.toSApplication(applicationNode, createdBy);
        try {
            SApplication application = importApplication(importResult.getApplication());
            for (ApplicationPageNode applicationPageNode : applicationNode.getApplicationPages()) {
                importApplicationPage(importResult, application, applicationPageNode);
            }
            return importResult.getImportStatus();
        } catch (SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    private void importApplicationPage(final ImportResult importResult, final SApplication application, final ApplicationPageNode applicationPageNode) throws ExecutionException {
        ImportError importError = applicationPageImporter.importApplicationPage(application, applicationPageNode);
        if(importError != null) {
            importResult.getImportStatus().addError(importError);
        }
    }

    private SApplication importApplication(SApplication applicationToBeImported) throws SBonitaException, ExecutionException {
        SApplication conflictingApplication = applicationService.getApplicationByToken(applicationToBeImported.getToken());
        if (conflictingApplication != null) {
            strategy.whenApplicationExists(conflictingApplication, applicationToBeImported);
        }
        return applicationService.createApplication(applicationToBeImported);
    }

}
