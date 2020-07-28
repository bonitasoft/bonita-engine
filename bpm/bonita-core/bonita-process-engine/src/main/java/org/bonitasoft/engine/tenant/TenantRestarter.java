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

import org.bonitasoft.engine.api.impl.StarterThread;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 */
@Component
public class TenantRestarter {

    private final UserTransactionService transactionService;
    private final List<TenantRestartHandler> tenantRestartHandlers;
    private final Long tenantId;
    private final SessionAccessor sessionAccessor;
    private final PlatformService platformService;

    public TenantRestarter(@Value("${tenantId}") Long tenantId, UserTransactionService transactionService,
            SessionAccessor sessionAccessor, PlatformService platformService,
            List<TenantRestartHandler> tenantRestartHandlers) {
        this.tenantId = tenantId;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.platformService = platformService;
        this.tenantRestartHandlers = tenantRestartHandlers;
    }

    public void executeBeforeServicesStart() throws Exception {
        transactionService.executeInTransaction(() -> {
            for (TenantRestartHandler tenantRestartHandler : tenantRestartHandlers) {
                tenantRestartHandler.beforeServicesStart();
            }
            return null;
        });
    }

    public void executeAfterServicesStart() {
        new StarterThread(tenantId, sessionAccessor, transactionService, platformService,
                tenantRestartHandlers).start();
    }

}
