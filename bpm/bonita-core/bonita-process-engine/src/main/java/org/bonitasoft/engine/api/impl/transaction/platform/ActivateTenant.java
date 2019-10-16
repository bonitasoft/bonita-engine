/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.transaction.platform;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public final class ActivateTenant implements TransactionContent {

    private final long tenantId;
    private final PlatformService platformService;
    private final SchedulerService schedulerService;
    private final WorkService workService;
    private final ConnectorExecutor connectorExecutor;

    public ActivateTenant(final long tenantId, final PlatformService platformService, final SchedulerService schedulerService,
                          final WorkService workService, final ConnectorExecutor connectorExecutor) {
        this.tenantId = tenantId;
        this.platformService = platformService;
        this.schedulerService = schedulerService;
        this.workService = workService;
        this.connectorExecutor = connectorExecutor;
    }

    @Override
    public void execute() throws SBonitaException {
        String previousStatus = platformService.getTenant(tenantId).getStatus();
        platformService.activateTenant(tenantId);
        // we execute that only if the tenant was not already activated
        if (!previousStatus.equals(STenant.ACTIVATED)) {
            workService.start();
            connectorExecutor.start();
            schedulerService.resumeJobs(tenantId);
        }
    }

}
