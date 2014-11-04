/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.api.impl.application;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.business.application.ApplicationExportService;
import com.bonitasoft.engine.business.application.SBonitaExportException;
import org.bonitasoft.engine.exception.ExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExporterDeletegate {

    private ApplicationExportService exportService;

    public ApplicationExporterDeletegate(ApplicationExportService exportService) {
        this.exportService = exportService;
    }

    public byte[] exportApplications(long... applicationIds) throws ExecutionException {
        try {
            return exportService.exportApplications(applicationIds);
        } catch (SBonitaExportException e) {
            throw new ExecutionException(e);
        }
    }

}
