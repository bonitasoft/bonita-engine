package org.bonitasoft.engine.classloader;

import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashSet;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ClassLoaderUpdaterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private BonitaTaskExecutor bonitaTaskExecutor;
    @Captor
    private ArgumentCaptor<Callable<?>> callableGivenToTheTaskExecutor;
    @Captor
    private ArgumentCaptor<Callable<?>> callableGivenToTheTransactionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private ClassLoaderServiceImpl classLoaderService;
    @InjectMocks
    private ClassLoaderUpdater classLoaderUpdater;


    @Test
    public void should_refresh_classloaders_in_session_and_in_transaction() throws Exception {
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableGivenToTheTaskExecutor.capture());
        doReturn(null).when(userTransactionService).executeInTransaction(callableGivenToTheTransactionService.capture());

        HashSet<Pair<ScopeType, Long>> ids = new HashSet<>();
        ids.add(pair(ScopeType.TENANT, 4L));
        ids.add(pair(PROCESS, 45L));
        classLoaderUpdater.refreshClassloaders(classLoaderService, 12L, ids);

        callableGivenToTheTaskExecutor.getValue().call();
        callableGivenToTheTransactionService.getValue().call();

        verify(classLoaderService).refreshClassLoaderImmediately(ScopeType.TENANT, 4L);
        verify(classLoaderService).refreshClassLoaderImmediately(PROCESS, 45L);
    }
    @Test
    public void should_not_create_a_session_when_there_is_no_tenant_id() throws Exception {
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableGivenToTheTaskExecutor.capture());

        classLoaderUpdater.refreshClassloaders(classLoaderService, null, singleton(pair(PROCESS, 4L)));

        callableGivenToTheTaskExecutor.getValue().call();

        verifyZeroInteractions(sessionAccessor);
    }
    @Test
    public void should_create_a_session_when_there_is_a_tenant_id() throws Exception {
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableGivenToTheTaskExecutor.capture());

        classLoaderUpdater.refreshClassloaders(classLoaderService, 54L, singleton(pair(PROCESS, 4L)));

        callableGivenToTheTaskExecutor.getValue().call();

        verify(sessionAccessor).setTenantId(54L);
    }

}