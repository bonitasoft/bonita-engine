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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePlatformInfoSynchronizationTest {

    @Mock
    private PlatformInformationProvider provider;

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private UpdatePlatformInfoSynchronization synchronization;

    @Before
    public void setUp() throws Exception {
        given(loggerService.isLoggable(eq(UpdatePlatformInfoSynchronization.class), any(TechnicalLogSeverity.class))).willReturn(true);
    }

    @Test
    public void afterCompletion_should_register_element_when_committed() throws Exception {
        //when
        synchronization.afterCompletion(TransactionState.COMMITTED);

        //then
        verify(provider).register();
    }

    @Test
    public void afterCompletion_should_not_register_element_when_rolled_back() throws Exception {
        //when
        synchronization.afterCompletion(TransactionState.ROLLEDBACK);

        //then
        verify(provider, never()).register();
    }

    @Test
    public void before_commit_should_do_nothing() throws Exception {
        //when
        synchronization.beforeCommit();

        //then
        verifyZeroInteractions(provider);
    }
}