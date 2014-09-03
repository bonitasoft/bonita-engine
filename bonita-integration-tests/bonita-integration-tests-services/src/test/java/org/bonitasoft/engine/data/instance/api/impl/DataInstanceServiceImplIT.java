/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.instance.DataInstanceServiceTest;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceVisibilityMappingBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.TenantHibernatePersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Emmanuel Duchastenier
 */
public class DataInstanceServiceImplIT extends DataInstanceServiceTest {

    private static DataInstanceServiceImpl dataInstanceServiceImpl;

    @BeforeClass
    public static void setupImplementation() {
        final Recorder recorder = getServicesBuilder().buildRecorder();
        final TenantHibernatePersistenceService persistenceService = getServicesBuilder().buildTenantPersistenceService();
        final TechnicalLoggerService technicalLoggerService = getServicesBuilder().buildTechnicalLoggerService();
        final ArchiveService archiveService = getServicesBuilder().buildArchiveService();
        dataInstanceServiceImpl = new DataInstanceServiceImpl(recorder, persistenceService, archiveService, technicalLoggerService);
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

    @Test
    public void shouldBeAbleToInsertSameDataVisibilityOnDifferentTenant() throws Exception {
        // given:
        final long containerId = 654154L;
        final String containerType = "monProcess";
        final String dataName = "anyData";
        final long dataInstanceId = 95446L;
        final SDataInstanceVisibilityMapping mapping = BuilderFactory.get(SDataInstanceVisibilityMappingBuilderFactory.class)
                .createNewInstance(containerId, containerType, dataName, dataInstanceId).done();
        mapping.setTenantId(1);
        final DataInstanceServiceImpl spy = Mockito.spy(dataInstanceServiceImpl);
        doReturn(mapping).when(spy).createDataInstanceVisibilityMapping(containerId, containerType, dataName, dataInstanceId);

        // when:
        getTransactionService().begin();
        spy.insertDataInstanceVisibilityMapping(containerId, containerType, dataName, dataInstanceId, 0L, true);
        getTransactionService().complete();

        mapping.setTenantId(2);
        getTransactionService().begin();
        spy.insertDataInstanceVisibilityMapping(containerId, containerType, dataName, dataInstanceId, 0L, true);
        getTransactionService().complete();

        // then:
        // no integrity constraint violation
    }

    @Test
    public void deleteLocalArchivedDataInstancesShouldDeleteArchivedContainerAsWell() throws Exception {
        final long containerId = 83;
        final String containerType = DataInstanceContainer.PROCESS_INSTANCE.toString();
        final String instanceName = "kaupunki";

        final SDataInstance dataInstance = buildDataInstance(instanceName, Integer.class.getName(), null, null, containerId,
                containerType, false);

        getTransactionService().begin();
        dataInstanceServiceImpl.createDataInstance(dataInstance);
        dataInstanceServiceImpl.createDataContainer(containerId, containerType, true);
        getTransactionService().complete();

        getTransactionService().begin();
        final List<SDataInstanceVisibilityMapping> dataInstanceVisibilityMappings = dataInstanceServiceImpl.getDataInstanceVisibilityMappings(containerId,
                containerType, 0, 100);
        assertThat(dataInstanceVisibilityMappings).hasSize(1);
        getTransactionService().complete();

        getTransactionService().begin();
        final long id = dataInstanceServiceImpl.getSADataInstanceDataVisibilityMapping(instanceName, containerId,
                containerType);
        assertThat(id).isGreaterThan(0);
        dataInstanceServiceImpl.deleteLocalArchivedDataInstances(containerId, containerType);
        getTransactionService().complete();

        try {
            getTransactionService().begin();
            dataInstanceServiceImpl.getSADataInstanceDataVisibilityMapping(instanceName, containerId, containerType);
            fail("Mapping was not deleted");
        } catch (final SBonitaReadException sbre) {

        } finally {
            getTransactionService().complete();
        }
    }

}
