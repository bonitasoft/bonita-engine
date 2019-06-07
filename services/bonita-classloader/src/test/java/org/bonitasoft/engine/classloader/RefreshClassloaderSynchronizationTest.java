package org.bonitasoft.engine.classloader;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class RefreshClassloaderSynchronizationTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BroadcastService broadcastService;
    @Mock
    private RefreshClassLoaderTask refreshClassLoaderTask;
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private ClassLoaderUpdater classLoaderUpdater;
    private RefreshClassloaderSynchronization refreshClassloaderSynchronization;


    @Before
    public void before() {
        refreshClassloaderSynchronization = new RefreshClassloaderSynchronization(classLoaderService, broadcastService,
                refreshClassLoaderTask, classLoaderUpdater, 1L, ScopeType.PROCESS, 111L);
    }

    @Test
    public void should_remove_the_synchronization_when_its_executed() {

        refreshClassloaderSynchronization.afterCompletion(TransactionState.NO_TRANSACTION);

        verify(classLoaderService).removeRefreshClassLoaderSynchronization();
    }

    @Test
    public void should_refresh_classloader_using_classLoaderUpdater() throws Exception {
        doReturn(emptyMap()).when(broadcastService).executeOnOthersAndWait(any(),anyLong());

        refreshClassloaderSynchronization.afterCompletion(TransactionState.COMMITTED);

        verify(classLoaderUpdater).refreshClassloaders(classLoaderService, 1L, singleton(pair(ScopeType.PROCESS, 111L)));
    }

    @Test
    public void should_refresh_classloader_on_other_nodes_after_commit() throws Exception {
        doReturn(emptyMap()).when(broadcastService).executeOnOthersAndWait(any(),anyLong());

        refreshClassloaderSynchronization.afterCompletion(TransactionState.COMMITTED);

        verify(broadcastService).executeOnOthersAndWait(refreshClassLoaderTask, 1L);
    }

    @Test
    public void should_not_refresh_classloader_when_transaction_is_not_committed() {
        refreshClassloaderSynchronization.afterCompletion(TransactionState.ROLLBACKONLY);

        verifyZeroInteractions(broadcastService);
        verifyZeroInteractions(classLoaderUpdater);
    }
}
