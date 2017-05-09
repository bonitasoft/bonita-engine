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
package org.bonitasoft.engine.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JTATransactionServiceImplTest {

    @Mock
    TechnicalLoggerService logger;
    @Mock
    TransactionManager txManager;
    @Mock
    Transaction transaction;
    @InjectMocks
    private JTATransactionServiceImpl txService;
    @Mock
    private Callable<Object> txContent;
    @Mock
    private Callable<Void> beforeCommitCallable;

    @Before
    public void before() throws Exception {
        doReturn(transaction).when(txManager).getTransaction();
    }

    @Test
    public void beginTransaction() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_ACTIVE);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        txService.begin();
        verify(txManager, times(1)).begin();
        assertEquals(1, txService.getNumberOfActiveTransactions());
    }

    @Test(expected = STransactionCreationException.class)
    public void should_throw_exception_if_call_is_nested() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.begin();
        txService.begin();
    }

    @Test
    public void should_fail_if_begin_fails() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        doThrow(new SystemException("Mocked")).when(txManager).begin();

        try {
            txService.begin();
            fail("Thanks to the mock an exception must have been thrown");
        } catch (STransactionCreationException e) {
            verify(txManager, times(1)).begin();
            assertEquals(0, txService.getNumberOfActiveTransactions());
        }
    }

    @Test
    public void should_increment_tx_counter_when_begin() throws Exception {
        doReturn(Status.STATUS_NO_TRANSACTION).when(txManager).getStatus();

        txService.begin();

        assertThat(txService.getNumberOfActiveTransactions()).isEqualTo(1);
    }

    @Test
    public void setRollbackOnly_call_setRollbackOnly_on_txManager() throws Exception {
        txService.setRollbackOnly();
        verify(txManager).setRollbackOnly();
    }

    @Test
    public void should_executeInTransaction_with_fail_on_execute_do_not_make_other_tx_to_fail() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_ACTIVE);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        Callable<?> callable = mock(Callable.class);

        txService.executeInTransaction(callable);

        verify(txManager).begin();
        verify(callable).call();
        verify(txManager).commit();
    }

    /**
     * The method call has to be executed between a transaction.
     *
     * @throws Exception
     */
    @Test
    public void testExecuteInTransactionWithRollback() throws Exception {
        // First to allow to start the transaction, then to force to call rollback
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_MARKED_ROLLBACK);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        Callable<?> callable = mock(Callable.class);
        when(callable.call()).thenThrow(new Exception("Mocked exception"));

        try {
            txService.executeInTransaction(callable);
            fail("An exception should have been thrown.");
        } catch (Exception e) {
        }
        verify(txManager).begin();
        verify(callable).call();
        verify(txManager).setRollbackOnly();
        verify(txManager).rollback();
    }

    @Test
    public void should_not_register_synchronization_to_decrement_numberOfActiveTransaction_when_tx_managed_externally() throws Exception {
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();

        txService.begin();

        //we do not register synchro on number of transaction when managed externally because we cannot decrement correctly the counter at the end of the bonita tx
        verify(transaction, never()).registerSynchronization(any(Synchronization.class));
    }

    @Test
    public void should_not_increment_tx_counter_when_managed_externally() throws Exception {
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();

        txService.begin();

        //we do not register synchro on number of transaction when managed externally because we cannot decrement correctly the counter at the end of the bonita tx
        assertThat(txService.getNumberOfActiveTransactions()).isEqualTo(0);
    }

    @Test
    public void should_register_synchronization_to_decrement_numberOfActiveTransaction() throws Exception {
        doReturn(Status.STATUS_NO_TRANSACTION).when(txManager).getStatus();

        txService.begin();

        verify(transaction).registerSynchronization(any(Synchronization.class));
    }

    @Test
    public void should_call_beforeCommitCallable_on_complete() throws Exception {
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        txService.registerBeforeCommitCallable(beforeCommitCallable);

        txService.complete();

        verify(beforeCommitCallable).call();
    }

    @Test
    public void should_not_call_beforeCommitCallable_when_transaction_is_rollbacked() throws Exception {
        doReturn(Status.STATUS_MARKED_ROLLBACK).when(txManager).getStatus();
        txService.registerBeforeCommitCallable(beforeCommitCallable);

        txService.complete();

        verify(beforeCommitCallable, never()).call();
    }

    @Test
    public void should_reset_transaction_context_when_error_on_commit() throws Exception {
        doReturn(Status.STATUS_NO_TRANSACTION).doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        doThrow(SystemException.class).when(txManager).commit();

        try {
            txService.begin();
            txService.complete();
            fail();
        } catch (STransactionCommitException ignored) {
        }
        assertThat(txService.getTransactionServiceContext().isInScopeOfBonitaTransaction).isFalse();
    }

    @Test
    public void should_reset_transaction_context_when_error_on_begin() throws Exception {
        doReturn(Status.STATUS_NO_TRANSACTION).doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        doThrow(SystemException.class).when(txManager).begin();

        try {
            txService.begin();
            fail();
        } catch (STransactionCreationException ignored) {
        }
        assertThat(txService.getTransactionServiceContext().isInScopeOfBonitaTransaction).isFalse();
    }

    @Test
    public void should_commit_even_when_exception_on_commit_callable() throws Exception {
        //given
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        txService.registerBeforeCommitCallable(beforeCommitCallable);
        doThrow(Exception.class).when(beforeCommitCallable).call();
        //when
        try {
            txService.complete();
            fail();
        } catch (STransactionCommitException ignored) {
        }
        //then
        verify(txManager).commit();
    }

    @Test
    public void should_not_call_begin_if_transaction_is_externally_managed() throws Exception {
        //given
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        //when
        txService.begin();
        //then
        verify(txManager, never()).begin();
    }

    @Test
    public void should_call_begin_when_tx_manager_do_not_have_transaction() throws Exception {
        //given
        doReturn(Status.STATUS_NO_TRANSACTION).when(txManager).getStatus();
        //when
        txService.begin();
        //then
        verify(txManager).begin();
    }

    @Test
    public void should_not_call_commit_if_transaction_is_externally_managed() throws Exception {
        //given
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        txService.getTransactionServiceContext().externallyManaged = true;
        //when
        txService.complete();
        //then
        verify(txManager, never()).commit();
    }

    @Test
    public void should_call_commit_on_transaction_manager() throws Exception {
        //given
        doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        //when
        txService.complete();
        //then
        verify(txManager).commit();
    }

    @Test
    public void should_call_rollback_when_status_is_rollback_only() throws Exception {
        //given
        doReturn(Status.STATUS_MARKED_ROLLBACK).when(txManager).getStatus();
        //when
        txService.complete();
        //then
        verify(txManager).rollback();
    }

    @Test(expected = STransactionCommitException.class)
    public void should_throw_exception_when_complete_on_no_transaction() throws Exception {
        doReturn(Status.STATUS_NO_TRANSACTION).when(txManager).getStatus();

        txService.complete();
    }

    @Test
    public void should_begin_execute_callable_and_complete() throws Exception {
        //given
        doReturn(Status.STATUS_NO_TRANSACTION).doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        //when
        txService.executeInTransaction(txContent);
        //then
        InOrder inOrder = inOrder(txManager, txContent);
        inOrder.verify(txManager).begin();
        inOrder.verify(txContent).call();
        inOrder.verify(txManager).commit();
    }

    @Test
    public void should_trace_call_when_TRACE_active() throws Exception {
        doReturn(true).when(logger).isLoggable(any(Class.class), eq(TechnicalLogSeverity.TRACE));
        doReturn(Status.STATUS_NO_TRANSACTION).doReturn(Status.STATUS_ACTIVE).when(txManager).getStatus();
        TransactionService txService = new JTATransactionServiceImpl(logger, txManager);

        in_an_other_method_to_verify_the_stacktrace_contains_this_method_name(txService);

        try {
            txService.begin();
            fail();
        } catch (STransactionCreationException e) {
            assertThat(e.getMessage()).contains("in_an_other_method_to_verify_the_stacktrace_contains_this_method_name");
        }
    }

    private void in_an_other_method_to_verify_the_stacktrace_contains_this_method_name(TransactionService txService) throws STransactionCreationException {
        txService.begin();
    }
}
