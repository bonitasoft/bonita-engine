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
package org.bonitasoft.engine.execution.work.failurewrapping;

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * Adding context information about Process definition to exception for better logging
 * 
 * @author Celine Souchet
 */
public class ProcessDefinitionContextWork extends TxInHandleFailureWrappingWork {

    private static final long serialVersionUID = 6958842321501639910L;

    private long processDefinitionId;

    /**
     * @param wrappedWork
     *            The work to wrap
     * @param processDefinitionId
     *            The identifier of the process definition
     */
    public ProcessDefinitionContextWork(final BonitaWork wrappedWork, final long processDefinitionId) {
        super(wrappedWork);
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    protected void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessDefinitionDeployInfo processDeploymentInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);

        sBonitaException.setProcessDefinitionIdOnContext(processDefinitionId);
        sBonitaException.setProcessDefinitionNameOnContext(processDeploymentInfo.getName());
        sBonitaException.setProcessDefinitionVersionOnContext(processDeploymentInfo.getVersion());
    }

    /**
     * @return The identifier of the process definition
     * @since 6.3
     */
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * @param processDefinitionId
     *            The identifier of the process definition
     * @since 6.3
     */
    protected void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
}
