/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.restart;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodeWork;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodeWork.Type;
import org.bonitasoft.engine.execution.work.NotifyChildFinishedWork;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class RestartFlowsNodeHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        final WorkService workService = platformServiceAccessor.getWorkService();
        final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        try {
            final BPMInstanceBuilders bpmInstanceBuilders = tenantServiceAccessor.getBPMInstanceBuilders();
            QueryOptions queryOptions = QueryOptions.defaultQueryOptions();
            List<SFlowNodeInstance> sFlowNodeInstances;
            do {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "restarting flow nodes...");
                sFlowNodeInstances = activityInstanceService.getFlowNodeInstancesToRestart(queryOptions);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
                    if (sFlowNodeInstance.isTerminal()) {
                        // if it is terminal it means the notify was not called yet
                        logger.log(getClass(), TechnicalLogSeverity.INFO, "restarting flow node (Notify...) " + sFlowNodeInstance.getName() + ":"
                                + sFlowNodeInstance.getId());
                        workService.registerWork(new NotifyChildFinishedWork(sFlowNodeInstance.getProcessDefinitionId(), sFlowNodeInstance.getParentProcessInstanceId(), sFlowNodeInstance.getId(),
                                sFlowNodeInstance.getParentContainerId(), sFlowNodeInstance.getParentContainerType().name(), sFlowNodeInstance.getStateId()));
                    } else {
                        logger.log(getClass(), TechnicalLogSeverity.INFO, "restarting flow node (Execute..) " + sFlowNodeInstance.getName() + ":"
                                + sFlowNodeInstance.getId());
                        workService.registerWork(new ExecuteFlowNodeWork(Type.PROCESS, sFlowNodeInstance.getId(), null, null, sFlowNodeInstance
                                .getParentProcessInstanceId()));
                    }
                }
            } while (sFlowNodeInstances.size() == queryOptions.getNumberOfResults());
        } catch (final WorkRegisterException e) {
            handleException(e, "Unable to restart flowNodes: can't register work");
        } catch (final SBonitaException e) {
            handleException(e, "Unable to restart flowNodes: can't read flow nodes");
        }

    }

    private void handleException(final Exception e, final String message) throws RestartException {
        throw new RestartException(message, e);
    }
}
