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
package org.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.restart.TenantRestartHandler;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread start when the engine is ready.
 * Its purpose is to start elements to be recovered from the previous run of the engine.
 *
 * @author Baptiste Mesta
 */
public class StarterThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(StarterThread.class);

    private final List<TenantRestartHandler> tenantRestartHandlers;
    private final Long tenantId;
    private final SessionAccessor sessionAccessor;
    private final UserTransactionService transactionService;
    private final PlatformService platformService;

    public StarterThread(Long tenantId, SessionAccessor sessionAccessor,
            UserTransactionService transactionService, PlatformService platformService,
            List<TenantRestartHandler> tenantRestartHandlers) {
        super("Tenant " + tenantId + " starter Thread");
        this.tenantRestartHandlers = tenantRestartHandlers;
        this.tenantId = tenantId;
        this.sessionAccessor = sessionAccessor;
        this.transactionService = transactionService;
        this.platformService = platformService;
    }

    @Override
    public void run() {
        STenant tenant = getTenant();
        logger.info("Restarting elements of tenant {} that were not finished at the last shutdown", tenant.getId());
        if (!tenant.isActivated()) {
            logger.warn("Unable to restart elements of tenant because tenant is {}", tenant.getStatus());
            return;
        }
        executeHandlers(sessionAccessor);
    }

    private void executeHandlers(SessionAccessor sessionAccessor) {
        sessionAccessor.setTenantId(tenantId);
        try {
            for (final TenantRestartHandler restartHandler : tenantRestartHandlers) {
                try {
                    logger.info("Executing Restart Handler " + restartHandler.getClass().getName());
                    restartHandler.afterServicesStart();
                } catch (Exception e) {
                    logger.error("The Restart Handler " + restartHandler.getClass().getName() + " failed", e);
                }
            }
        } finally {
            sessionAccessor.deleteTenantId();
        }
    }

    STenant getTenant() {
        try {
            return transactionService.executeInTransaction(() -> platformService.getTenant(tenantId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
