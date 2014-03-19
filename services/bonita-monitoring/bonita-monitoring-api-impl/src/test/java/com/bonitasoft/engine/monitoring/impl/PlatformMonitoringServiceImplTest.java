package com.bonitasoft.engine.monitoring.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.monitoring.mbean.SJvmMXBean;


@RunWith(MockitoJUnitRunner.class)
public class PlatformMonitoringServiceImplTest {
    
    @Mock
    private SJvmMXBean jvmMBean;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TransactionService transactionService;
    
    @Mock
    private TechnicalLoggerService loggerService;
    
    private PlatformMonitoringServiceImpl platformMonitoringService;
    
    @Before
    public void setUp() throws Exception {
        platformMonitoringService = new PlatformMonitoringServiceImpl(true, jvmMBean, transactionService, schedulerService, loggerService);
    }


    @Test
    public void getNumberOfActiveTransactions_return_nb_of_active_transactions_from_transaction_service() throws Exception {
        //given
        doReturn(11L).when(transactionService).getNumberOfActiveTransactions();
        
        //when
        long activeTransactions = platformMonitoringService.getNumberOfActiveTransactions();

        //then
        assertThat(activeTransactions).isEqualTo(11);
    }
    
}
