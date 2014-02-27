/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work.failurehandling;

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Aurelien Pupier
 * Adding context information about Flow Node instance to exception for better logging
 */
public class FailureHandlingFlowNodeInstanceContextWork extends FailureHandlingBonitaWork {

	private static final long serialVersionUID = -8192129441020811731L;
	private final long flowNodeInstanceId;
	
	public FailureHandlingFlowNodeInstanceContextWork(BonitaWork wrappedWork, final long flowNodeInstanceId) {
		super(wrappedWork);
		this.flowNodeInstanceId = flowNodeInstanceId;
	}

	@Override
	protected void setExceptionContext(SBonitaException sBonitaException, Map<String, Object> context) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
                
        sBonitaException.setFlowNodeDefinitionId(flowNodeInstance.getFlowNodeDefinitionId());
        sBonitaException.setFlowNodeInstanceId(flowNodeInstanceId);
        sBonitaException.setFlowNodeName(flowNodeInstance.getName());
	}

}
