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
package org.bonitasoft.engine.core.contract.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractDataServiceImplTest {

    @Mock
    private ReadPersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private ArchiveService archiveService;

    @InjectMocks
    private ContractDataServiceImpl contractDataService;

    @Before
    public void setUp() {
        when(archiveService.getDefinitiveArchiveReadPersistenceService()).thenReturn(persistenceService);
    }

    @Test
    public void getUserTaskData_returns_the_stored_value() throws Exception {
        final STaskContractData contractData = new STaskContractData(1983L, "id", 10L);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(contractData);

        final Long id = (Long) contractDataService.getUserTaskDataValue(1983L, "id");

        assertThat(id).isEqualTo(10L);
    }

    @Test(expected = SContractDataNotFoundException.class)
    public void getUserTaskData_throws_an_exception_when_data_not_found() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        contractDataService.getUserTaskDataValue(1983L, "id");
    }

    @Test(expected = SBonitaReadException.class)
    public void getUserTaskData_throws_an_exception_with_a_read_exception() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.getUserTaskDataValue(1983L, "id");
    }

    @Test
    public void addUserTaskData_store_values_for_the_user_task() throws Exception {
        final STaskContractData contractData = new STaskContractData(1983L, "id", 54L);
        contractData.setId(10L);
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        contractDataService.addUserTaskData(contractData);

        verify(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test
    public void addUserTaskData_accept_null_inputs() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        contractDataService.addUserTaskData(54L, null);

        verify(recorder, times(0)).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test(expected = SContractDataCreationException.class)
    public void addUserTaskData_throws_an_exception_if_not_able_to_store_the_data() throws Exception {
        final STaskContractData contractData = new STaskContractData(1983L, "id", 54L);
        contractData.setId(10L);
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        doThrow(new SRecorderException("exception")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        contractDataService.addUserTaskData(contractData);
    }

    @Test
    public void deleteUserTaskData_deletes_data() throws Exception {
        final List<STaskContractData> data = new ArrayList<STaskContractData>();
        data.add(new STaskContractData(1983L, "id", 456478L));
        data.add(new STaskContractData(1983L, "id2", 4564456478L));
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.deleteUserTaskData(1983L);

        verify(recorder, times(2)).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
    }

    @Test(expected = SContractDataDeletionException.class)
    public void deleteUserTaskData_throws_exception() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        final List<STaskContractData> data = new ArrayList<STaskContractData>();
        data.add(new STaskContractData(1983L, "id", 456478L));
        data.add(new STaskContractData(1983L, "id2", 4564456478L));
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);
        doThrow(new SRecorderException("exception")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        contractDataService.deleteUserTaskData(1983L);
    }

    @Test(expected = SContractDataDeletionException.class)
    public void deleteUserTaskData_throws_exception_when_retrieving_data() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.deleteUserTaskData(1983L);
    }

    @Test
    public void archiveUserTaskData_should_create_archive_data() throws Exception {
        final long usertTaskId = 1983L;
        final long time = 1010101010101001L;
        final List<STaskContractData> data = new ArrayList<STaskContractData>();
        final STaskContractData scd1 = new STaskContractData(usertTaskId, "id", 456478L);
        final STaskContractData scd2 = new STaskContractData(usertTaskId, "id2", 4564456478L);
        data.add(scd1);
        data.add(scd2);
        final ArchiveInsertRecord[] archivedata = new ArchiveInsertRecord[2];
        archivedata[0] = new ArchiveInsertRecord(new SATaskContractData(scd1));
        archivedata[1] = new ArchiveInsertRecord(new SATaskContractData(scd2));
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.archiveAndDeleteUserTaskData(usertTaskId, time);

        verify(archiveService).recordInserts(time, archivedata);
    }

    @Test
    public void archiveUserTaskData_should_not_create_archive_data_if_no_data_defined() throws Exception {
        final long usertTaskId = 1983L;
        final long time = 1010101010101001L;
        final List<STaskContractData> data = new ArrayList<STaskContractData>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.archiveAndDeleteUserTaskData(usertTaskId, time);

        verifyZeroInteractions(archiveService);
    }

    @Test(expected = SObjectModificationException.class)
    public void archiveUserTaskData_should_throw_exception_when_an_exception_occurs_when_getting_data() throws Exception {
        final long usertTaskId = 1983L;
        final long time = 1010101010101001L;
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.archiveAndDeleteUserTaskData(usertTaskId, time);
    }

    @Test
    public void getArchivedUserTaskData_returns_the_stored_value() throws Exception {
        final STaskContractData contractData = new STaskContractData(1983L, "id", 10L);
        final SATaskContractData saTaskContractData = new SATaskContractData(contractData);
        saTaskContractData.setArchiveDate(768468743687L);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(saTaskContractData);

        final Long id = (Long) contractDataService.getArchivedUserTaskDataValue(1983L, "id");

        assertThat(id).isEqualTo(10L);
    }

    @Test(expected = SContractDataNotFoundException.class)
    public void getArchivedUserTaskData_throws_an_exception_when_data_not_found() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        contractDataService.getArchivedUserTaskDataValue(1983L, "id");
    }

    @Test(expected = SBonitaReadException.class)
    public void getArchivedUserTaskData_throws_an_exception_with_a_read_exception() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.getArchivedUserTaskDataValue(1983L, "id");
    }

    /************* Process Data tests *************/

    @Test
    public void addProcessDataStoreValuesForTheProcessInstance() throws Exception {
        final SProcessContractData contractData = new SProcessContractData(1983L, "id", 54L);
        contractData.setId(10L);
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        contractDataService.addProcessData(contractData);

        verify(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test
    public void addProcessData_accept_null_inputs() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        contractDataService.addProcessData(54L, null);

        verify(recorder, times(0)).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test(expected = SContractDataCreationException.class)
    public void addProcessData_throws_an_exception_if_not_able_to_store_the_data() throws Exception {
        final SProcessContractData contractData = new SProcessContractData(1983L, "id", 54L);
        contractData.setId(10L);
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        doThrow(new SRecorderException("exception")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        contractDataService.addProcessData(contractData);
    }

    @Test
    public void getProcessDataReturnsTheStoredValue() throws Exception {
        long processInstanceId = 1117L;
        final SProcessContractData contractData = new SProcessContractData(processInstanceId, "TheName", "TheValue");
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(contractData);

        final String dataValue = (String) contractDataService.getProcessDataValue(processInstanceId, "TheName");

        assertThat(dataValue).isEqualTo("TheValue");
    }

    @Test(expected = SContractDataNotFoundException.class)
    public void getProcessDataShouldThrowContractDataNotFoundWhenNoDataIsReturned() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        contractDataService.getProcessDataValue(147L, null);
    }

    @Test(expected = SBonitaReadException.class)
    public void getProcessData_throws_an_exception_with_a_read_exception() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.getProcessDataValue(1983L, "id");
    }

    @Test
    public void deleteProcessData_deletes_data() throws Exception {
        final List<SProcessContractData> data = new ArrayList<SProcessContractData>();
        data.add(new SProcessContractData(1983L, "id", 456478L));
        data.add(new SProcessContractData(1983L, "id2", 4564456478L));
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.deleteProcessData(1983L);

        verify(recorder, times(2)).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
    }

    @Test(expected = SContractDataDeletionException.class)
    public void deleteProcessData_throws_exception() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        final List<SProcessContractData> data = new ArrayList<SProcessContractData>();
        data.add(new SProcessContractData(1983L, "id", 456478L));
        data.add(new SProcessContractData(1983L, "id2", 4564456478L));
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);
        doThrow(new SRecorderException("exception")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        contractDataService.deleteProcessData(1983L);
    }

    @Test(expected = SContractDataDeletionException.class)
    public void deleteProcessData_throws_exception_when_retrieving_data() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.deleteProcessData(1983L);
    }

    @Test
    public void archiveProcessData_should_create_archive_data() throws Exception {
        final long processInstanceId = 1983L;
        final long time = 1010101010101001L;
        final List<SProcessContractData> data = new ArrayList<SProcessContractData>();
        final SProcessContractData scd1 = new SProcessContractData(processInstanceId, "id", 456478L);
        final SProcessContractData scd2 = new SProcessContractData(processInstanceId, "id2", 4564456478L);
        data.add(scd1);
        data.add(scd2);
        final ArchiveInsertRecord[] archivedata = new ArchiveInsertRecord[2];
        archivedata[0] = new ArchiveInsertRecord(new SAProcessContractData(scd1));
        archivedata[1] = new ArchiveInsertRecord(new SAProcessContractData(scd2));
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.archiveAndDeleteProcessData(processInstanceId, time);

        verify(archiveService).recordInserts(time, archivedata);
    }

    @Test
    public void archiveProcessData_should_not_create_archive_data_if_no_data_defined() throws Exception {
        final long usertProcessId = 1983L;
        final long time = 1010101010101001L;
        final List<SProcessContractData> data = new ArrayList<SProcessContractData>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.archiveAndDeleteProcessData(usertProcessId, time);

        verifyZeroInteractions(archiveService);
    }

    @Test(expected = SObjectModificationException.class)
    public void archiveProcessData_should_throw_exception_when_an_exception_occurs_when_getting_data() throws Exception {
        final long usertProcessId = 1983L;
        final long time = 1010101010101001L;
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.archiveAndDeleteProcessData(usertProcessId, time);
    }

    @Test
    public void getArchivedProcessData_returns_the_stored_value() throws Exception {
        final SProcessContractData contractData = new SProcessContractData(1983L, "id", 10L);
        final SAProcessContractData saProcessContractData = new SAProcessContractData(contractData);
        saProcessContractData.setArchiveDate(768468743687L);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(saProcessContractData);

        final Long id = (Long) contractDataService.getArchivedProcessDataValue(1983L, "id");

        assertThat(id).isEqualTo(10L);
    }

    @Test(expected = SContractDataNotFoundException.class)
    public void getArchivedProcessData_throws_an_exception_when_data_not_found() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        contractDataService.getArchivedProcessDataValue(1983L, "id");
    }

    @Test(expected = SBonitaReadException.class)
    public void getArchivedProcessData_throws_an_exception_with_a_read_exception() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.getArchivedProcessDataValue(1983L, "id");
    }
}
