/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl.application;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.filter.ApplicationsWithIdsFilterBuilder;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationExporterDelegate {

    private final ApplicationService applicationService;
    private final ApplicationExporter exporter;

    public ApplicationExporterDelegate(ApplicationService applicationService, ApplicationExporter exporter) {
        this.applicationService = applicationService;
        this.exporter = exporter;
    }

    public byte[] exportApplications(long... applicationIds) throws ExportException {
        ApplicationsWithIdsFilterBuilder filterBuilder = new ApplicationsWithIdsFilterBuilder(ArrayUtils.toObject(applicationIds));
        try {
            List<SApplication> applications = applicationService.searchApplications(filterBuilder.buildQueryOptions());
            return exporter.export(applications);
        } catch (SBonitaReadException e) {
            throw new ExportException(e);
        }
    }

}
