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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformInfoUpdateScheduledExecutorTest {

    public static final int PERIOD = 2;
    @Mock
    private TransactionalPlatformInformationUpdater transactionalPlatformInformationUpdater;

    @Mock
    private ScheduledExecutorService executorService;

    private PlatformInfoUpdateScheduledExecutor platformInfoUpdateScheduledExecutor;

    @Before
    public void setUp() throws Exception {
        platformInfoUpdateScheduledExecutor = spy(new PlatformInfoUpdateScheduledExecutor(transactionalPlatformInformationUpdater));
        given(platformInfoUpdateScheduledExecutor.getScheduledExecutor()).willReturn(executorService);
        given(platformInfoUpdateScheduledExecutor.getPeriod()).willReturn(PERIOD);
    }

    @Test
    public void start_should_schedule_updater() throws Exception {
        //when
        platformInfoUpdateScheduledExecutor.start();

        //then
        verify(executorService).scheduleWithFixedDelay(transactionalPlatformInformationUpdater, PERIOD, PERIOD, TimeUnit.SECONDS);
    }

    @Test
    public void stop_should_shutdown_scheduler() throws Exception {
        //when
        platformInfoUpdateScheduledExecutor.stop();

        //then
        verify(executorService).shutdown();
    }

    @Test
    public void pause_should_do_nothing() throws Exception {
        //when
        platformInfoUpdateScheduledExecutor.pause();

        //then
        verifyZeroInteractions(executorService);
    }

    @Test
    public void resume_should_do_nothing() throws Exception {
        //when
        platformInfoUpdateScheduledExecutor.resume();

        //then
        verifyZeroInteractions(executorService);

    }

}