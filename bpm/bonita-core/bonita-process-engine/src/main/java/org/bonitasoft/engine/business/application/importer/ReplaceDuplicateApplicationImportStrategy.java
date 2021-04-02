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

import static java.lang.String.format;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;

/**
 * @author Pascal Garcia
 * @author Emmanuel Duchastenier
 */
public class ReplaceDuplicateApplicationImportStrategy implements ApplicationImportStrategy {

    private ApplicationService applicationService;

    ReplaceDuplicateApplicationImportStrategy(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public void whenApplicationExists(SApplication existing, SApplicationWithIcon toBeImported)
            throws SBonitaException {
        try {
            applicationService.deleteApplication(existing.getId());
        } catch (SObjectModificationException | SObjectNotFoundException e) {
            throw new SDeletionException(
                    format("Existing application '%s' cannot be deleted when replacing with newer version",
                            existing.getDisplayName()));
        }
    }

}
