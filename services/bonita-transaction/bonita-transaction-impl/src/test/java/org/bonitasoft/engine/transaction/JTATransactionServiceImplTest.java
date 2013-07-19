package org.bonitasoft.engine.transaction;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;


public class JTATransactionServiceImplTest {

    @Test
    public void beginTransaction() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);
        EventService eventService = mock(EventService.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager, eventService);

        txService.begin();
        verify(txManager, times(1)).begin();
    }

    @Test(expected=STransactionCreationException.class)
    public void doNotSupportNestedTransaction() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);
        EventService eventService = mock(EventService.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager, eventService);

        txService.begin();
    }

    @Test
    public void beginTransactionEventFailed() throws Exception {
        // We want to ensure that when an exception was thrown after the transaction's begin then
        // we close the open transaction to be in a consistent state.

        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);
        EventService eventService = mock(EventService.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        when(eventService.hasHandlers(TransactionService.TRANSACTION_ACTIVE_EVT, null)).thenThrow(new RuntimeException("Mocked"));

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager, eventService));

        try {
            txService.begin();
            fail("The begin should have thrown an exception.");
        } catch (STransactionCreationException e) {
            verify(txManager, times(1)).rollback();
        }
    }

}
