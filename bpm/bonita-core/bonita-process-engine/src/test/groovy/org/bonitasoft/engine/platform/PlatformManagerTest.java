package org.bonitasoft.engine.platform;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.impl.SPlatformPropertiesImpl;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PlatformManagerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PlatformService platformService;
    @InjectMocks
    public PlatformManager platformManager;

    @Before
    public void before() throws Exception {
        when(transactionService.executeInTransaction(any())).thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());

        doReturn(new SPlatform("1.3.0", "1.2.0", "1.1.0", "someUser", 123455)).when(platformService).getPlatform();
        doReturn(new SPlatformPropertiesImpl("1.3.0")).when(platformService).getSPlatformProperties();
    }

    @Test
    public void should_start_scheduler_when_starting_node() throws Exception {

        platformManager.start();

        verify(schedulerService).start();
    }

    @Test
    public void should_not_start_scheduler_when_starting_node_if_node_is_already_started() throws Exception {
        platformManager.start();
        verify(schedulerService).start();
        verify(schedulerService).isStarted();

        platformManager.start();

        verifyNoMoreInteractions(schedulerService);
    }

}