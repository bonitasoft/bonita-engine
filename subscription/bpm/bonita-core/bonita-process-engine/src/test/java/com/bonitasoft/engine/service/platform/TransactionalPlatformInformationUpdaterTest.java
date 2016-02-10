/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.bonitasoft.engine.execution.LockInfo;
import com.bonitasoft.engine.execution.transaction.LockedTransactionExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalPlatformInformationUpdaterTest {

    @Mock
    private PlatformInformationManagerImpl platformInformationManager;

    @Mock
    private PlatformInformationProvider provider;

    @Mock
    private LockedTransactionExecutor lockedTransactionExecutor;

    @InjectMocks
    private TransactionalPlatformInformationUpdater transactionalPlatformInformationUpdater;

    @Before
    public void setUp() throws Exception {
        given(provider.get()).willReturn(1);

    }

    @Test
    public void run_should_execute_update_when_there_are_changes() throws Exception {
        //given
        given(provider.get()).willReturn(1);

        //when
        transactionalPlatformInformationUpdater.run();

        //then
        ArgumentCaptor<LockInfo> lockInfoCaptor = ArgumentCaptor.forClass(LockInfo.class);
        verify(lockedTransactionExecutor).executeInsideLock(lockInfoCaptor.capture(), any(TransactionalPlatformInformationUpdater.UpdatePlatformInfoTransactionContent.class));
        assertThat(lockInfoCaptor.getValue()).isEqualToComparingFieldByField(PlatformInfoLock.build());
    }

    @Test
    public void run_should_do_nothing_when_there_are_no_changes() throws Exception {
        //given
        given(provider.get()).willReturn(0);

        //when
        transactionalPlatformInformationUpdater.run();

        //then

        verifyZeroInteractions(lockedTransactionExecutor);
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

}
