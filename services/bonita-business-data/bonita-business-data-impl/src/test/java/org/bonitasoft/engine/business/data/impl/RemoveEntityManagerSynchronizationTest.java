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

import static javax.transaction.Status.STATUS_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;

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
        RemoveEntityManagerSynchronization removeEntityManagerSynchronization = new RemoveEntityManagerSynchronization(
                localManager);

        //when
        removeEntityManagerSynchronization.beforeCompletion();

        //then
        verify(entityManager, times(0)).close();
        assertThat(localManager.get()).as("should remove entity manager").isNotNull();

    }

    @Test
    public void should_close_entityManager_after_transaction_completion() throws Exception {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(entityManager);
        RemoveEntityManagerSynchronization removeEntityManagerSynchronization = new RemoveEntityManagerSynchronization(
                localManager);

        //when
        removeEntityManagerSynchronization.afterCompletion(STATUS_ACTIVE);

        //then
        verify(entityManager).close();
        assertThat(localManager.get()).as("should remove entity manager").isNull();

    }

    @Test
    public void should_not_throw_NPE_after_transaction_completion_in_case_there_was_a_problem() throws Exception {
        //given
        ThreadLocal<EntityManager> localManager = new ThreadLocal<>();
        localManager.set(null);
        RemoveEntityManagerSynchronization removeEntityManagerSynchronization = new RemoveEntityManagerSynchronization(
                localManager);

        //when
        removeEntityManagerSynchronization.afterCompletion(STATUS_ACTIVE);

        //then
        assertThat(localManager.get()).as("should remove entity manager").isNull();

    }
}
