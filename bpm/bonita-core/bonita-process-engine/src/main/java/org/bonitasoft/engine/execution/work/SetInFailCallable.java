/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import java.util.concurrent.Callable;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class SetInFailCallable implements Callable<Void> {

    /**
     * 
     */

    private final FlowNodeExecutor flowNodeExecutor;

    private final ActivityInstanceService activityInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;

    private final long flowNodeInstanceId;

    SetInFailCallable(FlowNodeExecutor flowNodeExecutor, ActivityInstanceService activityInstanceService,
            FlowNodeStateManager flowNodeStateManager, long flowNodeInstanceId) {
        this.flowNodeExecutor = flowNodeExecutor;
        this.activityInstanceService = activityInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
        this.flowNodeInstanceId = flowNodeInstanceId;
    }

    @Override
    public Void call() throws Exception {
        final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        long processDefinitionId = flowNodeInstance.getProcessDefinitionId();
        flowNodeExecutor.archiveFlowNodeInstance(flowNodeInstance, false, processDefinitionId);
        activityInstanceService.setState(flowNodeInstance, flowNodeStateManager.getFailedState());
        return null;
    }
}
