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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Baptiste Mesta
 */
public class RefreshClassloaderSynchronization implements BonitaTransactionSynchronization {

    private BroadcastService broadcastService;
    private final AbstractRefreshClassLoaderTask callable;
    private final Long tenantId;

    public RefreshClassloaderSynchronization(BroadcastService broadcastService, AbstractRefreshClassLoaderTask callable, Long tenantId) {
        this.broadcastService = broadcastService;
        this.callable = callable;
        this.tenantId = tenantId;
    }

    @Override
    public void beforeCommit() {

    }

    @Override
    public void afterCompletion(TransactionState txState) {
        try {
            Map<String, TaskResult<Void>> execute = broadcastService.executeOnOthersAndWait(callable, tenantId);
            for (Map.Entry<String, TaskResult<Void>> resultEntry : execute.entrySet()) {
                if (resultEntry.getValue().isError()) {
                    throw new IllegalStateException(resultEntry.getValue().getThrowable());
                }

            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Unable to execute refresh classloader on other nodes", e);
        }
    }
}
