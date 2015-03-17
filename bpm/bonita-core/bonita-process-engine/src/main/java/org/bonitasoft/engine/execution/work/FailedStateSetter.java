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

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class FailedStateSetter {

    private final FlowNodeExecutor flowNodeExecutor;

    private final ActivityInstanceService activityInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;
    private final TechnicalLoggerService loggerService;

    public FailedStateSetter(final FlowNodeExecutor flowNodeExecutor, final ActivityInstanceService activityInstanceService,
            final FlowNodeStateManager flowNodeStateManager, TechnicalLoggerService loggerService) {
        this.flowNodeExecutor = flowNodeExecutor;
        this.activityInstanceService = activityInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
        this.loggerService = loggerService;
    }

    public void setAsFailed(long flowNodeInstanceId) throws SFlowNodeReadException, SArchivingException, SFlowNodeModificationException {
        final SFlowNodeInstance flowNodeInstance;
        try {
            flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
            setAsFailed(flowNodeInstance);
        } catch (SFlowNodeNotFoundException e) {
            if (loggerService.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Impossible to put flow node instance in failed state: flow node instance with id '" + flowNodeInstanceId + "' not found.");
            }
        }
    }

    private void setAsFailed(final SFlowNodeInstance flowNodeInstance) throws SArchivingException, SFlowNodeModificationException {
        final long processDefinitionId = flowNodeInstance.getProcessDefinitionId();
        flowNodeExecutor.archiveFlowNodeInstance(flowNodeInstance, false, processDefinitionId);
        activityInstanceService.setState(flowNodeInstance, flowNodeStateManager.getFailedState());
    }

}
