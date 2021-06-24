/**
 * Copyright (C) 2021 Bonitasoft S.A.
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

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.AlreadyExistsException;

public class UpdateNewerNonEditableApplicationStrategy implements ApplicationImportStrategy {

    private final ApplicationService applicationService;

    UpdateNewerNonEditableApplicationStrategy(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public void whenApplicationExists(SApplication existing, SApplicationWithIcon toBeImported)
            throws AlreadyExistsException, SBonitaException {
        if (existing != null) {
            if (!existing.isEditable() && !existing.getVersion().equals(toBeImported.getVersion())) {
                applicationService.forceDeleteApplication(existing);
            } else {
                throw new AlreadyExistsException(
                        "An application with token '" + existing.getToken() + "' already exists",
                        existing.getToken());
            }
        }
    }

}
