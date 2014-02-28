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
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Celine Souchet
 * 
 */
public class FailureHandlingProcessDefinitionContextWork extends FailureHandlingBonitaWork {

    private static final long serialVersionUID = 6958842321501639910L;

    private long processDefinitionId;


	/**
     * @param wrappedWork
     * @param processDefinitionId
     *            The identifier of the process definition
     */
    public FailureHandlingProcessDefinitionContextWork(final BonitaWork wrappedWork, final long processDefinitionId) {
        super(wrappedWork);
        this.processDefinitionId = processDefinitionId;
    }
    
    /**
     * @param wrappedWork
     * Take care to call setProcessDefitionID later on
     */
    protected FailureHandlingProcessDefinitionContextWork(final BonitaWork wrappedWork){
    	 super(wrappedWork);
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processDefinitionId = " + processDefinitionId;
    }

    @Override
    protected void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessDefinitionDeployInfo processDeploymentInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);

        sBonitaException.setProcessDefinitionId(processDefinitionId);
        sBonitaException.setProcessDefinitionName(processDeploymentInfo.getName());
        sBonitaException.setProcessDefinitionVersion(processDeploymentInfo.getVersion());
    }

    public void setProcessDefinitionId(long processDefinitionId) {
    	this.processDefinitionId = processDefinitionId;
    }
}
