/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.importer;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.model.SApplicationPage;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationPageImportResult {

    private final SApplicationPage applicationPage;
    private final ImportError error;

    public ApplicationPageImportResult(SApplicationPage applicationPage, ImportError error) {
        this.applicationPage = applicationPage;
        this.error = error;
    }

    public SApplicationPage getApplicationPage() {
        return applicationPage;
    }

    public ImportError getError() {
        return error;
    }
}
