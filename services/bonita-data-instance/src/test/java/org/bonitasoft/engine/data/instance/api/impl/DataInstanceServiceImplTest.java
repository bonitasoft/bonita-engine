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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.instance.api.DataContainer;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.impl.SALongTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAShortTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SLongTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    @Captor
    private ArgumentCaptor<ArchiveInsertRecord> archiveInsertRecordArgumentCaptor;
    @InjectMocks
    private DataInstanceServiceImpl dataInstanceServiceImpl;

    @Test(expected = SDataInstanceReadException.class)
    public final void should_throw_read_exception_when_persistence_service_has_read_exception() throws SBonitaException {
        dataInstanceServiceImpl.getLastSADataInstance("kaupunki", 1, "PROCESS_INSTANCE", parentContainerResolver);
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

    @Test
    public final void should_archive_the_first_value_of_a_data_when_creating_it() throws Exception {
        //given
        SShortTextDataInstanceImpl dataInstance = new SShortTextDataInstanceImpl();
        dataInstance.setValue("theValue");
        //when
        dataInstanceServiceImpl.createDataInstance(dataInstance);
        //then
        verify(archiveService).recordInsert(anyLong(), archiveInsertRecordArgumentCaptor.capture());
        final SAShortTextDataInstanceImpl entity = (SAShortTextDataInstanceImpl) archiveInsertRecordArgumentCaptor.getValue().getEntity();
        assertThat(entity.getValue()).isEqualTo("theValue");
    }

    @Test
    public final void should_archive_new_value_on_data_value_update() throws Exception {
        //given
        SShortTextDataInstanceImpl dataInstance = new SShortTextDataInstanceImpl();
        //set directly to "theNewValue" because the set is done by the persistence service
        dataInstance.setValue("theNewValue");
        EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField("value", "theNewValue");
        //when
        dataInstanceServiceImpl.updateDataInstance(dataInstance, updateDescriptor);
        //then
        verify(archiveService).recordInsert(anyLong(), archiveInsertRecordArgumentCaptor.capture());
        final SAShortTextDataInstanceImpl entity = (SAShortTextDataInstanceImpl) archiveInsertRecordArgumentCaptor.getValue().getEntity();
        assertThat(entity.getValue()).isEqualTo("theNewValue");
    }

    @Test
    public void should_return_the_last_version_of_an_archived_data_for_long_running_process() throws Exception {
        //see bug BS-15990
        List<SADataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(createArchDataInstance(1, 66L, "TASK", 1475800000000L, "VALUE1"));
        dataInstances.add(createArchDataInstance(2, 66L, "TASK", 1478080000000L, "VALUE2"));
        dataInstances.add(createArchDataInstance(3, 66L, "TASK", 1478081000000L, "VALUE3"));
        dataInstances.add(createArchDataInstance(4, 66L, "TASK", 1478087736549L, "VALUE4"));
        doReturn(dataInstances).when(persistenceService).selectList(any(SelectListDescriptor.class));
        //order the retrieved list by container level and by archive date
        SADataInstance dataInstance = dataInstanceServiceImpl.getLastSADataInstance("testData", 1L, "TASK", parentContainerResolver);
        //should return the last version
        assertThat(dataInstance.getValue()).isEqualTo("VALUE4");
    }

    @Test
    public void should_return_the_last_version_of_an_archived_data() throws Exception {
        //see bug BS-15990
        List<SADataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(createArchDataInstance(1, 66L, "TASK", 1475800000000L, "VALUE1"));
        dataInstances.add(createArchDataInstance(2, 66L, "TASK", 1475800000001L, "VALUE2"));
        dataInstances.add(createArchDataInstance(3, 66L, "TASK", 1475800000002L, "VALUE3"));
        dataInstances.add(createArchDataInstance(4, 66L, "TASK", 1475800000003L, "VALUE4"));
        doReturn(dataInstances).when(persistenceService).selectList(any(SelectListDescriptor.class));
        //order the retrieved list by container level and by archive date
        SADataInstance dataInstance = dataInstanceServiceImpl.getLastSADataInstance("testData", 1L, "TASK", parentContainerResolver);
        //should return the last version
        assertThat(dataInstance.getValue()).isEqualTo("VALUE4");
    }

    @Test
    public void should_return_the_archived_data_in_the_up_most_container() throws Exception {
        //given
        List<SADataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(createArchDataInstance(1, 66L, "PROC", 1475800000002L, "PROC_VALUE"));
        dataInstances.add(createArchDataInstance(2, 67L, "TASK", 1475800000001L, "TASK_VALUE"));
        dataInstances.add(createArchDataInstance(2, 68L, "SUBTASK", 1475800000000L, "SUBTASK_VALUE"));
        doReturn(dataInstances).when(persistenceService).selectList(any(SelectListDescriptor.class));
        doReturn(Arrays.asList(new DataContainer(68L, "SUBTASK"), new DataContainer(67L, "TASK"), new DataContainer(66L, "PROC"))).when(parentContainerResolver)
                .getArchivedContainerHierarchy(new DataContainer(68L, "SUBTASK"));
        //when
        SADataInstance dataInstance = dataInstanceServiceImpl.getLastSADataInstance("testData", 68L, "SUBTASK", parentContainerResolver);
        //then
        assertThat(dataInstance.getValue()).isEqualTo("SUBTASK_VALUE");
    }

    private SALongTextDataInstanceImpl createArchDataInstance(long id, long containerId, String containerType, long archiveDate, String value) {
        SALongTextDataInstanceImpl dataInstance = new SALongTextDataInstanceImpl();
        dataInstance.setId(id);
        dataInstance.setContainerId(containerId);
        dataInstance.setContainerType(containerType);
        dataInstance.setArchiveDate(archiveDate);
        dataInstance.setValue(value);
        return dataInstance;
    }

    private SLongTextDataInstanceImpl createDataInstance(long id, long containerId, String containerType, String value) {
        SLongTextDataInstanceImpl dataInstance = new SLongTextDataInstanceImpl();
        dataInstance.setId(id);
        dataInstance.setContainerId(containerId);
        dataInstance.setContainerType(containerType);
        dataInstance.setValue(value);
        return dataInstance;
    }

    @Test
    public void should_return_the_data_in_the_up_most_container() throws Exception {
        //given
        List<SDataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(createDataInstance(1, 66L, "PROC", "PROC_VALUE"));
        dataInstances.add(createDataInstance(2, 67L, "TASK", "TASK_VALUE"));
        dataInstances.add(createDataInstance(2, 68L, "SUBTASK", "SUBTASK_VALUE"));
        doReturn(dataInstances).when(persistenceService).selectList(any(SelectListDescriptor.class));
        doReturn(Arrays.asList(new DataContainer(68L, "SUBTASK"), new DataContainer(67L, "TASK"), new DataContainer(66L, "PROC"))).when(parentContainerResolver)
                .getContainerHierarchy(new DataContainer(68L, "SUBTASK"));
        //when
        SDataInstance dataInstance = dataInstanceServiceImpl.getDataInstance("testData", 68L, "SUBTASK", parentContainerResolver);
        //then
        assertThat(dataInstance.getValue()).isEqualTo("SUBTASK_VALUE");
    }
}
