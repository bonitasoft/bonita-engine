/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalPlatformInformationUpdaterTest {

    public static final int OBJECT_TO_LOCK_ID = 1;
    public static final String OBJECT_TYPE = "platformInfo";
    public static final int NONE_TENANT_ID = -1;
    @Mock
    private LockService lockService;

    @Mock
    private PlatformInformationManagerImpl platformInformationManager;

    @Mock
    private TransactionService transactionService;

    @Mock
    private PlatformInformationProvider provider;

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private TransactionalPlatformInformationUpdater transactionalPlatformInformationUpdater;

    @Before
    public void setUp() throws Exception {
        given(loggerService.isLoggable(Matchers.<Class<?>> any(), any(TechnicalLogSeverity.class))).willReturn(true);
        given(provider.get()).willReturn(1);

    }

    @Test
    public void run_should_execute_update_inside_a_locked_transaction_when_there_are_changes() throws Exception {
        //given
        BonitaLock lock = mock(BonitaLock.class);
        given(lockService.lock(OBJECT_TO_LOCK_ID, OBJECT_TYPE, NONE_TENANT_ID)).willReturn(lock);
        given(provider.get()).willReturn(1);

        //when
        transactionalPlatformInformationUpdater.run();

        //then
        InOrder inOrder = inOrder(lockService, transactionService);
        inOrder.verify(lockService).lock(OBJECT_TO_LOCK_ID, OBJECT_TYPE, NONE_TENANT_ID);
        inOrder.verify(transactionService)
                .executeInTransaction(any(TransactionalPlatformInformationUpdater.UpdatePlatformInfoTransactionContent.class));
        inOrder.verify(lockService).unlock(lock, -1);
    }

    @Test
    public void run_should_do_nothing_when_there_are_no_changes() throws Exception {
        //given
        given(provider.get()).willReturn(0);

        //when
        transactionalPlatformInformationUpdater.run();

        //then

        verifyZeroInteractions(lockService, transactionService);
    }

    @Test
    public void UpdatePlatformInfoTransaction_content_should_call_update_on_platformInfoService() throws Exception {
        //given
        TransactionalPlatformInformationUpdater.UpdatePlatformInfoTransactionContent content = transactionalPlatformInformationUpdater.new UpdatePlatformInfoTransactionContent();

        //when
        content.call();

        //then
        verify(platformInformationManager).update();
    }

    @Test
    public void run_should_log_exception_when_lock_throws_exception() throws Exception {
        //given
        SLockException lockException = new SLockException("impossible to lock");
        given(lockService.lock(OBJECT_TO_LOCK_ID, OBJECT_TYPE, NONE_TENANT_ID)).willThrow(lockException);

        //when
        transactionalPlatformInformationUpdater.run();

        //then
        verify(loggerService).log(TransactionalPlatformInformationUpdater.class, TechnicalLogSeverity.ERROR, "Unable to update the platform information.",
                lockException);

    }

    @Test
    public void run_should_log_exception_and_unlock_when_transactionService_throws_exception() throws Exception {
        //given
        BonitaLock lock = mock(BonitaLock.class);
        given(lockService.lock(OBJECT_TO_LOCK_ID, OBJECT_TYPE, NONE_TENANT_ID)).willReturn(lock);

        SPlatformUpdateException updateException = new SPlatformUpdateException("impossible to update");
        given(transactionService.executeInTransaction(any(TransactionalPlatformInformationUpdater.UpdatePlatformInfoTransactionContent.class)))
                .willThrow(updateException);

        //when
        transactionalPlatformInformationUpdater.run();

        //then
        verify(loggerService).log(TransactionalPlatformInformationUpdater.class, TechnicalLogSeverity.ERROR, "Unable to update the platform information.",
                updateException);
        verify(lockService).unlock(lock, NONE_TENANT_ID);

    }

    @Test
    public void run_should_log_exception_when_unlock_throws_exception() throws Exception {
        //given
        BonitaLock lock = mock(BonitaLock.class);
        given(lockService.lock(OBJECT_TO_LOCK_ID, OBJECT_TYPE, NONE_TENANT_ID)).willReturn(lock);
        SLockException exception = new SLockException("Unable to unlock");
        doThrow(exception).when(lockService).unlock(lock, NONE_TENANT_ID);

        //when
        transactionalPlatformInformationUpdater.run();

        //then
        verify(loggerService).log(TransactionalPlatformInformationUpdater.class, TechnicalLogSeverity.ERROR,
                "Unable to release platform lock. Please, restart your server", exception);

    }

}
