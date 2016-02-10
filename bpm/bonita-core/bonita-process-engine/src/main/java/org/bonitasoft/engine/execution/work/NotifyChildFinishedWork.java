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

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * Work that notify a container that a flow node is in completed state
 * e.g. when a flow node of a process finish we evaluate the outgoing transitions of this flow node.
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class NotifyChildFinishedWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = -8987586943379865375L;

    private final long processDefinitionId;

    private final long flowNodeInstanceId;

    private final String parentType;

    private final long parentId;

    NotifyChildFinishedWork(final long processDefinitionId, final long flowNodeInstanceId, final long parentId, final String parentType) {
        this.processDefinitionId = processDefinitionId;
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.parentId = parentId;
        this.parentType = parentType;
    }

    protected ClassLoader getClassLoader(final Map<String, Object> context) throws SBonitaException {
        return getTenantAccessor(context).getClassLoaderService().getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        final ClassLoader processClassloader = getClassLoader(context);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(processClassloader);
            final ContainerRegistry containerRegistry = getTenantAccessor(context).getContainerRegistry();
            containerRegistry.nodeReachedState(processDefinitionId, flowNodeInstanceId, parentId, parentType);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId:" + parentId + ", flowNodeInstanceId: " + flowNodeInstanceId;
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        WaitingEventsInterrupter waitingEventsInterrupter = new WaitingEventsInterrupter(tenantAccessor.getEventInstanceService(),
                tenantAccessor.getSchedulerService(), loggerService);
        FailedStateSetter failedStateSetter = new FailedStateSetter(waitingEventsInterrupter, tenantAccessor.getActivityInstanceService(),
                tenantAccessor.getFlowNodeStateManager(), loggerService);
        userTransactionService.executeInTransaction(new SetInFailCallable(failedStateSetter, flowNodeInstanceId));
    }

    @Override
    public String getRecoveryProcedure() {
        return "call processApi.executeFlowNode(" + flowNodeInstanceId + ")";
    }

    @Override
    public String toString() {
        return "Work[" + getDescription() + "]";
    }
}
