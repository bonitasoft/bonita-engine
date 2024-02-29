/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.livingapplication;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class LivingApplicationExporterDelegate {

    private final ApplicationService applicationService;
    private final ApplicationExporter exporter;

    public LivingApplicationExporterDelegate(ApplicationService applicationService, ApplicationExporter exporter) {
        this.applicationService = applicationService;
        this.exporter = exporter;
    }

    public byte[] exportApplications(long... applicationIds) throws ExportException {
        try {
            List<SApplication> applications = new ArrayList<>();
            for (long applicationId : applicationIds) {
                applications.add(applicationService.getApplication(applicationId));
            }
            return exporter.export(applications);
        } catch (SObjectNotFoundException | SBonitaReadException e) {
            throw new ExportException(e);
        }
    }

}
