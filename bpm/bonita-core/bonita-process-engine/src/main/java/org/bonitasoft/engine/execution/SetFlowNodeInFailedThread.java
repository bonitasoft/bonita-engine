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
package org.bonitasoft.engine.execution;

import java.io.IOException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Charles Souillard
 */
public class SetFlowNodeInFailedThread extends AbstractSetInFailedThread {

    // local parameters
    private final long flowNodeInstanceId;

    private final long processDefinitionId;

    private final FlowNodeExecutor flowNodeExecutor;

    // output parameters
    private long parentFlowNodeInstanceId;

    private String flowNodeInstanceName;

    public SetFlowNodeInFailedThread(final long flowNodeInstanceId, final long processDefinitionId, final FlowNodeExecutor flowNodeExecutor)
            throws STenantIdNotSetException, BonitaHomeNotSetException, BonitaHomeConfigurationException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException {
        super();
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.flowNodeExecutor = flowNodeExecutor;
    }

    @Override
    protected void setInFail() throws SBonitaException {
        final ActivityInstanceService activityInstanceService = getTenantServiceAccessor().getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = getTenantServiceAccessor().getFlowNodeStateManager();

        final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        parentFlowNodeInstanceId = flowNodeInstance.getParentProcessInstanceId();
        flowNodeInstanceName = flowNodeInstance.getName();
        flowNodeExecutor.archiveFlowNodeInstance(flowNodeInstance, false, processDefinitionId);
        activityInstanceService.setState(flowNodeInstance, flowNodeStateManager.getFailedState());
    }

    public String getFlowNodeInstanceName() {
        return flowNodeInstanceName;
    }

    public long getParentFlowNodeInstanceId() {
        return parentFlowNodeInstanceId;
    }
}
