/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.dependency.impl;

import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;
import org.bonitasoft.engine.transaction.UserTransactionService;


/**
 * @author Baptiste Mesta
 */
public class RefreshClassloaderSynchronization implements BonitaTransactionSynchronization {

    private AbstractDependencyService dependencyService;
    private BonitaTaskExecutor bonitaTaskExecutor;
    private UserTransactionService userTransactionService;
    private BroadcastService broadcastService;
    private SessionAccessor sessionAccessor;
    private final AbstractRefreshClassLoaderTask callable;
    private final Set<Pair<ScopeType, Long>> ids = new HashSet<>();
    private final Long tenantId;

    public RefreshClassloaderSynchronization(AbstractDependencyService dependencyService,
                                             BonitaTaskExecutor bonitaTaskExecutor, UserTransactionService userTransactionService,
                                             BroadcastService broadcastService,
                                             SessionAccessor sessionAccessor, AbstractRefreshClassLoaderTask callable,
                                             Long tenantId, ScopeType scopeType, Long scopeId) {
        this.dependencyService = dependencyService;
        this.bonitaTaskExecutor = bonitaTaskExecutor;
        this.userTransactionService = userTransactionService;
        this.broadcastService = broadcastService;
        this.sessionAccessor = sessionAccessor;
        this.callable = callable;
        this.tenantId = tenantId;
        addClassloader(scopeType, scopeId);
    }

    @Override
    public void beforeCommit() {
    }

    @Override
    public void afterCompletion(TransactionState txState) {
        dependencyService.removeRefreshClassLoaderSynchronization();
        if (txState == TransactionState.COMMITTED) {
            refreshLocalClassLoader();
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

    private void refreshLocalClassLoader() {
        Future<Void> execute = bonitaTaskExecutor.execute(
                inSession(inTransaction(() -> {
                    for (Pair<ScopeType, Long> id : ids) {
                        dependencyService.refreshClassLoader(id.getKey(), id.getValue());
                    }
                    return null;
                })));
        try {
            execute.get(5, TimeUnit.MINUTES);//hard coded timeout, it should never happen
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SBonitaRuntimeException("Unable to refresh the classloaders: "+ids, e);
        }
    }

    private <T> Callable<T> inSession(Callable<T> callable) {
        if (tenantId == null) {
            return callable;
        }
        return () -> {
            sessionAccessor.setTenantId(tenantId);
            try {
                return callable.call();
            } finally {
                sessionAccessor.deleteTenantId();
            }
        };
    }

    private <T> Callable<T> inTransaction(Callable<T> callable) {
        return () -> userTransactionService.executeInTransaction(callable);
    }

    //Testing purpose only
    Set<Pair<ScopeType, Long>> getIds() {
        return ids;
    }

    void addClassloader(ScopeType type, long id) {
        ids.add(pair(type, id));
    }
}
