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
package org.bonitasoft.engine.execution.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

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
            final long tenantId = tenantServiceAccessor.getTenantId();
            final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
            final ArrayList<Long> flownodesToRestart = new ArrayList<Long>();
            flownodesToRestartByTenant.put(tenantId, flownodesToRestart);
            final FlowNodeInstanceService flowNodeInstanceService = tenantServiceAccessor.getActivityInstanceService();

            // using a to low page size (100) causes too many access to the database and causes timeout exception if there are lot of elements.
            // As we retrieve only the id we can use a greater page size
            QueryOptions queryOptions = new QueryOptions(0, 50000);
            List<Long> ids = null;
            logInfo(logger, "Restarting flow nodes...");
            do {
                ids = flowNodeInstanceService.getFlowNodeInstanceIdsToRestart(queryOptions);
                flownodesToRestart.addAll(ids);
                queryOptions = QueryOptions.getNextPage(queryOptions);

            } while (ids.size() == queryOptions.getNumberOfResults());
            logInfo(logger, "Found " + flownodesToRestart.size() + " flow nodes to restart on tenant " + tenantId);
        } catch (final SBonitaReadException e) {
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
        final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        final TransactionService transactionService = platformServiceAccessor.getTransactionService();
        final long tenantId = tenantServiceAccessor.getTenantId();
        final List<Long> flownodesIds = flownodesToRestartByTenant.get(tenantId);

        logger.log(getClass(), TechnicalLogSeverity.INFO, "Restarting " + flownodesIds.size() + " flow nodes for tenant " + tenantId);
        try {
            final Iterator<Long> iterator = flownodesIds.iterator();
            do {
                transactionService.executeInTransaction(new ExecuteFlowNodes(tenantServiceAccessor, iterator));
            } while (iterator.hasNext());
        } catch (final Exception e) {
            throw new RestartException("Unable to restart elements", e);
        }

    }
}
