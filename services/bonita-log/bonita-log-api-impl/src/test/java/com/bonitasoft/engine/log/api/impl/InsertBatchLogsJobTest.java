package com.bonitasoft.engine.log.api.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InsertBatchLogsJobTest {

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private SQueriableLog log1;

    @Mock
    private SQueriableLog log2;

    @Before
    public void setUp() throws Exception {
        BatchLogBuffer.getInstance().clearLogs();
    }

    @Test
    public void execute_should_insert_batch_via_persitence_service() throws Exception {
        // given
        BatchLogBuffer logBuffer = BatchLogBuffer.getInstance();
        logBuffer.addLogs(Arrays.asList(log1, log2));

        InsertBatchLogsJob insertBatchLogsJob = new InsertBatchLogsJob();
        insertBatchLogsJob.setPersistenceService(persistenceService);

        // when
        insertBatchLogsJob.execute();

        // then
        verify(persistenceService, times(1)).insertInBatch(new ArrayList<PersistentObject>(Arrays.asList(log1, log2)));
    }
}
