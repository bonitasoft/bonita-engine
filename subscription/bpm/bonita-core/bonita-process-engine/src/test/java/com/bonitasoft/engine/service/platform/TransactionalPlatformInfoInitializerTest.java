/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import com.bonitasoft.engine.execution.LockInfo;
import com.bonitasoft.engine.execution.transaction.LockedTransactionExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalPlatformInfoInitializerTest {

    @Mock
    private PlatformInfoInitializer platformInfoInitializer;

    @Mock
    private LockedTransactionExecutor lockedTransactionExecutor;

    @InjectMocks
    private TransactionalPlatformInfoInitializer transactionalPlatformInfoInitializer;

    @Test
    public void ensurePlatformInfoIsSet_should_call_simple_initializer() throws Exception {
        //when
        transactionalPlatformInfoInitializer.ensurePlatformInfoIsSet();

        //then
        ArgumentCaptor<LockInfo> captor = ArgumentCaptor.forClass(LockInfo.class);
        verify(lockedTransactionExecutor).executeInsideLock(captor.capture(), any(TransactionalPlatformInfoInitializer.PlatformInfoInitContent.class));
    }

    @Test
    public void call_should_delegate_execution_to_platformInitializer() throws Exception {
        //given

        //when
        transactionalPlatformInfoInitializer.new PlatformInfoInitContent().call();

        //then
        verify(platformInfoInitializer).ensurePlatformInfoIsSet();
    }

}