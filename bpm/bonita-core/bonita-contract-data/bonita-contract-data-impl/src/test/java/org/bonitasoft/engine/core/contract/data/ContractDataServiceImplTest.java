package org.bonitasoft.engine.core.contract.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
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
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        contractDataService.deleteUserTaskData(1983L);

        verify(recorder).recordDeleteAll(any(DeleteAllRecord.class));
    }

    @Test(expected = SContractDataDeletionException.class)
    public void deleteUserTaskData_throws_exception() throws Exception {
        when(queriableLoggerService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);
        doThrow(new SRecorderException("exception")).when(recorder).recordDeleteAll(any(DeleteAllRecord.class));

        contractDataService.deleteUserTaskData(1983L);
    }

}
