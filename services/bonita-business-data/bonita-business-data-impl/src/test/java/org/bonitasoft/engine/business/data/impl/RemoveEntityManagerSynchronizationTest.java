package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;

import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveEntityManagerSynchronizationTest {

    @Mock
    EntityManager entityManager;

    @Test
    public void should_keep_entityManager_open_before_commit() throws Exception {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        RemoveEntityManagerSynchronization removeEntityManagerSynchronization = new RemoveEntityManagerSynchronization(localManager);

        //when
        removeEntityManagerSynchronization.beforeCommit();

        //then
        verify(entityManager, times(0)).close();
        assertThat(localManager.get()).as("should remove entity manager").isNotNull();

    }

    @Test
    public void should_close_entityManager_after_transaction_completion() throws Exception {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        RemoveEntityManagerSynchronization removeEntityManagerSynchronization = new RemoveEntityManagerSynchronization(localManager);

        //when
        removeEntityManagerSynchronization.afterCompletion(TransactionState.ACTIVE);

        //then
        verify(entityManager).close();
        assertThat(localManager.get()).as("should remove entity manager").isNull();

    }
}
