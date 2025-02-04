/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.classloader;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.service.BonitaTaskExecutor;
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
    private ClassLoaderServiceImpl classLoaderService;
    @InjectMocks
    private ClassLoaderUpdater classLoaderUpdater;

    @Test
    public void should_refresh_classloaders_in_transaction() throws Exception {
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableGivenToTheTaskExecutor.capture());
        doReturn(null).when(userTransactionService)
                .executeInTransaction(callableGivenToTheTransactionService.capture());

        HashSet<ClassLoaderIdentifier> ids = new HashSet<>();
        ids.add(ClassLoaderIdentifier.TENANT);
        ids.add(identifier(PROCESS, 45L));
        classLoaderUpdater.refreshClassloaders(classLoaderService, ids);

        callableGivenToTheTaskExecutor.getValue().call();
        callableGivenToTheTransactionService.getValue().call();

        verify(classLoaderService).refreshClassLoaderImmediately(ClassLoaderIdentifier.TENANT);
        verify(classLoaderService).refreshClassLoaderImmediately(identifier(PROCESS, 45L));
    }

}
