/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ExecutionException;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.converter.ApplicationContainerConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationImporter {

    private final ApplicationService applicationService;
    private final ApplicationImportStrategy strategy;
    private final ApplicationContainerImporter importer;
    private ApplicationContainerConverter containerConverter;

    public ApplicationImporter(ApplicationService applicationService, ApplicationImportStrategy strategy, ApplicationContainerImporter importer,
            ApplicationContainerConverter containerConverter) {
        this.applicationService = applicationService;
        this.strategy = strategy;
        this.importer = importer;
        this.containerConverter = containerConverter;
    }

    public List<ImportStatus> importApplications(final byte[] xmlContent, long createdBy) throws ExecutionException {
        ApplicationNodeContainer applicationNodeContainer = importer.importXML(xmlContent);
        List<SApplication> applications = containerConverter.toSApplications(applicationNodeContainer, createdBy);
        try {
            for (SApplication applicationToBeImported : applications) {
                importApplication(applicationToBeImported);
            }
            return Collections.emptyList();
        } catch (SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    private void importApplication(SApplication applicationToBeImported) throws SBonitaException, ExecutionException {
        SApplication conflictingApplication = applicationService.getApplicationByToken(applicationToBeImported.getToken());
        if (conflictingApplication != null) {
            strategy.whenApplicationExists(conflictingApplication, applicationToBeImported);
        }
        applicationService.createApplication(applicationToBeImported);
    }

}
