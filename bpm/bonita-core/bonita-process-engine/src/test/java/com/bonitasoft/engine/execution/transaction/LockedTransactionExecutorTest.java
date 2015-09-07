/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.execution.transaction;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.bonitasoft.engine.execution.LockInfo;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LockedTransactionExecutorTest {

    public static final long OBJECT_TO_LOCK_ID = 1;
    public static final String OBJECT_TYPE = "platformInfo";
    public static final int NONE_TENANT_ID = -1;
    public static final Class<LockedTransactionExecutor> CALLER_CLASS = LockedTransactionExecutor.class;
    @Mock
    private LockService lockService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TechnicalLoggerService loggerService;

    @Mock
    private Callable<String> callable;

    @InjectMocks
    private LockedTransactionExecutor lockedTransactionExecutor;

    private LockInfo lockInfo;

    @Before
    public void setUp() throws Exception {
        given(loggerService.isLoggable(Matchers.<Class<?>>any(), any(TechnicalLogSeverity.class))).willReturn(true);
        lockInfo = new LockInfo(OBJECT_TO_LOCK_ID, OBJECT_TYPE, NONE_TENANT_ID);
    }

    @Test
    public void should_execute_transaction_inside_a_locked() throws Exception {
        //given
        BonitaLock lock = Mockito.mock(BonitaLock.class);
        given(lockService.lock(lockInfo.getId(), lockInfo.getType(), lockInfo.getTenantId())).willReturn(lock);

        //when
        lockedTransactionExecutor.executeInsideLock(lockInfo, callable);

        //then
        InOrder inOrder = Mockito.inOrder(lockService, transactionService);
        inOrder.verify(lockService).lock(lockInfo.getId(), lockInfo.getType(), lockInfo.getTenantId());
        inOrder.verify(transactionService)
                .executeInTransaction(callable);
        inOrder.verify(lockService).unlock(lock, lockInfo.getTenantId());
    }

    @Test
    public void should_log_exception_when_lock_throws_exception() throws Exception {
        //given
        SLockException lockException = new SLockException("impossible to lock");
        given(lockService.lock(lockInfo.getId(), lockInfo.getType(), lockInfo.getTenantId())).willThrow(lockException);

        //when
        lockedTransactionExecutor.executeInsideLock(lockInfo, callable);

        //then
        verify(loggerService).log(CALLER_CLASS, TechnicalLogSeverity.ERROR, "Unable to execute transaction.",
                lockException);

    }

    @Test
    public void should_log_exception_and_unlock_when_transactionService_throws_exception() throws Exception {
        //given
        BonitaLock lock = Mockito.mock(BonitaLock.class);
        given(lockService.lock(lockInfo.getId(), lockInfo.getType(), lockInfo.getTenantId())).willReturn(lock);

        SPlatformUpdateException updateException = new SPlatformUpdateException("impossible to update");
        given(transactionService.executeInTransaction(callable))
                .willThrow(updateException);

        //when
        lockedTransactionExecutor.executeInsideLock(lockInfo, callable);

        //then
        verify(loggerService).log(CALLER_CLASS, TechnicalLogSeverity.ERROR, "Unable to execute transaction.",
                updateException);
        verify(lockService).unlock(lock, lockInfo.getTenantId());

    }

    @Test
    public void should_log_exception_when_unlock_throws_exception() throws Exception {
        //given
        BonitaLock lock = Mockito.mock(BonitaLock.class);
        given(lockService.lock(lockInfo.getId(), lockInfo.getType(), lockInfo.getTenantId())).willReturn(lock);
        SLockException exception = new SLockException("Unable to unlock");
        Mockito.doThrow(exception).when(lockService).unlock(lock, lockInfo.getTenantId());

        //when
        lockedTransactionExecutor.executeInsideLock(lockInfo, callable);

        //then
        verify(loggerService).log(CALLER_CLASS, TechnicalLogSeverity.ERROR,
                "Unable to release lock: " + lockInfo.toString() + ". Please, restart your server", exception);

    }


}