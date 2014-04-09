package com.bonitasoft.engine.log.api.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BatchQueriableLoggerImplTest {
    
    @Mock
    private PersistenceService persistenceService;
    
    @Mock
    private TransactionService transactionService;
    
    @Mock
    private QueriableLoggerStrategy loggerStrategy;
    
    @Mock
    private QueriableLogSessionProvider sessionProvider;
    
    @Mock
    private TechnicalLoggerService logger;
    
    @Mock
    private PlatformService platformService;
    
    @Mock
    private BatchLogSynchronization synchro;
    
    @Mock
    private SQueriableLog log1;

    @Mock
    private SQueriableLog log2;
    
    
    @Test
    public void log_should_add_logs_to_synchronization() throws Exception {
        //given
        BatchQueriableLoggerImpl loggerService = spy(new BatchQueriableLoggerImpl(persistenceService, transactionService, loggerStrategy, sessionProvider, logger, platformService, true));
        doReturn(synchro).when(loggerService).getBatchLogSynchronization();
        
        //when
        loggerService.log(Arrays.asList(log1, log2));

        //then
        verify(synchro, times(1)).addLog(log1);
        verify(synchro, times(1)).addLog(log2);
    }
    
}
