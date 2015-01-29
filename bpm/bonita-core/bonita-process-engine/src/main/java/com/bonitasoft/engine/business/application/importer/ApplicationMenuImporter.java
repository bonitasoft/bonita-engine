/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ImportException;

import com.bonitasoft.engine.business.application.converter.ApplicationMenuNodeConverter;
import com.bonitasoft.engine.business.application.xml.ApplicationMenuNode;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuImporter {

    private ApplicationService applicationService;
    private ApplicationMenuNodeConverter converter;

    public ApplicationMenuImporter(ApplicationService applicationService, ApplicationMenuNodeConverter converter) {
        this.applicationService = applicationService;
        this.converter = converter;
    }

    public List<ImportError> importApplicationMenu(ApplicationMenuNode applicationMenuNode, SApplication application, SApplicationMenu parentMenu)
            throws ImportException {
        List<ImportError> errors = new ArrayList<ImportError>();
        try {
            ApplicationMenuImportResult importResult = converter.toSApplicationMenu(applicationMenuNode, application, parentMenu);
            if (importResult.getError() == null) {
                SApplicationMenu applicationMenu = applicationService.createApplicationMenu(importResult.getApplicationMenu());
                for (ApplicationMenuNode subMenuNode : applicationMenuNode.getApplicationMenus()) {
                    errors.addAll(importApplicationMenu(subMenuNode, application, applicationMenu));
                }
            } else {
                errors.add(importResult.getError());
            }
            return errors;
        } catch (SBonitaException e) {
            throw new ImportException(e);
        }
    }
}
