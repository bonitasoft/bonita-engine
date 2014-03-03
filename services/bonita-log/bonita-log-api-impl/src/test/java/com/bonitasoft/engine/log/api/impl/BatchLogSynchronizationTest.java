package com.bonitasoft.engine.log.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class BatchLogSynchronizationTest {

    
    @Mock
    private PersistenceService persistenceService;

    @Mock
    private BatchLogBuffer batchLogBuffer;

    @Mock
    private InsertBatchLogsJobRegister jobRegister;
    
    @Mock
    private SQueriableLog log1;

    @Mock
    private SQueriableLog log2;
    
    @Captor
    private ArgumentCaptor<List<PersistentObject>> captor;
    
    @Before
    public void setUp() throws Exception {
        
    }
    
    @Test
    public void afterCompletion_add_logs_to_buffer_and_register_job_if_delayable_and_transaction_was_committed() throws Exception {
        //given
        BatchLogSynchronization synchro = new BatchLogSynchronization(persistenceService, batchLogBuffer, jobRegister, true);
        synchro.addLog(log1);
        synchro.addLog(log2);

        //when
        synchro.afterCompletion(TransactionState.COMMITTED);

        //then
        verify(batchLogBuffer, times(1)).addLogs(Arrays.asList(log1, log2));
        verify(jobRegister, times(1)).registerJobIfNotRegistered();
    }

    @Test
    public void afterCompletion_doesnt_add_logs_to_buffer_nor_register_job_if_not_delayable_and_transaction_was_committed() throws Exception {
        //given
        BatchLogSynchronization synchro = new BatchLogSynchronization(persistenceService, batchLogBuffer, jobRegister, false);
        synchro.addLog(log1);
        synchro.addLog(log2);
        
        //when
        synchro.afterCompletion(TransactionState.COMMITTED);
        
        //then
        verify(batchLogBuffer, never()).addLogs(Matchers.<List<SQueriableLog>>any());
        verify(jobRegister, never()).registerJobIfNotRegistered();
    }

    @Test
    public void afterCompletion_doesnt_add_logs_to_buffer_nor_register_job_if_delayable_and_transaction_wasnt_committed() throws Exception {
        //given
        BatchLogSynchronization synchro = new BatchLogSynchronization(persistenceService, batchLogBuffer, jobRegister, true);
        synchro.addLog(log1);
        synchro.addLog(log2);
        
        //when
        synchro.afterCompletion(TransactionState.ROLLEDBACK);
        
        //then
        verify(batchLogBuffer, never()).addLogs(Matchers.<List<SQueriableLog>>any());
        verify(jobRegister, never()).registerJobIfNotRegistered();
    }
    
    @Test
    public void beforeCommit_should_insert_batch_and_fush_if_not_delayable() throws Exception {
        //given
        BatchLogSynchronization synchro = new BatchLogSynchronization(persistenceService, batchLogBuffer, jobRegister, false);
        synchro.addLog(log1);
        synchro.addLog(log2);

        //when
        synchro.beforeCommit();

        //then
        verify(persistenceService, times(1)).insertInBatch(captor.capture());
        assertThat(captor.getValue()).isEqualTo(Arrays.asList(log1, log2));
        verify(persistenceService, times(1)).flushStatements();
    }

    @Test
    public void beforeCommit_should_not_insert_batch_neither_fush_if_delayable() throws Exception {
        //given
        BatchLogSynchronization synchro = new BatchLogSynchronization(persistenceService, batchLogBuffer, jobRegister, true);
        synchro.addLog(log1);
        synchro.addLog(log2);
        
        //when
        synchro.beforeCommit();
        
        //then
        verify(persistenceService, never()).insertInBatch(Matchers.<List<PersistentObject>>any());
        verify(persistenceService, never()).flushStatements();
    }
    
}
