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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataInstanceServiceImplTest {

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ParentContainerResolver parentContainerResolver;

    @InjectMocks
    private DataInstanceServiceImpl dataInstanceServiceImpl;

    @Test
    @Ignore("must refactor tests")
    public final void getLastSADataInstanceFromContainer() throws SBonitaException {
        final SADataInstance archiveInstance = mock(SADataInstance.class);
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(archiveInstance).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SADataInstance>> any());

        final SADataInstance dataInstance = dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PROCESS_INSTANCE", parentContainerResolver);
        Assert.assertNotNull(dataInstance);
    }

    @Test(expected = SDataInstanceNotFoundException.class)
    @Ignore("must refactor tests")
    public final void throwExceptionWhentheLastSADataInstanceFromContainerDoesNotExist() throws SBonitaException {
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(null).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SADataInstance>> any());

        dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PRCESS_INSTANCE", parentContainerResolver);
    }

    @Test(expected = SDataInstanceReadException.class)
    public final void getLastSADataInstanceFromContainerThrowsAnExceptionDueToProblemOnPersistenceService() throws SBonitaException {
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doThrow(new SBonitaReadException("moustache")).when(persistenceService).selectOne(Matchers.<SelectOneDescriptor<SADataInstance>> any());
        dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PRCESS_INSTANCE", parentContainerResolver);
    }

    @Test
    public final void getLastSADataInstancesFromContainer() throws SBonitaException {
        final List<SADataInstance> archiveInstances = Collections.emptyList();
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(archiveInstances).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SADataInstance>> any());

        final List<SADataInstance> dataInstances = dataInstanceServiceImpl.getLastLocalSADataInstances(1, "PROCESS_INSTANCE", 0, 10);
        Assert.assertEquals(archiveInstances, dataInstances);
    }

    @Test
    public final void getEmptyLastSADataInstancesFromContainer() throws SBonitaException {
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doReturn(Collections.emptyList()).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SADataInstance>> any());

        final List<SADataInstance> dataInstances = dataInstanceServiceImpl.getLastLocalSADataInstances(1, "PROCESS_INSTANCE", 0, 10);
        Assert.assertEquals(Collections.emptyList(), dataInstances);
    }

    @Test(expected = SDataInstanceReadException.class)
    public final void getLastSADataInstancesFromContainerThrowsAnExceptionDueToProblemOnPersistenceService() throws SBonitaException {
        final List<SADataInstance> archiveInstances = Collections.emptyList();
        doReturn(persistenceService).when(archiveService).getDefinitiveArchiveReadPersistenceService();
        doThrow(new SBonitaReadException("moustache")).when(persistenceService).selectList(Matchers.<SelectListDescriptor<SADataInstance>> any());

        final List<SADataInstance> dataInstances = dataInstanceServiceImpl.getLastLocalSADataInstances(1, "PROCESS_INSTANCE", 0, 10);
        Assert.assertEquals(archiveInstances, dataInstances);
    }

}
