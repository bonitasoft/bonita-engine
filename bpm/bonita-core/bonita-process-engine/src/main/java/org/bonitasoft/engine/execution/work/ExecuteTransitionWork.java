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
package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ExecuteTransitionWork extends TxLockProcessInstanceWork {

    private static final long serialVersionUID = 3875386133862872479L;

    private final long processDefinitionId;

    private final long transitionInstanceId;

    public ExecuteTransitionWork(final long processDefinitionId, final long processInstanceId, final long transitionInstanceId) {
    	super(processInstanceId);
        this.processDefinitionId = processDefinitionId;
        this.transitionInstanceId = transitionInstanceId;
    }

    @Override
    protected void work() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SProcessDefinition sProcessDefinition = tenantAccessor.getProcessDefinitionService().getProcessDefinition(processDefinitionId);
        final STransitionInstance sTransitionInstance = tenantAccessor.getTransitionInstanceService().get(transitionInstanceId);
        tenantAccessor.getProcessExecutor().executeTransition(sProcessDefinition, sTransitionInstance);
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": transitionInstanceId:" + transitionInstanceId;
    }

}
