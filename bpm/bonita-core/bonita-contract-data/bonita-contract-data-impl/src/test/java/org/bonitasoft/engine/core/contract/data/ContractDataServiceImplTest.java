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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

    @Test
    public void getUserTaskData_returns_the_stored_value() throws Exception {
        final SContractData contractData = new SContractData("id", 10L, 1983L);
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(contractData);

        final Long id = (Long) contractDataService.getUserTaskData(1983L, "id");

        assertThat(id).isEqualTo(10L);
    }

    @Test(expected = SContractDataNotFoundException.class)
    public void getUserTaskData_throws_an_exception_when_data_not_found() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        contractDataService.getUserTaskData(1983L, "id");
    }

    @Test(expected = SBonitaReadException.class)
    public void getUserTaskData_throws_an_exception_with_a_read_exception() throws Exception {
        when(persistenceService.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.getUserTaskData(1983L, "id");
    }

    @Test
    public void addUserTaskData_store_values_for_the_user_task() throws Exception {
        final SContractData contractData = new SContractData("id", 54L, 1983L);
        contractData.setId(10L);
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        contractDataService.addUserTaskData(contractData);

        verify(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test(expected=SContractDataCreationException.class)
    public void addUserTaskData_throws_an_exception_if_not_able_to_store_the_data() throws Exception {
        final SContractData contractData = new SContractData("id", 54L, 1983L);
        contractData.setId(10L);
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        doThrow(new SRecorderException("exception")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        contractDataService.addUserTaskData(contractData);
    }

    @Test
    public void deleteUserTaskData_deletes_data() throws Exception {
        final List<SContractData> data = new ArrayList<SContractData>();
        data.add(new SContractData("id", 456478L, 1983L));
        data.add(new SContractData("id2", 4564456478L, 1983L));
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.deleteUserTaskData(1983L);

        verify(recorder, times(2)).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
    }

    @Test(expected = SContractDataDeletionException.class)
    public void deleteUserTaskData_throws_exception() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        final List<SContractData> data = new ArrayList<SContractData>();
        data.add(new SContractData("id", 456478L, 1983L));
        data.add(new SContractData("id2", 4564456478L, 1983L));
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
        final List<SContractData> data = new ArrayList<SContractData>();
        final SContractData scd1 = new SContractData("id", 456478L, usertTaskId);
        final SContractData scd2 = new SContractData("id2", 4564456478L, usertTaskId);
        data.add(scd1);
        data.add(scd2);
        final ArchiveInsertRecord[] archivedata = new ArchiveInsertRecord[2];
        archivedata[0] = new ArchiveInsertRecord(new SAContractData(scd1));
        archivedata[1] = new ArchiveInsertRecord(new SAContractData(scd2));
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.archiveUserTaskData(usertTaskId, time);

        verify(archiveService).recordInserts(time, archivedata);
    }

    @Test
    public void archiveUserTaskData_should_not_create_archive_data_if_no_data_defined() throws Exception {
        final long usertTaskId = 1983L;
        final long time = 1010101010101001L;
        final List<SContractData> data = new ArrayList<SContractData>();
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenReturn(data);

        contractDataService.archiveUserTaskData(usertTaskId, time);

        verifyZeroInteractions(archiveService);
    }

    @Test(expected = SObjectModificationException.class)
    public void archiveUserTaskData_should_throw_exception_when_an_exception_occurs_when_getting_data() throws Exception {
        final long usertTaskId = 1983L;
        final long time = 1010101010101001L;
        when(persistenceService.selectList(any(SelectListDescriptor.class))).thenThrow(new SBonitaReadException("exception"));

        contractDataService.archiveUserTaskData(usertTaskId, time);
    }

}
