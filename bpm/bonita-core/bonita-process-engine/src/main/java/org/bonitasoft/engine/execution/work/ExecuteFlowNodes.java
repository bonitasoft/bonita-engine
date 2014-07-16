/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 ** 
 * @since 6.2
 */
package org.bonitasoft.engine.execution.work;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

public class ExecuteFlowNodes implements Callable<Object> {

    private final WorkService workService;

    private final TechnicalLoggerService logger;

    private final ActivityInstanceService activityInstanceService;

    private final Iterator<Long> iterator;

    public ExecuteFlowNodes(final WorkService workService, final TechnicalLoggerService logger, final ActivityInstanceService activityInstanceService,
            final Iterator<Long> iterator) {
        this.workService = workService;
        this.logger = logger;
        this.activityInstanceService = activityInstanceService;
        this.iterator = iterator;
    }

    @Override
    public Object call() throws Exception {
        try {
            for (int i = 0; i < 20 && iterator.hasNext(); i++) {
                SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(iterator.next());
                if (flowNodeInstance.isTerminal()) {
                    createNotifyChildFinishedWork(workService, logger, flowNodeInstance);
                } else {
                    createExecuteFlowNodeWork(workService, logger, flowNodeInstance);
                }
            }
            return null;
        } catch (final SWorkRegisterException e) {
            throw new RestartException("Unable to restart flowNodes: can't register work", e);
        } catch (final SBonitaException e) {
            throw new RestartException("Unable to restart flowNodes: can't read flow nodes", e);
        }
    }

    void createExecuteFlowNodeWork(final WorkService workService, final TechnicalLoggerService logger, final SFlowNodeInstance sFlowNodeInstance)
            throws SWorkRegisterException {
        logInfo(logger, "Restarting flow node (Execute ...) with name = <" + sFlowNodeInstance.getName() + ">, and id = <" + sFlowNodeInstance.getId()
                + "> in state = <" + sFlowNodeInstance.getStateName() + ">");
        // ExecuteFlowNodeWork and ExecuteConnectorOfActivityWork
        workService.registerWork(WorkFactory.createExecuteFlowNodeWork(sFlowNodeInstance.getProcessDefinitionId(),
                sFlowNodeInstance.getParentProcessInstanceId(), sFlowNodeInstance.getId(), null, null));
    }

    void createNotifyChildFinishedWork(final WorkService workService, final TechnicalLoggerService logger, final SFlowNodeInstance sFlowNodeInstance)
            throws SWorkRegisterException {
        logInfo(logger, "Restarting flow node (Notify finished...) with name = <" + sFlowNodeInstance.getName() + ">, and id = <" + sFlowNodeInstance.getId()
                + " in state = <" + sFlowNodeInstance.getStateName() + ">");
        // NotifyChildFinishedWork, if it is terminal it means the notify was not called yet
        workService.registerWork(WorkFactory.createNotifyChildFinishedWork(sFlowNodeInstance.getProcessDefinitionId(), sFlowNodeInstance
                .getParentProcessInstanceId(), sFlowNodeInstance.getId(), sFlowNodeInstance.getParentContainerId(), sFlowNodeInstance.getParentContainerType()
                .name()));
    }

    private void logInfo(final TechnicalLoggerService logger, final String message) {
        final boolean isInfo = logger.isLoggable(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO);
        if (isInfo) {
            logger.log(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO, message);
        }
    }
}
