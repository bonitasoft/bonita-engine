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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static javax.transaction.Status.*;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BroadcastService;
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
    private ClassLoaderServiceImpl classLoaderService;
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

        refreshClassloaderSynchronization.afterCompletion(STATUS_NO_TRANSACTION);

        verify(classLoaderService).removeRefreshClassLoaderSynchronization();
    }

    @Test
    public void should_refresh_classloader_using_classLoaderUpdater() throws Exception {
        doReturn(emptyMap()).when(broadcastService).executeOnOthersAndWait(any(), anyLong());

        refreshClassloaderSynchronization.afterCompletion(STATUS_COMMITTED);

        verify(classLoaderUpdater).refreshClassloaders(classLoaderService, 1L,
                singleton(pair(ScopeType.PROCESS, 111L)));
    }

    @Test
    public void should_refresh_classloader_on_other_nodes_after_commit() throws Exception {
        doReturn(emptyMap()).when(broadcastService).executeOnOthersAndWait(any(), anyLong());

        refreshClassloaderSynchronization.afterCompletion(STATUS_COMMITTED);

        verify(broadcastService).executeOnOthersAndWait(refreshClassLoaderTask, 1L);
    }

    @Test
    public void should_not_refresh_classloader_when_transaction_is_not_committed() {
        refreshClassloaderSynchronization.afterCompletion(STATUS_MARKED_ROLLBACK);

        verifyZeroInteractions(broadcastService);
        verifyZeroInteractions(classLoaderUpdater);
    }
}
