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
package org.bonitasoft.engine.service.platform;

import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.springframework.stereotype.Service;

/**
 * Updates the platform information using directly the {@link PersistenceService}
 *
 * @author Elias Ricken de Medeiros
 */
@Service("platformInformationService")
public class PlatformInformationServiceImpl implements PlatformInformationService {

    private final PersistenceService persistenceService;

    public PlatformInformationServiceImpl(final PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public void updatePlatformInfo(final SPlatform platform, final String platformInfo)
            throws SPlatformUpdateException {

        UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addField(SPlatform.INFORMATION, platformInfo);

        try {
            persistenceService.update(desc);
        } catch (SPersistenceException e) {
            throw new SPlatformUpdateException(e);
        }
    }

}
