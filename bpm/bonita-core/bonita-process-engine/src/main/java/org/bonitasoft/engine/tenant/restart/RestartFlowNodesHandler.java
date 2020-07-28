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
import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.execution.work.ExecuteConnectorOfActivity;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodeWork;
import org.bonitasoft.engine.execution.work.NotifyChildFinishedWork;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.springframework.beans.factory.annotation.Qualifier;
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

    List<Long> flownodesToRestart = new ArrayList<>();
    private final TechnicalLogger logger;
    private final FlowNodeInstanceService flowNodeInstanceService;
    private final ExecuteFlowNodes executeFlowNodes;

    public RestartFlowNodesHandler(@Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            FlowNodeInstanceService flowNodeInstanceService,
            ExecuteFlowNodes executeFlowNodes) {
        this.logger = logger.asLogger(RestartFlowNodesHandler.class);
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.executeFlowNodes = executeFlowNodes;
    }

    @Override
    public void beforeServicesStart()
            throws RestartException {
        try {
            flownodesToRestart = new ArrayList<>();
            // using a too low page size (100) causes too many access to the database and causes timeout exception if there are lot of elements.
            // As we retrieve only the id we can use a greater page size
            QueryOptions queryOptions = new QueryOptions(0, 50000);
            List<Long> ids;
            logger.info("Start detecting flow nodes to restart...");
            do {
                ids = flowNodeInstanceService.getFlowNodeInstanceIdsToRestart(queryOptions);
                flownodesToRestart.addAll(ids);
                queryOptions = QueryOptions.getNextPage(queryOptions);
            } while (ids.size() == queryOptions.getNumberOfResults());
            logger.info("Found {} flow nodes to restart", flownodesToRestart.size());
        } catch (final SBonitaReadException e) {
            throw new RestartException("Unable to detect flow nodes that need to be restarted", e);
        }
    }

    @Override
    public void afterServicesStart() {
        logger.info("Restarting {} flow nodes", flownodesToRestart.size());
        executeFlowNodes.executeFlowNodes(flownodesToRestart);
        logger.info("All flow nodes to be restarted have been handled");
    }
}
