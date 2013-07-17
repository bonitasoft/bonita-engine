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

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.event.OperationsWithContext;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.TxBonitaWork;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class InstantiateProcessWork extends TxBonitaWork {

    private final OperationsWithContext operations;

    private final ProcessExecutor processExecutor;

    private final SProcessDefinition processDefinition;

    private long callerId = -1;

    private long subProcessId = -1;

    private Long idOfTheProcessToInterrupt;

    private SFlowNodeInstance subProcflowNodeInstance;

    private final ProcessInstanceService processInstanceService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final LockService lockService;

    private final TechnicalLoggerService logger;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final TransactionExecutor transactionExecutor;

    public InstantiateProcessWork(final SProcessDefinition processDefinition, final OperationsWithContext operations, final ProcessExecutor processExecutor,
            final ProcessInstanceService processInstanceService, final FlowNodeInstanceService flowNodeInstanceService, final LockService lockService,
            final TechnicalLoggerService logger, final BPMInstancesCreator bpmInstancesCreator, final TransactionExecutor transactionExecutor) {
        this.processDefinition = processDefinition;
        this.operations = operations;
        this.processExecutor = processExecutor;
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.lockService = lockService;
        this.logger = logger;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.transactionExecutor = transactionExecutor;
    }

    @Override
    protected void work() throws SBonitaException {
        if (idOfTheProcessToInterrupt != null) {
            final TransactionalProcessInstanceInterruptor interruptor = new TransactionalProcessInstanceInterruptor(
                    bpmInstancesCreator.getBPMInstanceBuilders(), processInstanceService, flowNodeInstanceService, processExecutor, lockService, logger);
            interruptor.interruptProcessInstance(idOfTheProcessToInterrupt, SStateCategory.ABORTING, -1, subProcflowNodeInstance.getId());
        }
        processExecutor.start(0, processDefinition, getExpressionContext(), operations.getOperations(), null, null, callerId, subProcessId);
    }

    private SExpressionContext getExpressionContext() {
        return operations.getContext();
    }

    public void setCallerId(final long callerId) {
        this.callerId = callerId;
    }

    public void setSubProcessId(final long subProcessId) {
        this.subProcessId = subProcessId;
    }

    public void setSubProcflowNodeInstance(final SFlowNodeInstance subProcflowNodeInstance) {
        this.subProcflowNodeInstance = subProcflowNodeInstance;
    }

    public void setIdOfTheProcessToInterrupt(final Long idOfTheProcessToInterrupt) {
        this.idOfTheProcessToInterrupt = idOfTheProcessToInterrupt;
    }

    @Override
    protected String getDescription() {
        return getClass().getSimpleName() + ": Process of type " + processDefinition.getName() + " (" + processDefinition.getVersion() + ")"
                + ((subProcflowNodeInstance != null) ? ", subProcflowNodeInstanceId:" + subProcflowNodeInstance.getId() : "");
    }
}
