/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;

/**
 * Restart flow nodes for works: {@link ExecuteFlowNodeWork} {@link ExecuteConnectorOfActivity} {@link NotifyChildFinishedWork}
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class RestartFlowNodesHandler implements TenantRestartHandler {

    final Map<Long, List<Long>> flownodesToRestartByTenant = new HashMap<Long, List<Long>>();

    @Override
    public void beforeServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws RestartException {
        try {
            long tenantId = tenantServiceAccessor.getTenantId();
            TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
            ArrayList<Long> flownodesToRestart = new ArrayList<Long>();
            flownodesToRestartByTenant.put(tenantId, flownodesToRestart);
            FlowNodeInstanceService flowNodeInstanceService = tenantServiceAccessor.getActivityInstanceService();
            QueryOptions queryOptions = new QueryOptions(0, 100);
            List<Long> ids = null;
            logInfo(logger, "Restarting flow nodes...");
            do {
                ids = flowNodeInstanceService.getFlowNodeInstanceIdsToRestart(queryOptions);
                flownodesToRestart.addAll(ids);
                queryOptions = QueryOptions.getNextPage(queryOptions);

            } while (ids.size() == queryOptions.getNumberOfResults());
            logInfo(logger, "Found " + flownodesToRestart.size() + " flow nodes to restart on tenant " + tenantId);
        } catch (SFlowNodeReadException e) {
            throw new RestartException("unable to flag elements as to be restarted", e);
        }
    }

    private void logInfo(final TechnicalLoggerService logger, final String message) {
        final boolean isInfo = logger.isLoggable(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO);
        if (isInfo) {
            logger.log(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO, message);
        }
    }



    @Override
    public void afterServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws RestartException {
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        final WorkService workService = tenantServiceAccessor.getWorkService();
        final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        TransactionService transactionService = platformServiceAccessor.getTransactionService();
        long tenantId = tenantServiceAccessor.getTenantId();
        List<Long> flownodesIds = flownodesToRestartByTenant.get(tenantId);

        logger.log(getClass(), TechnicalLogSeverity.INFO, "Restarting " + flownodesIds.size() + " flow nodes for tenant " + tenantId);
        try {
            Iterator<Long> iterator = flownodesIds.iterator();
            ExecuteFlowNodes exec = null;
            do {
                exec = new ExecuteFlowNodes(workService, logger, activityInstanceService, iterator);
                transactionService.executeInTransaction(exec);
            } while (iterator.hasNext());
        } catch (Exception e) {
            throw new RestartException("Unable to restart elements", e);
        }

    }
}
