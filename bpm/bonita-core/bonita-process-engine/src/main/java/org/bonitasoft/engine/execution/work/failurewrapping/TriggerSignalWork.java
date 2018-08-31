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

package org.bonitasoft.engine.execution.work.failurewrapping;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.execution.work.TenantAwareBonitaWork;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 */
public class TriggerSignalWork extends TenantAwareBonitaWork {

    private long signalId;
    private String signalName;

    public TriggerSignalWork(long signalId, String signalName) {
        this.signalId = signalId;
        this.signalName = signalName;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + " waitingSignalEvent: " + signalId;
    }

    @Override
    public CompletableFuture<Void> work(Map<String, Object> context) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        SWaitingSignalEvent listeningSignal = tenantAccessor.getEventInstanceService()
                .getWaitingSignalEvent(signalId);
        tenantAccessor.getEventsHandler().triggerCatchEvent(listeningSignal, null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void handleFailure(Throwable e, Map<String, Object> context) throws Exception {
        throw new UnsupportedOperationException("No automatic failure handling for signals. See recovery procedure.");
    }

    @Override
    public String getRecoveryProcedure() {
        return "send the signal " + signalName + " again";
    }
}
