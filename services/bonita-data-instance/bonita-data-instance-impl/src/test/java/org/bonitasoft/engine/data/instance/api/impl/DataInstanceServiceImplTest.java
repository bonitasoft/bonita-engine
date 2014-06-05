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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class DataInstanceServiceImplTest {

    private Recorder recorder;

    private ReadPersistenceService persistenceService;

    private ArchiveService archiveService;

    private TechnicalLoggerService logger;

    private DataInstanceServiceImpl dataInstanceServiceImpl;

    @Before
    public void setUp() {
        persistenceService = mock(ReadPersistenceService.class);
        recorder = mock(Recorder.class);
        logger = mock(TechnicalLoggerService.class);
        archiveService = mock(ArchiveService.class);
        dataInstanceServiceImpl = new DataInstanceServiceImpl(recorder, persistenceService,
                archiveService, logger);
    }

    // /**
    // * Test method for {@link org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl#getDataInstance(long)}.
    // *
    // * @throws SDataInstanceException
    // * @throws SDataException
    // * @throws SDataSourceInactiveException
    // * @throws SDataSourceInitializationException
    // * @throws SDataSourceNotFoundException
    // */
    // @Test
    // public final void getTransientDataInstanceById() throws SDataInstanceException, SDataSourceNotFoundException, SDataSourceInitializationException,
    // SDataSourceInactiveException, SDataException {
    // final long dataInstanceId = 456L;
    // final SDataSource dataSource = mock(SDataSource.class);
    // doReturn(dataSource).when(dataSourceService).getDataSource(anyString(), anyString());
    // final DataInstanceDataSource dataInstanceDataSource = mock(DataInstanceDataSource.class);
    // doReturn(dataInstanceDataSource).when(dataSourceService).getDataSourceImplementation(eq(DataInstanceDataSource.class), anyLong());
    // final SDataInstance sDataInstance = mock(SDataInstance.class);
    // doReturn(sDataInstance).when(dataInstanceDataSource).getDataInstance(dataInstanceId);
    //
    // Assert.assertEquals(sDataInstance, dataInstanceServiceImpl.getDataInstance(dataInstanceId));
    // }
    //
    // @Test
    // public final void getDefaultDataInstanceById() throws SDataInstanceException, SDataSourceNotFoundException, SDataSourceInitializationException,
    // SDataSourceInactiveException, SDataException {
    // final long dataInstanceId = 456L;
    // // Throw exception when get transient data
    // final SDataSource transientSDataSource = mock(SDataSource.class);
    // doReturn(1L).when(transientSDataSource).getId();
    // doReturn(transientSDataSource).when(dataSourceService).getDataSource(anyString(),
    // anyString());
    // final DataInstanceDataSource dataInstanceDataSource = mock(DataInstanceDataSource.class);
    // doReturn(dataInstanceDataSource).when(dataSourceService).getDataSourceImplementation(DataInstanceDataSource.class, 1L);
    // doThrow(new SDataInstanceReadException("plop")).when(dataInstanceDataSource).getDataInstance(dataInstanceId);
    //
    // // Get data instance in database
    // final SDataSource defaultSDataSource = mock(SDataSource.class);
    // doReturn(defaultSDataSource).when(dataSourceService).getDataSource(DataInstanceServiceImpl.DEFAULT_DATA_SOURCE,
    // DataInstanceServiceImpl.DATA_SOURCE_VERSION);
    // doReturn(2L).when(transientSDataSource).getId();
    // final DataInstanceDataSource dataInstanceDataSource2 = mock(DataInstanceDataSource.class);
    // doReturn(dataInstanceDataSource2).when(dataSourceService).getDataSourceImplementation(eq(DataInstanceDataSource.class), anyLong());
    // final SDataInstance sDataInstance = mock(SDataInstance.class);
    // doReturn(sDataInstance).when(dataInstanceDataSource2).getDataInstance(dataInstanceId);
    //
    // Assert.assertEquals(sDataInstance, dataInstanceServiceImpl.getDataInstance(dataInstanceId));
    // }
    //
    // @Test(expected = SDataInstanceReadException.class)
    // public final void getDataInstanceByIdNotExists() throws SDataInstanceException, SDataSourceNotFoundException, SDataSourceInitializationException,
    // SDataSourceInactiveException, SDataException {
    // final long dataInstanceId = 456L;
    // final SDataSource transientSDataSource = mock(SDataSource.class);
    // doReturn(transientSDataSource).when(dataSourceService).getDataSource(anyString(), anyString());
    // final DataInstanceDataSource dataInstanceDataSource = mock(DataInstanceDataSource.class);
    // doReturn(dataInstanceDataSource).when(dataSourceService).getDataSourceImplementation(eq(DataInstanceDataSource.class), anyLong());
    // doThrow(new SDataInstanceReadException("plop")).when(dataInstanceDataSource).getDataInstance(dataInstanceId);
    //
    // dataInstanceServiceImpl.getDataInstance(dataInstanceId);
    // }

    @Test
    public final void getLastSADataInstanceFromContainer() throws SBonitaException {
        final SADataInstance archiveInstance = mock(SADataInstance.class);
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(archiveInstance).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        final SADataInstance dataInstance = dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PROCESS_INSTANCE");
        Assert.assertNotNull(dataInstance);
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    public final void throwExceptionWhentheLastSADataInstanceFromContainerDoesNotExist() throws SBonitaException {
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(null).when(persistenceService).selectOne(any(SelectOneDescriptor.class));

        dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PRCESS_INSTANCE");
    }

    @Test(expected = SDataInstanceReadException.class)
    public final void getLastSADataInstanceFromContainerThrowsAnExceptionDueToProblemOnPersistenceService() throws SBonitaException {
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doThrow(new SBonitaReadException("moustache")).when(persistenceService).selectOne(any(SelectOneDescriptor.class));
        dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PRCESS_INSTANCE");
    }

    @Test
    public final void getLastSADataInstancesFromContainer() throws SBonitaException {
        final List<SADataInstance> archiveInstances = mock(List.class);
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(archiveInstances).when(persistenceService).selectList(any(SelectListDescriptor.class));

        final List<SADataInstance> dataInstances = dataInstanceServiceImpl.getLastLocalSADataInstances(1, "PROCESS_INSTANCE", 0, 10);
        Assert.assertEquals(archiveInstances, dataInstances);
    }

    @Test
    public final void getEmptyLastSADataInstancesFromContainer() throws SBonitaException {
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(Collections.emptyList()).when(persistenceService).selectList(any(SelectListDescriptor.class));

        final List<SADataInstance> dataInstances = dataInstanceServiceImpl.getLastLocalSADataInstances(1, "PROCESS_INSTANCE", 0, 10);
        Assert.assertEquals(Collections.emptyList(), dataInstances);
    }

    @Test(expected = SDataInstanceReadException.class)
    public final void getLastSADataInstancesFromContainerThrowsAnExceptionDueToProblemOnPersistenceService() throws SBonitaException {
        final List<SADataInstance> archiveInstances = mock(List.class);
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doThrow(new SBonitaReadException("moustache")).when(persistenceService).selectList(any(SelectListDescriptor.class));

        final List<SADataInstance> dataInstances = dataInstanceServiceImpl.getLastLocalSADataInstances(1, "PROCESS_INSTANCE", 0, 10);
        Assert.assertEquals(archiveInstances, dataInstances);
    }

}
