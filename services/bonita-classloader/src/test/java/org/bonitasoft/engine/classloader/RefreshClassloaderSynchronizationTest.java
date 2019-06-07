package org.bonitasoft.engine.classloader;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionState;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class RefreshClassloaderSynchronizationTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BroadcastService broadcastService;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private BonitaTaskExecutor bonitaTaskExecutor;
    @Captor
    private ArgumentCaptor<Callable<?>> callableArgumentCaptor1;
    @Captor
    private ArgumentCaptor<Callable<?>> callableArgumentCaptor2;

    private RefreshClassloaderSynchronization refreshClassloaderSynchronization;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private RefreshClassLoaderTask refreshClassLoaderTask;
    @Mock
    private ClassLoaderService classLoaderService;


    @Before
    public void before() {
        refreshClassloaderSynchronization = new RefreshClassloaderSynchronization(classLoaderService, bonitaTaskExecutor,
                userTransactionService, broadcastService, sessionAccessor, refreshClassLoaderTask, 1L, ScopeType.PROCESS, 111L);
    }

    @Test
    public void should_remove_the_synchronization_when_its_executed() {

        refreshClassloaderSynchronization.afterCompletion(TransactionState.NO_TRANSACTION);

        verify(classLoaderService).removeRefreshClassLoaderSynchronization();
    }

    @Test
    public void should_refresh_classloader_after_commit_in_an_executor() throws Exception {
        doReturn(emptyMap()).when(broadcastService).executeOnOthersAndWait(any(),anyLong());
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableArgumentCaptor1.capture());
        doReturn(null).when(userTransactionService).executeInTransaction(callableArgumentCaptor2.capture());

        refreshClassloaderSynchronization.afterCompletion(TransactionState.COMMITTED);
        callableArgumentCaptor1.getValue().call();
        callableArgumentCaptor2.getValue().call();

        verify(classLoaderService).refreshClassLoader(ScopeType.PROCESS, 111);
    }

    @Test
    public void should_refresh_classloader_on_other_nodes_after_commit() throws Exception {
        doReturn(emptyMap()).when(broadcastService).executeOnOthersAndWait(any(),anyLong());
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(any());

        refreshClassloaderSynchronization.afterCompletion(TransactionState.COMMITTED);

        verify(broadcastService).executeOnOthersAndWait(refreshClassLoaderTask, 1L);
    }

    @Test
    public void should_not_refresh_classloader_when_transaction_is_not_committed() {
        refreshClassloaderSynchronization.afterCompletion(TransactionState.ROLLBACKONLY);

        verifyZeroInteractions(broadcastService);
        verifyZeroInteractions(bonitaTaskExecutor);
    }
}
