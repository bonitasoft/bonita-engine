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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * Restart flow nodes for works: {@link ExecuteFlowNodeWork} {@link ExecuteConnectorOfActivity} {@link NotifyChildFinishedWork}
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class RestartFlowNodesHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        final WorkService workService = tenantServiceAccessor.getWorkService();
        final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        try {
            QueryOptions queryOptions = QueryOptions.defaultQueryOptions();
            List<SFlowNodeInstance> sFlowNodeInstances;
            logInfo(logger, "Restarting flow nodes...");
            do {
                // TODO: unitary test query under getFlowNodeInstancesToRestart():
                sFlowNodeInstances = activityInstanceService.getFlowNodeInstancesToRestart(queryOptions);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
                    if (sFlowNodeInstance.isTerminal()) {
                        createNotifyChildFinishedWork(workService, logger, sFlowNodeInstance);
                    } else {
                        createExecuteFlowNodeWork(workService, logger, sFlowNodeInstance);
                    }
                }
            } while (sFlowNodeInstances.size() == queryOptions.getNumberOfResults());
        } catch (final SWorkRegisterException e) {
            throw new RestartException("Unable to restart flowNodes: can't register work", e);
        } catch (final SBonitaException e) {
            throw new RestartException("Unable to restart flowNodes: can't read flow nodes", e);
        }
    }

    private void logInfo(final TechnicalLoggerService logger, final String message) {
        final boolean isInfo = logger.isLoggable(getClass(), TechnicalLogSeverity.INFO);
        if (isInfo) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, message);
        }
    }

    private void createExecuteFlowNodeWork(final WorkService workService, final TechnicalLoggerService logger, final SFlowNodeInstance sFlowNodeInstance)
            throws SWorkRegisterException {
        logInfo(logger, "Restarting flow node (Execute ...) with name = <" + sFlowNodeInstance.getName() + ">, and id = <" + sFlowNodeInstance.getId()
                + "> in state = <" + sFlowNodeInstance.getStateName() + ">");
        // ExecuteFlowNodeWork and ExecuteConnectorOfActivityWork
        workService.registerWork(WorkFactory.createExecuteFlowNodeWork(sFlowNodeInstance.getProcessDefinitionId(),
                sFlowNodeInstance.getParentProcessInstanceId(), sFlowNodeInstance.getId(), null, null));
    }

    private void createNotifyChildFinishedWork(final WorkService workService, final TechnicalLoggerService logger, final SFlowNodeInstance sFlowNodeInstance)
            throws SWorkRegisterException {
        logInfo(logger, "Restarting flow node (Notify finished...) with name = <" + sFlowNodeInstance.getName() + ">, and id = <" + sFlowNodeInstance.getId()
                + " in state = <" + sFlowNodeInstance.getStateName() + ">");
        // NotifyChildFinishedWork, if it is terminal it means the notify was not called yet
        workService.registerWork(WorkFactory.createNotifyChildFinishedWork(sFlowNodeInstance.getProcessDefinitionId(), sFlowNodeInstance
                .getParentProcessInstanceId(), sFlowNodeInstance.getId(), sFlowNodeInstance.getParentContainerId(), sFlowNodeInstance.getParentContainerType()
                .name()));
    }

}
