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
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.work.WrappingBonitaWork;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Adding context information about Process definition and instance to exception for better logging
 * 
 * @author Celine Souchet
 */
public class ProcessInstanceContextWork extends TxInHandleFailureWrappingWork {

    private static final long serialVersionUID = 6958842321501639910L;

    private long processInstanceId;

    private long rootProcessInstanceId;

    /**
     * @param wrappedWork
     *            The work to wrap
     * @param processInstanceId
     *            The identifier of the process instance
     */
    public ProcessInstanceContextWork(final WrappingBonitaWork wrappedWork, final long processInstanceId) {
        this(wrappedWork, processInstanceId, -1);
    }

    /**
     * @param wrappedWork
     *            The work to wrap
     * @param processDefinitionId
     *            The identifier of the process definition
     * @param processInstanceId
     *            The identifier of the process instance
     * @param rootProcessInstanceId
     *            The identifier of the root process instance
     */
    public ProcessInstanceContextWork(final WrappingBonitaWork wrappedWork, final long processInstanceId, final long rootProcessInstanceId) {
        super(wrappedWork);
        this.processInstanceId = processInstanceId;
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    protected void setProcessInstanceId(final long id) {
        processInstanceId = id;
    }

    protected void setRootProcessInstanceId(final long id) {
        rootProcessInstanceId = id;
    }

    @Override
    protected void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) throws SBonitaException {
        if (rootProcessInstanceId < 0) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
            final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
            final SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
            rootProcessInstanceId = sProcessInstance.getRootProcessInstanceId();
        }
        sBonitaException.setProcessInstanceIdOnContext(processInstanceId);
        sBonitaException.setRootProcessInstanceIdOnContext(rootProcessInstanceId);
    }

}
