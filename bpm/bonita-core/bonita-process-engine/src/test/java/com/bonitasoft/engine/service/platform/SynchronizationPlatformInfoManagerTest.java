/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizationPlatformInfoManagerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UpdatePlatformInfoSynchronization synchronization;

    @InjectMocks
    private SynchronizationPlatformInfoManager manager;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void update_should_register_synchronization() throws Exception {
        //when
        manager.update();

        //then
        verify(transactionService).registerBonitaSynchronization(synchronization);
    }

    @Test
    public void update_should_throw_SPlatformUpdateException_when_transactionService_throws_Exception() throws Exception {
        //given
        doThrow(new STransactionNotFoundException("no transaction found")).when(transactionService)
                .registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));

        //then
        expectedException.expect(SPlatformUpdateException.class);
        expectedException.expectMessage("Unable to update the platform information");

        //when
        manager.update();

    }

}
