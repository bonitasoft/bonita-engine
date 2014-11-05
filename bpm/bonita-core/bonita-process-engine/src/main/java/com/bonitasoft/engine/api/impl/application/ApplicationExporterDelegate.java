/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.api.impl.application;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.exporter.ApplicationsExporter;
import com.bonitasoft.engine.business.application.filter.ApplicationsWithIdsFilterBuilder;
import com.bonitasoft.engine.business.application.model.SApplication;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExporterDelegate {

    private final ApplicationService applicationService;
    private final ApplicationsExporter exporter;

    public ApplicationExporterDelegate(ApplicationService applicationService, ApplicationsExporter exporter) {
        this.applicationService = applicationService;
        this.exporter = exporter;
    }

    public byte[] exportApplications(long... applicationIds) throws ExecutionException {
        ApplicationsWithIdsFilterBuilder filterBuilder = new ApplicationsWithIdsFilterBuilder(ArrayUtils.toObject(applicationIds));
        try {
            List<SApplication> applications = applicationService.searchApplications(filterBuilder.buildQueryOptions());
            return exporter.export(applications);
        } catch (SBonitaReadException e) {
            throw new ExecutionException(e);
        }
    }

}
