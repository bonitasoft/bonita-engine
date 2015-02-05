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
package org.bonitasoft.engine.data.instance.api.impl;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.instance.DataInstanceServiceTest;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.TenantHibernatePersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.BeforeClass;

/**
 * @author Emmanuel Duchastenier
 */
public class DataInstanceServiceImplIT extends DataInstanceServiceTest {

    private static DataInstanceServiceImpl dataInstanceServiceImpl;

    @BeforeClass
    public static void setupImplementation() {
        final Recorder recorder = getServicesBuilder().buildTenantRecorder();
        final TenantHibernatePersistenceService persistenceService = getServicesBuilder().buildTenantPersistenceService();
        final TechnicalLoggerService technicalLoggerService = getServicesBuilder().buildTechnicalLoggerService();
        final ArchiveService archiveService = getServicesBuilder().buildArchiveService();
        parentContainerResolver = getServicesBuilder().buildParentContainerResolver();
        dataInstanceServiceImpl = new DataInstanceServiceImpl(recorder, persistenceService, archiveService,
                technicalLoggerService);
        final CacheService cacheService = getServicesBuilder().buildCacheService();
        try {
            cacheService.start();
        } catch (final SBonitaException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataInstanceService getDataInstanceServiceImplementation() {
        return dataInstanceServiceImpl;
    }

}
