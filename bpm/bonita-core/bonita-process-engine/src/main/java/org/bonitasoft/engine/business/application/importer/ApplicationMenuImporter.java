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

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.ApplicationMenuNodeConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ImportException;

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
