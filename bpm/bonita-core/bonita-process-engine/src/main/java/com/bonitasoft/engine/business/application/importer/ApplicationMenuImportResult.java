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
import org.bonitasoft.engine.business.application.model.SApplicationMenu;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuImportResult {

    private final ImportError error;
    private final SApplicationMenu applicationMenu;

    public ApplicationMenuImportResult(ImportError error, SApplicationMenu applicationMenu) {
        this.error = error;
        this.applicationMenu = applicationMenu;
    }

    public ImportError getError() {
        return error;
    }

    public SApplicationMenu getApplicationMenu() {
        return applicationMenu;
    }

}
