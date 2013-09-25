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

import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class DataInstanceServiceImplTest {

    private DataService dataSourceService;

    private SDataInstanceBuilders dataInstanceBuilders;

    private Recorder recorder;

    private SEventBuilders eventBuilders;

    private ReadPersistenceService persistenceService;

    private ArchiveService archiveService;

    private TechnicalLoggerService logger;

    private DataInstanceServiceImpl dataInstanceServiceImpl;

    @Before
    public void setUp() throws Exception {
        dataSourceService = mock(DataService.class);
        persistenceService = mock(ReadPersistenceService.class);
        recorder = mock(Recorder.class);
        eventBuilders = mock(SEventBuilders.class);
        logger = mock(TechnicalLoggerService.class);
        archiveService = mock(ArchiveService.class);
        dataInstanceServiceImpl = new DataInstanceServiceImpl(dataSourceService, dataInstanceBuilders, recorder, eventBuilders, persistenceService,
                archiveService, logger);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#createDataInstance(org.bonitasoft.engine.data.instance.model.SDataInstance)}.
     */
    @Test
    public final void createDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#updateDataInstance(org.bonitasoft.engine.data.instance.model.SDataInstance, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#deleteDataInstance(org.bonitasoft.engine.data.instance.model.SDataInstance)}.
     */
    @Test
    public final void deleteDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getDataInstance(long)}.
     */
    @Test
    public final void getDataInstanceById() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getDataInstance(java.lang.String, long, java.lang.String)}.
     */
    @Test
    public final void getDataInstanceByNameAndContainerIdAndType() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getDataInstances(long, java.lang.String, int, int)}.
     */
    @Test
    public final void getPaginatedDataInstancesByContainerIdAndType() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getLocalDataInstance(java.lang.String, long, java.lang.String)}.
     */
    @Test
    public final void getLocalDataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getLocalDataInstances(long, java.lang.String, int, int)}.
     */
    @Test
    public final void getLocalDataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#addChildContainer(long, java.lang.String, long, java.lang.String)}.
     */
    @Test
    public final void addChildContainer() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#removeContainer(long, java.lang.String)}.
     */
    @Test
    public final void removeContainer() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#createDataContainer(long, java.lang.String)}.
     */
    @Test
    public final void createDataContainer() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#insertMappingForLocalElement(long, java.lang.String)}.
     */
    @Test
    public final void insertMappingForLocalElement() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#insertDataInstanceVisibilityMapping(long, java.lang.String, java.lang.String, long, long)}
     * .
     */
    @Test
    public final void insertDataInstanceVisibilityMapping() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getSADataInstance(long, java.lang.String, java.lang.String, long)}.
     */
    @Test
    public final void getSADataInstanceByContainerIdAndTypeAndNameAndTime() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getSADataInstance(long, long)}.
     */
    @Test
    public final void getSADataInstanceBySourceObjectIdAndTime() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getSADataInstances(long)}.
     */
    @Test
    public final void getSADataInstancesByDataInstanceId() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getLastSADataInstance(long)}.
     */
    @Test
    public final void getLastSADataInstance() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getNumberOfDataInstances(long, org.bonitasoft.engine.data.instance.api.DataInstanceContainer)}
     * .
     */
    @Test
    public final void getNumberOfDataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getDataInstances(java.util.List, long, java.lang.String)}.
     */
    @Test
    public final void getDataInstancesListOfStringLongString() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getSADataInstances(long, java.lang.String, java.util.List, long)}.
     */
    @Test
    public final void getSADataInstancesByContainerIdAndTypeAndNamesAndTime() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getLocalSADataInstances(long, java.lang.String, int, int)}.
     */
    @Test
    public final void getLocalSADataInstances() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#deleteSADataInstance(org.bonitasoft.engine.data.instance.model.archive.SADataInstance)}
     * .
     */
    @Test
    public final void deleteSADataInstance() {
        // TODO : Not yet implemented
    }

}
