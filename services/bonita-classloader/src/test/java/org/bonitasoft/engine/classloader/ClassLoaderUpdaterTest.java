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

import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
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
        doReturn(null).when(userTransactionService)
                .executeInTransaction(callableGivenToTheTransactionService.capture());

        HashSet<ClassLoaderIdentifier> ids = new HashSet<>();
        ids.add(identifier(ScopeType.TENANT, 4L));
        ids.add(identifier(PROCESS, 45L));
        classLoaderUpdater.refreshClassloaders(classLoaderService, 12L, ids);

        callableGivenToTheTaskExecutor.getValue().call();
        callableGivenToTheTransactionService.getValue().call();

        verify(classLoaderService).refreshClassLoaderImmediately(identifier(ScopeType.TENANT, 4L));
        verify(classLoaderService).refreshClassLoaderImmediately(identifier(PROCESS, 45L));
    }

    @Test
    public void should_not_create_a_session_when_there_is_no_tenant_id() throws Exception {
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableGivenToTheTaskExecutor.capture());

        classLoaderUpdater.refreshClassloaders(classLoaderService, null, singleton(identifier(PROCESS, 4L)));

        callableGivenToTheTaskExecutor.getValue().call();

        verifyNoInteractions(sessionAccessor);
    }

    @Test
    public void should_create_a_session_when_there_is_a_tenant_id() throws Exception {
        doReturn(completedFuture(null)).when(bonitaTaskExecutor).execute(callableGivenToTheTaskExecutor.capture());

        classLoaderUpdater.refreshClassloaders(classLoaderService, 54L, singleton(identifier(PROCESS, 4L)));

        callableGivenToTheTaskExecutor.getValue().call();

        verify(sessionAccessor).setTenantId(54L);
    }

    @Test
    public void should_throw_SBonitaRuntimeException_on_timeout_in_execute() throws Exception {
        // Make executor return a Future whose timed get throws TimeoutException
        Future future = mock(Future.class);
        doReturn(future).when(bonitaTaskExecutor).execute((Callable<Object>) any());
        doThrow(new TimeoutException("simulated timeout"))
                .when(future).get(anyLong(), any(TimeUnit.class));

        assertThatExceptionOfType(SBonitaRuntimeException.class)
                .isThrownBy(
                        () -> classLoaderUpdater.initializeClassLoader(classLoaderService, identifier(PROCESS, 45L)))
                .withCauseExactlyInstanceOf(TimeoutException.class)
                .withMessageStartingWith("Unable to refresh the classloaders");
    }

    @Test
    public void should_keep_positive_timeout_value_when_validating() throws Exception {
        // given
        setTimeoutValue(classLoaderUpdater, 10L);

        // when
        classLoaderUpdater.validateClassLoaderInitializationTimeoutMinutes();

        // then
        assertThat(getTimeoutValue(classLoaderUpdater)).isEqualTo(10L);
    }

    @Test
    public void should_reset_to_default_when_timeout_is_zero() throws Exception {
        // given
        setTimeoutValue(classLoaderUpdater, 0L);

        // when
        classLoaderUpdater.validateClassLoaderInitializationTimeoutMinutes();

        // then
        assertThat(getTimeoutValue(classLoaderUpdater)).isEqualTo(5L);
    }

    @Test
    public void should_reset_to_default_when_timeout_is_negative() throws Exception {
        // given
        setTimeoutValue(classLoaderUpdater, -5L);

        // when
        classLoaderUpdater.validateClassLoaderInitializationTimeoutMinutes();

        // then
        assertThat(getTimeoutValue(classLoaderUpdater)).isEqualTo(5L);
    }

    @Test
    public void should_keep_minimum_positive_value_when_validating() throws Exception {
        // given
        setTimeoutValue(classLoaderUpdater, 1L);

        // when
        classLoaderUpdater.validateClassLoaderInitializationTimeoutMinutes();

        // then
        assertThat(getTimeoutValue(classLoaderUpdater)).isEqualTo(1L);
    }

    @Test
    public void should_keep_large_timeout_value_when_validating() throws Exception {
        // given
        setTimeoutValue(classLoaderUpdater, 120L);

        // when
        classLoaderUpdater.validateClassLoaderInitializationTimeoutMinutes();

        // then
        assertThat(getTimeoutValue(classLoaderUpdater)).isEqualTo(120L);
    }

    private void setTimeoutValue(ClassLoaderUpdater updater, long value) throws Exception {
        FieldUtils.writeField(updater, "classLoaderInitializationTimeoutMinutes", value, true);
    }

    private long getTimeoutValue(ClassLoaderUpdater updater) throws Exception {
        return (long) FieldUtils.readField(updater, "classLoaderInitializationTimeoutMinutes", true);
    }

}
