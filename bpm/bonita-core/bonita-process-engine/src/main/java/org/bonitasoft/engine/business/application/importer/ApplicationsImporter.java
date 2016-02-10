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
package org.bonitasoft.engine.business.application.importer;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationsImporter {

    private final ApplicationContainerImporter containerImporter;
    private final ApplicationImporter applicationImporter;

    public ApplicationsImporter(ApplicationContainerImporter containerImporter, ApplicationImporter applicationImporter) {
        this.containerImporter = containerImporter;
        this.applicationImporter = applicationImporter;
    }

    public List<ImportStatus> importApplications(final byte[] xmlContent, long createdBy) throws ImportException, AlreadyExistsException {
        ApplicationNodeContainer applicationNodeContainer = containerImporter.importXML(xmlContent);
        ArrayList<ImportStatus> importStatus = new ArrayList<ImportStatus>(applicationNodeContainer.getApplications().size());
        for (ApplicationNode applicationNode : applicationNodeContainer.getApplications()) {
            importStatus.add(applicationImporter.importApplication(applicationNode, createdBy));
        }
        return importStatus;
    }

}
