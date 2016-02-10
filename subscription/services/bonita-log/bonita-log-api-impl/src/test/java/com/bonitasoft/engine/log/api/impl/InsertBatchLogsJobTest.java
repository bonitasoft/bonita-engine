/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
    public void setUp() {
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
