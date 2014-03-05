package org.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIImplTest {

    @Mock
    private PlatformServiceAccessor serviceAccessor;

    @Mock
    private SchedulerService schedulerService;

    @Spy
    private PlatformAPIImpl platformAPI;

    @Test
    public void test() throws Exception {
        doReturn(serviceAccessor).when(platformAPI).getPlatformAccessor();
        doReturn(schedulerService).when(serviceAccessor).getSchedulerService();

        platformAPI.rescheduleErroneousTriggers();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void test1() throws Exception {
        doReturn(serviceAccessor).when(platformAPI).getPlatformAccessor();
        doReturn(schedulerService).when(serviceAccessor).getSchedulerService();
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void test2() throws Exception {
        doThrow(new IOException()).when(platformAPI).getPlatformAccessor();

        platformAPI.rescheduleErroneousTriggers();
    }

}
