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
package org.bonitasoft.engine.tenant;

import java.util.List;

import javax.transaction.Status;

import org.bonitasoft.engine.api.impl.StarterThread;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 */
@Component
public class TenantRestarter {

    private UserTransactionService transactionService;
    private List<TenantRestartHandler> tenantRestartHandlers;
    private Long tenantId;
    private SessionAccessor sessionAccessor;
    private SessionService sessionService;
    private PlatformService platformService;

    public TenantRestarter(@Value("${tenantId}") Long tenantId, UserTransactionService transactionService,
            SessionAccessor sessionAccessor, SessionService sessionService,
            PlatformService platformService, List<TenantRestartHandler> tenantRestartHandlers) {
        this.tenantId = tenantId;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.platformService = platformService;
        this.tenantRestartHandlers = tenantRestartHandlers;
    }

    public List<TenantRestartHandler> executeBeforeServicesStart() throws Exception {
        if (transactionService.isTransactionActive()) {
            return beforeServicesStart();
        }
        return transactionService.executeInTransaction(this::beforeServicesStart);
    }

    private List<TenantRestartHandler> beforeServicesStart() throws RestartException {
        for (TenantRestartHandler tenantRestartHandler : tenantRestartHandlers) {
            tenantRestartHandler.beforeServicesStart();
        }
        return tenantRestartHandlers;
    }

    public void executeAfterServicesStart(List<TenantRestartHandler> tenantRestartHandlers)
            throws STransactionNotFoundException {
        if (transactionService.isTransactionActive()) {
            executeAfterServicesStartAfterCurrentTransaction(tenantRestartHandlers);
        } else {
            afterServicesStart(tenantRestartHandlers);
        }
    }

    private void afterServicesStart(List<TenantRestartHandler> tenantRestartHandlers) {
        new StarterThread(tenantId, sessionAccessor, sessionService, transactionService, platformService,
                tenantRestartHandlers).start();
    }

    private void executeAfterServicesStartAfterCurrentTransaction(
            final List<TenantRestartHandler> tenantRestartHandlers) throws STransactionNotFoundException {
        transactionService.registerBonitaSynchronization((BonitaTransactionSynchronization) txState -> {
            if (txState == Status.STATUS_COMMITTED) {
                afterServicesStart(tenantRestartHandlers);
            }
        });
    }
}
