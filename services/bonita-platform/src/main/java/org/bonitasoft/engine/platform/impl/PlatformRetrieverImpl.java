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

package org.bonitasoft.engine.platform.impl;

import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformRetrieverImpl implements PlatformRetriever {

    private final PersistenceService platformPersistenceService;

    private static final String QUERY_GET_PLATFORM = "getPlatform";

    public PlatformRetrieverImpl(final PersistenceService platformPersistenceService) {
        this.platformPersistenceService = platformPersistenceService;
    }

    @Override
    public SPlatform getPlatform() throws SPlatformNotFoundException {
        try {
            SPlatform platform = platformPersistenceService.selectOne(new SelectOneDescriptor<SPlatform>(QUERY_GET_PLATFORM, null, SPlatform.class));
            if (platform == null) {
                throw new SPlatformNotFoundException("No platform found");
            }
            return platform;
        } catch (final SBonitaReadException e) {
            throw new SPlatformNotFoundException("Unable to check if a platform already exists : " + e.getMessage(), e);
        }
    }

}
