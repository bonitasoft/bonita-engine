/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import java.util.List;

import com.bonitasoft.engine.business.application.ApplicationExportService;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SBonitaExportException;
import com.bonitasoft.engine.business.application.impl.exporter.ApplicationsExporter;
import com.bonitasoft.engine.business.application.impl.filter.ApplicationsWithIdsFilterBuilder;
import com.bonitasoft.engine.business.application.model.SApplication;
import org.apache.commons.lang3.ArrayUtils;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExportServiceImpl implements ApplicationExportService {

    private final ApplicationService applicationService;
    private final ApplicationsExporter exporter;

    public ApplicationExportServiceImpl(ApplicationService applicationService, ApplicationsExporter exporter) {
        this.applicationService = applicationService;
        this.exporter = exporter;
    }


    @Override
    public byte[] exportApplications(long... applicationIds) throws SBonitaExportException {
        ApplicationsWithIdsFilterBuilder filterBuilder = new ApplicationsWithIdsFilterBuilder(ArrayUtils.toObject(applicationIds));
        try {
            List<SApplication> applications = applicationService.searchApplications(filterBuilder.buildQueryOptions());
            return exporter.export(applications);
        } catch (SBonitaReadException e) {
            throw new SBonitaExportException(e);
        }
    }

}
