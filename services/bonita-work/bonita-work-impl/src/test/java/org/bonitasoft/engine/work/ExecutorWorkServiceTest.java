package org.bonitasoft.engine.work;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class ExecutorWorkServiceTest {

    private ExecutorWorkService executorWorkService;

    private TransactionService transactionService;

    private WorkSynchronizationFactory workSynchronizationFactory;

    private TechnicalLoggerService loggerService;

    private SessionAccessor sessionAccessor;

    private BonitaExecutorServiceFactory bonitaExecutorServiceFactory;

    private AbstractWorkSynchronization abstractWorkSynchronization;

    private ThreadPoolExecutor threadPoolExecutor;

    @Before
    public void before() throws Exception {
        transactionService = mock(TransactionService.class);
        workSynchronizationFactory = mock(WorkSynchronizationFactory.class);
        loggerService = mock(TechnicalLoggerService.class);
        sessionAccessor = mock(SessionAccessor.class);
        bonitaExecutorServiceFactory = mock(BonitaExecutorServiceFactory.class);
        abstractWorkSynchronization = mock(AbstractWorkSynchronization.class);
        threadPoolExecutor = mock(ThreadPoolExecutor.class);

        doReturn(1L).when(sessionAccessor).getTenantId();
        doReturn(threadPoolExecutor).when(bonitaExecutorServiceFactory).createExecutorService();

        executorWorkService = spy(new ExecutorWorkService(transactionService, workSynchronizationFactory, loggerService, sessionAccessor,
                bonitaExecutorServiceFactory));

    }

    @Test
    public void pauseShouldDeactivateWorkservice() throws WorkRegisterException {

        // when
        executorWorkService.pause();

        // then
        assertThat(executorWorkService.isDeactivated()).as("WorkService should be deactivated").isTrue();

    }

    @Test
    public void pauseShouldNotAllowToRegisterWork() throws WorkRegisterException {

        // given
        executorWorkService.pause();

        // when
        executorWorkService.registerWork(createBonitaWork());

        // then
        verify(executorWorkService, times(1)).isDeactivated();
        verify(abstractWorkSynchronization, times(0)).addWork(createBonitaWork());

    }

    @Test
    public void pauseShouldNotAllowToExecuteWork() throws WorkRegisterException {

        // given
        executorWorkService.start();
        BonitaWork bonitaWork = createBonitaWork();

        // when
        executorWorkService.pause();
        executorWorkService.executeWork(bonitaWork);

        // then
        verify(executorWorkService, atLeastOnce()).isDeactivated();
        verify(threadPoolExecutor, times(0)).submit(bonitaWork);
    }

    @Test
    public void resumeShouldDeactivateWorkservice() throws WorkRegisterException {

        // when
        executorWorkService.resume();

        // then
        assertThat(executorWorkService.isDeactivated()).as("WorkService should be deactivated").isFalse();

    }

    @Test
    public void resumeShouldAllowToRegisterWork() throws WorkRegisterException {

        // given
        executorWorkService.resume();

        // when
        executorWorkService.registerWork(createBonitaWork());

        // then
        verify(executorWorkService, atLeastOnce()).isDeactivated();
        verify(abstractWorkSynchronization, times(0)).addWork(createBonitaWork());
    }

    @Test
    public void resumeShouldAllowToExecuteWork() throws WorkRegisterException {

        // given
        BonitaWork bonitaWork = createBonitaWork();

        // when
        executorWorkService.resume();
        executorWorkService.executeWork(bonitaWork);

        // then
        verify(threadPoolExecutor, times(1)).submit(bonitaWork);
    }

    @Test
    public void checkStartStatus() {

        // when
        executorWorkService.start();

        // then
        assertThat(executorWorkService.isStopped()).isFalse();
        assertThat(executorWorkService.isDeactivated()).isFalse();

    }

    @Test
    public void checkStopStatus() {

        // when
        executorWorkService.stop();

        // then
        assertThat(executorWorkService.isStopped()).isTrue();
        assertThat(executorWorkService.isDeactivated()).isTrue();

    }

    @Test(expected = WorkRegisterException.class)
    public void executeWorkShouldThrowExceptionWhenTenantIdNotSet() throws WorkRegisterException, TenantIdNotSetException {

        // given
        executorWorkService.start();
        BonitaWork bonitaWork = createBonitaWork();

        // when
        doThrow(TenantIdNotSetException.class).when(sessionAccessor).getTenantId();

        // then
        executorWorkService.executeWork(bonitaWork);

    }

    private BonitaWork createBonitaWork() {
        BonitaWork work = new BonitaWork() {

            private static final long serialVersionUID = 1L;

            @Override
            public void work(Map<String, Object> context) throws Exception {
            }

            @Override
            public void handleFailure(Throwable e, Map<String, Object> context) throws Exception {

            }

            @Override
            public String getDescription() {
                return "fake bonita work";
            }
        };
        return work;
    }
}
