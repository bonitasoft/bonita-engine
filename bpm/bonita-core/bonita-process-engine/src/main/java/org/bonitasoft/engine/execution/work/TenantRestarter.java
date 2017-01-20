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

package org.bonitasoft.engine.execution.work;

import java.util.List;

import org.bonitasoft.engine.api.impl.StarterThread;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Baptiste Mesta
 */
public class TenantRestarter {

    private final PlatformServiceAccessor platformServiceAccessor;
    private final TenantServiceAccessor tenantServiceAccessor;

    public TenantRestarter(PlatformServiceAccessor platformServiceAccessor, TenantServiceAccessor tenantServiceAccessor) {
        this.platformServiceAccessor = platformServiceAccessor;
        this.tenantServiceAccessor = tenantServiceAccessor;
    }

    public List<TenantRestartHandler> executeBeforeServicesStart() throws RestartException {
        List<TenantRestartHandler> tenantRestartHandlers = platformServiceAccessor.getPlatformConfiguration().getTenantRestartHandlers();
        for (TenantRestartHandler tenantRestartHandler : tenantRestartHandlers) {
            tenantRestartHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        }
        return tenantRestartHandlers;
    }

    public void executeAfterServicesStart(List<TenantRestartHandler> tenantRestartHandlers) {

        new StarterThread(platformServiceAccessor, tenantServiceAccessor, tenantRestartHandlers).start();
    }

    public void executeAfterServicesStartAfterCurrentTransaction(final List<TenantRestartHandler> tenantRestartHandlers) throws STransactionNotFoundException {
        platformServiceAccessor.getTransactionService().registerBonitaSynchronization(new BonitaTransactionSynchronization() {

            @Override
            public void beforeCommit() {

            }

            @Override
            public void afterCompletion(TransactionState txState) {
                if (txState.equals(TransactionState.COMMITTED)) {
                    executeAfterServicesStart(tenantRestartHandlers);
                }
            }
        });
    }
}
