package com.bonitasoft.engine.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SRetryableException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class JTATransactionServiceExtTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private JTATransactionServiceExt jtaTransactionServiceExt;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private TransactionManager txManager;
    private InOrder inOrder;

    @Before
    public void before() throws Exception {
        int retries = 2;
        long delay = 100;
        double delayFActor = 1.5;
        jtaTransactionServiceExt = spy(new JTATransactionServiceExt(logger, txManager, retries, delay, delayFActor));
        doNothing().when(jtaTransactionServiceExt).begin();
        doNothing().when(jtaTransactionServiceExt).setRollbackOnly();
        doNothing().when(jtaTransactionServiceExt).complete();
        inOrder = inOrder(jtaTransactionServiceExt);
    }

    @Test
    public void should_executeInTransaction_retry_when_fail_with_retryable() throws Exception {
        try {
            expectedException.expect(IllegalStateException.class);
            jtaTransactionServiceExt.executeInTransaction(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    throw new SRetryableException(new IllegalStateException("error"));
                }
            });
        } finally {
            inOrder.verify(jtaTransactionServiceExt).begin();
            inOrder.verify(jtaTransactionServiceExt).setRollbackOnly();
            inOrder.verify(jtaTransactionServiceExt).complete();
            inOrder.verify(jtaTransactionServiceExt).begin();
            inOrder.verify(jtaTransactionServiceExt).setRollbackOnly();
            inOrder.verify(jtaTransactionServiceExt).complete();
            inOrder.verify(jtaTransactionServiceExt).begin();
            inOrder.verify(jtaTransactionServiceExt).setRollbackOnly();
            inOrder.verify(jtaTransactionServiceExt).complete();
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void should_executeInTransaction_fail_when_fail_with_non_retryable() throws Exception {
        try {
            expectedException.expect(IllegalStateException.class);
            jtaTransactionServiceExt.executeInTransaction(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    throw new IllegalStateException("error");
                }
            });
        } finally {
            inOrder.verify(jtaTransactionServiceExt).begin();
            inOrder.verify(jtaTransactionServiceExt).setRollbackOnly();
            inOrder.verify(jtaTransactionServiceExt).complete();
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void should_executeInTransaction_call_begin_and_complete() throws Exception {
        final Object theResult = jtaTransactionServiceExt.executeInTransaction(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return "The result";
            }
        });
        inOrder.verify(jtaTransactionServiceExt).begin();
        inOrder.verify(jtaTransactionServiceExt).complete();
        inOrder.verifyNoMoreInteractions();
        verify(jtaTransactionServiceExt, never()).setRollbackOnly();
        assertThat(theResult).isEqualTo("The result");
    }
}
