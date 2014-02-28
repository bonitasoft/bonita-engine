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
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Celine Souchet
 * 
 */
public class FailureHandlingProcessInstanceContextWork extends FailureHandlingProcessDefinitionContextWork {

    private static final long serialVersionUID = 6958842321501639910L;

    private final long processInstanceId;

    /**
     * @param wrappedWork
     * @param processInstanceId
     *            The identifier of the process instance
     */
    public FailureHandlingProcessInstanceContextWork(final BonitaWork wrappedWork, final long processInstanceId) {
        super(wrappedWork);
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId = " + processInstanceId;
    }

    @Override
    protected void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) throws SBonitaException {
    	sBonitaException.setProcessInstanceId(processInstanceId);

    	final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
    	final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
    	final SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
    	setProcessDefinitionId(sProcessInstance.getProcessDefinitionId());
    	sBonitaException.setRootProcessInstanceId(sProcessInstance.getRootProcessInstanceId());
    	super.setExceptionContext(sBonitaException, context);
    }
    
}
