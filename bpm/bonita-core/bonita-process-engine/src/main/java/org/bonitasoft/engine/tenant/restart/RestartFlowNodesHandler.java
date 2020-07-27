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
package org.bonitasoft.engine.tenant.restart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.execution.work.ExecuteConnectorOfActivity;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodeWork;
import org.bonitasoft.engine.execution.work.NotifyChildFinishedWork;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Restart flow nodes for works: {@link ExecuteFlowNodeWork} {@link ExecuteConnectorOfActivity}
 * {@link NotifyChildFinishedWork}
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
@Component
public class RestartFlowNodesHandler implements TenantRestartHandler {

    //the handler is executed on one tenant only but we keep a map by tenant because this class is a singleton
    //It should not be a singleton but have a factory to create it
    final Map<Long, List<Long>> flownodesToRestartByTenant = new HashMap<>();
    private Long tenantId;
    private TechnicalLoggerService logger;
    private FlowNodeInstanceService flowNodeInstanceService;
    private UserTransactionService transactionService;
    private ExecuteFlowNodes executeFlowNodes;

    public RestartFlowNodesHandler(@Value("${tenantId}") Long tenantId,
            @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            FlowNodeInstanceService flowNodeInstanceService,
            UserTransactionService transactionService,
            ExecuteFlowNodes executeFlowNodes) {
        this.tenantId = tenantId;
        this.logger = logger;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.transactionService = transactionService;
        this.executeFlowNodes = executeFlowNodes;
    }

    @Override
    public void beforeServicesStart()
            throws RestartException {
        flownodesToRestartByTenant.clear();
        try {
            final ArrayList<Long> flownodesToRestart = new ArrayList<>();
            flownodesToRestartByTenant.put(tenantId, flownodesToRestart);

            // using a too low page size (100) causes too many access to the database and causes timeout exception if there are lot of elements.
            // As we retrieve only the id we can use a greater page size
            QueryOptions queryOptions = new QueryOptions(0, 50000);
            List<Long> ids;
            logInfo("Start detecting flow nodes to restart on tenant " + tenantId + "...");
            do {
                ids = flowNodeInstanceService.getFlowNodeInstanceIdsToRestart(queryOptions);
                flownodesToRestart.addAll(ids);
                queryOptions = QueryOptions.getNextPage(queryOptions);

            } while (ids.size() == queryOptions.getNumberOfResults());
            logInfo("Found " + flownodesToRestart.size() + " flow nodes to restart on tenant " + tenantId);
        } catch (final SBonitaReadException e) {
            throw new RestartException("Unable to detect flow nodes as to be restarted on tenant " + tenantId, e);
        }
    }

    private void logInfo(final String message) {
        if (logger.isLoggable(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO)) {
            logger.log(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO, message);
        }
    }

    @Override
    public void afterServicesStart() {
        final List<Long> flownodesIds = flownodesToRestartByTenant.get(tenantId);
        logInfo("Restarting " + flownodesIds.size() + " flow nodes for tenant " + tenantId);
        executeFlowNodes.executeFlowNodes(flownodesIds);
        logInfo("All flow nodes to be restarted on tenant " + tenantId + " have been handled");
    }
}
