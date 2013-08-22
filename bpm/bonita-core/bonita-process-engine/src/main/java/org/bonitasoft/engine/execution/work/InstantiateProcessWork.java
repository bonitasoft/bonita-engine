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

import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.OperationsWithContext;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class InstantiateProcessWork extends TxBonitaWork {

    private static final long serialVersionUID = 4030451057251328099L;

    private final OperationsWithContext operations;

    private final long processDefinitionId;

    private long callerId = -1;

    private long subProcessId = -1;

    private long subProcflowNodeInstanceId = -1;

    private long targetSFlowNodeDefinitionId = -1;

    private long processToInterruptId = -1;

    public InstantiateProcessWork(final long processDefinitionId, final OperationsWithContext operations) {
        this.processDefinitionId = processDefinitionId;
        this.operations = operations;
    }

    @Override
    protected void work() throws Exception {
        if (processToInterruptId != -1) {
            final TransactionalProcessInstanceInterruptor interruptor = getTenantAccessor().getTransactionalProcessInstanceInterruptor();
            interruptor.interruptProcessInstance(processToInterruptId, SStateCategory.ABORTING, -1, subProcflowNodeInstanceId);
        }

        final ProcessExecutor processExecutor = getTenantAccessor().getProcessExecutor();
        processExecutor.start(processDefinitionId, targetSFlowNodeDefinitionId, 0, 0, operations.getContext(), operations.getOperations(),
                null, null, callerId, subProcessId);
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": Process definition with id " + processDefinitionId
                + (subProcflowNodeInstanceId != -1 ? ", subProcflowNodeInstanceId:" + subProcflowNodeInstanceId : "");
    }

    public void setCallerId(final long callerId) {
        this.callerId = callerId;
    }

    public void setSubProcessId(final long subProcessId) {
        this.subProcessId = subProcessId;
    }

    public void setSubProcflowNodeInstanceId(final long subProcflowNodeInstanceId) {
        this.subProcflowNodeInstanceId = subProcflowNodeInstanceId;
    }

    public void setProcessToInterruptId(final long processToInterruptId) {
        this.processToInterruptId = processToInterruptId;
    }

    public void setTargetSFlowNodeDefinitionId(long targetSFlowNodeDefinitionId) {
        this.targetSFlowNodeDefinitionId = targetSFlowNodeDefinitionId;
    }

}
