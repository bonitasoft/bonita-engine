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

import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.transaction.Status;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;

/**
 * @author Baptiste Mesta
 */
class RefreshClassloaderSynchronization implements BonitaTransactionSynchronization {

    private ClassLoaderServiceImpl classLoaderService;
    private ClassLoaderUpdater classLoaderUpdater;
    private BroadcastService broadcastService;
    private final RefreshClassLoaderTask callable;
    private final Set<Pair<ScopeType, Long>> ids = new HashSet<>();
    private final Long tenantId;

    public RefreshClassloaderSynchronization(ClassLoaderServiceImpl classLoaderService,
            BroadcastService broadcastService,
            RefreshClassLoaderTask callable,
            ClassLoaderUpdater classLoaderUpdater,
            Long tenantId, ScopeType scopeType, Long scopeId) {
        this.classLoaderService = classLoaderService;
        this.classLoaderUpdater = classLoaderUpdater;
        this.broadcastService = broadcastService;
        this.callable = callable;
        this.tenantId = tenantId;
        addClassloaderToRefresh(scopeType, scopeId);
    }

    @Override
    public void afterCompletion(final int txState) {
        classLoaderService.removeRefreshClassLoaderSynchronization();
        if (txState == Status.STATUS_COMMITTED) {
            classLoaderUpdater.refreshClassloaders(classLoaderService, tenantId, ids);
            refreshClassLoaderOnOtherNodes();
        }
    }

    private void refreshClassLoaderOnOtherNodes() {
        try {
            Map<String, TaskResult<Void>> execute = broadcastService.executeOnOthersAndWait(callable, tenantId);
            for (Map.Entry<String, TaskResult<Void>> resultEntry : execute.entrySet()) {
                if (resultEntry.getValue().isError()) {
                    throw new IllegalStateException(resultEntry.getValue().getThrowable());
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SBonitaRuntimeException("Unable to refresh the classloaders on all nodes: " + ids, e);
        }
    }

    //Testing purpose only
    Set<Pair<ScopeType, Long>> getIds() {
        return ids;
    }

    void addClassloaderToRefresh(ScopeType type, long id) {
        ids.add(pair(type, id));
    }
}
