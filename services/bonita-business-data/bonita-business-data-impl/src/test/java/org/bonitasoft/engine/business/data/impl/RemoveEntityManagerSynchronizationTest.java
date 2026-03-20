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
package org.bonitasoft.engine.business.data.impl;

import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_ROLLEDBACK;
import static javax.transaction.Status.STATUS_UNKNOWN;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Laurent Leseigneur
 */
@ExtendWith(MockitoExtension.class)
class RemoveEntityManagerSynchronizationTest {

    @Mock
    EntityManager entityManager;

    @Test
    void beforeCompletion_should_not_close_entityManager() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        RemoveEntityManagerSynchronization removeEntityManagerSynchronization = new RemoveEntityManagerSynchronization(
                localManager);

        //when
        removeEntityManagerSynchronization.beforeCompletion();

        //then
        verify(entityManager, never()).close();
        assertThat(localManager.get()).as("should remove entity manager").isNotNull();
    }

    @Test
    void afterCompletion_should_close_entityManager_on_STATUS_COMMITTED() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        doReturn(true).when(entityManager).isOpen();
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        //when
        sync.afterCompletion(STATUS_COMMITTED);

        //then
        verify(entityManager).close();
        assertThat(localManager.get()).as("should remove entity manager after commit").isNull();
    }

    @Test
    void afterCompletion_should_close_entityManager_on_STATUS_ROLLEDBACK() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        doReturn(true).when(entityManager).isOpen();
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        //when
        sync.afterCompletion(STATUS_ROLLEDBACK);

        //then
        verify(entityManager).close();
        assertThat(localManager.get()).as("should remove entity manager after rollback").isNull();
    }

    @Test
    void afterCompletion_should_close_entityManager_and_cleanup_threadLocal_on_STATUS_UNKNOWN() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        doReturn(true).when(entityManager).isOpen();
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        //when
        sync.afterCompletion(STATUS_UNKNOWN);

        //then
        verify(entityManager).close();
        assertThat(localManager.get()).as("should remove entity manager after heuristic outcome").isNull();
    }

    @Test
    void afterCompletion_should_not_close_already_closed_entityManager() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        doReturn(false).when(entityManager).isOpen();
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        //when
        sync.afterCompletion(STATUS_COMMITTED);

        //then — EM is already closed, so close() should not be called again
        verify(entityManager, never()).close();
        assertThat(localManager.get()).as("should remove entity manager even if already closed").isNull();
    }

    @Test
    void afterCompletion_should_not_throw_NPE_when_threadLocal_is_null() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(null);
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        //when
        sync.afterCompletion(STATUS_COMMITTED);

        //then
        assertThat(localManager.get()).as("should remove entity manager").isNull();
    }

    @Test
    void afterCompletion_should_still_remove_threadLocal_when_close_throws() {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        doReturn(true).when(entityManager).isOpen();
        doThrow(new PersistenceException("connection reset")).when(entityManager).close();
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        //when - then: close() failure should be ignored, and ThreadLocal is still cleaned up (via finally)
        assertThatNoException().isThrownBy(() -> sync.afterCompletion(STATUS_COMMITTED));
        verify(entityManager).close();
        assertThat(localManager.get())
                .as("ThreadLocal should be cleaned up even when close() throws")
                .isNull();
    }

    /**
     * Simulates what happens when Narayana's TransactionReaper calls afterCompletion()
     * on its own reaper thread instead of the application (worker) thread.
     * Since RemoveEntityManagerSynchronization uses ThreadLocal.get(), the reaper thread
     * sees null and the EM is never closed — the worker thread's ThreadLocal retains
     * the stale EntityManager.
     * This test documents the current known limitation (BPA-321).
     */
    @Test
    void afterCompletion_should_not_close_entityManager_when_called_from_different_thread() throws Exception {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        // Set the EM on the current (application) thread
        localManager.set(entityManager);
        RemoveEntityManagerSynchronization sync = new RemoveEntityManagerSynchronization(localManager);

        // Simulate: afterCompletion is called on a different thread (the reaper)
        CountDownLatch reaperDone = new CountDownLatch(1);
        AtomicReference<EntityManager> reaperThreadLocalValue = new AtomicReference<>();
        Thread reaperThread = new Thread(() -> {
            // On the reaper thread, localManager.get() returns null (different thread)
            reaperThreadLocalValue.set(localManager.get());
            sync.afterCompletion(STATUS_ROLLEDBACK);
            reaperDone.countDown();
        }, "Simulated-TransactionReaper");

        //when
        reaperThread.start();
        reaperDone.await(5, TimeUnit.SECONDS);

        //then
        // The reaper thread's ThreadLocal was null — it could not find the EM
        assertThat(reaperThreadLocalValue.get()).as("reaper thread should not see the worker's EM").isNull();
        // The EM was never closed because the reaper thread couldn't access it
        verify(entityManager, never()).close();
        // The worker thread's ThreadLocal still holds the stale EM (the leak)
        assertThat(localManager.get())
                .as("worker thread's ThreadLocal should still hold the stale EM (known limitation)")
                .isSameAs(entityManager);

        // Cleanup: remove from current thread to avoid test pollution
        localManager.remove();
    }
}
