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
package org.bonitasoft.engine.business.application.importer;

import java.util.*;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationMenuConverter;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ImportException;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
public class ApplicationMenuImporter {

    private ApplicationService applicationService;
    private NodeToApplicationMenuConverter converter;

    public ApplicationMenuImporter(ApplicationService applicationService, NodeToApplicationMenuConverter converter) {
        this.applicationService = applicationService;
        this.converter = converter;
    }

    public List<ImportError> importApplicationMenu(ApplicationMenuNode applicationMenuNode,
            SApplicationWithIcon application,
            SApplicationMenu parentMenu)
            throws ImportException {
        List<ImportError> errors = new ArrayList<ImportError>();
        try {
            ApplicationMenuImportResult importResult = converter.toSApplicationMenu(applicationMenuNode, application,
                    parentMenu);
            if (importResult.getError() == null) {
                SApplicationMenu applicationMenu = applicationService
                        .createApplicationMenu(importResult.getApplicationMenu());
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

    public List<ImportError> importApplicationMenus(List<ApplicationMenuNode> applicationMenus,
            SApplicationWithIcon application) throws ImportException {
        List<ImportError> importErrors = new ArrayList<>();
        for (ApplicationMenuNode applicationMenuNode : applicationMenus) {
            importErrors.addAll(importApplicationMenu(applicationMenuNode, application, null));
        }
        return importErrors;
    }

}
