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

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.work.ExecuteConnectorOfProcess;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Restart handler for work {@link ExecuteConnectorOfProcess}
 *
 * @author Baptiste Mesta
 */
@Component
public class RestartProcessHandler implements TenantRestartHandler {

    private final TechnicalLogger logger;
    private final ProcessInstanceService processInstanceService;
    private final ExecuteProcesses executeProcesses;
    //the handler is executed on one tenant only but we keep a map by tenant because this class is a singleton
    //It should not be a singleton but have a factory to create it
    private List<Long> processInstanceIdsToRestart = new ArrayList<>();

    public RestartProcessHandler(@Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            ProcessInstanceService processInstanceService,
            ExecuteProcesses executeProcesses) {
        this.logger = logger.asLogger(RestartProcessHandler.class);
        this.processInstanceService = processInstanceService;
        this.executeProcesses = executeProcesses;
    }

    @Override
    public void beforeServicesStart() throws RestartException {
        logger.info("Start detecting processes to restart...");
        processInstanceIdsToRestart = getAllProcessInstancesToRestart(processInstanceService);
        logger.info("Found {} process to restart", processInstanceIdsToRestart.size());
    }

    @Override
    public void afterServicesStart() {
        // get all process in initializing
        // call executeConnectors ON_ENTER on them (only case they can be in initializing)
        // get all process in completing
        // call executeConnectors ON_FINISH on them (only case they can be in completing)
        // also completed, aborted, cancelled

        logger.info("Attempting to restart {} processes", processInstanceIdsToRestart.size());
        executeProcesses.execute(processInstanceIdsToRestart);
        logger.info("All processes to be restarted have been handled");
    }

    public List<Long> getAllProcessInstancesToRestart(ProcessInstanceService processInstanceService)
            throws RestartException {
        final List<Long> ids = new ArrayList<>();
        QueryOptions queryOptions = new QueryOptions(0, 1000, SProcessInstance.class, "id", OrderByType.ASC);
        try {
            List<SProcessInstance> processInstances;
            do {
                processInstances = processInstanceService.getProcessInstancesInStates(queryOptions,
                        ProcessInstanceState.INITIALIZING,
                        ProcessInstanceState.COMPLETING, ProcessInstanceState.COMPLETED,
                        ProcessInstanceState.ABORTED, ProcessInstanceState.CANCELLED);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SProcessInstance sProcessInstance : processInstances) {
                    ids.add(sProcessInstance.getId());
                }
            } while (processInstances.size() == queryOptions.getNumberOfResults());
        } catch (final SProcessInstanceReadException e) {
            throw new RestartException("Unable to restart process: can't read process instances", e);
        }
        return ids;
    }

}
