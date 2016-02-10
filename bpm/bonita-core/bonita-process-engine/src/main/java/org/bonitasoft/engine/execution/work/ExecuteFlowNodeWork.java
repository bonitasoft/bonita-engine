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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * Work that is responsible of executing a flow node.
 * If the execution fails it will put the flow node in failed state
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteFlowNodeWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = -5873526992671300038L;

    public static enum Type {
        PROCESS,
        FLOWNODE;
    }

    private final long flowNodeInstanceId;

    private final List<SOperation> operations;

    private final SExpressionContext contextDependency;

    private final long processInstanceId;

    ExecuteFlowNodeWork(final long flowNodeInstanceId, final List<SOperation> operations, final SExpressionContext contextDependency,
            final long processInstanceId) {
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.operations = operations;
        this.contextDependency = contextDependency;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId:" + processInstanceId + ", flowNodeInstanceId: " + flowNodeInstanceId;
    }

    @Override
    public String getRecoveryProcedure() {
        return "call processApi.executeFlowNode(" + flowNodeInstanceId + ")";
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        tenantAccessor.getFlowNodeExecutor().executeFlowNode(flowNodeInstanceId, contextDependency, operations, processInstanceId, null, null);
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
    public String toString() {
        return "Work[" + getDescription() + "]";
    }

}
