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
package org.bonitasoft.engine.execution;

import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.flownode.SearchFlowNodeInstances;
import org.bonitasoft.engine.api.impl.transaction.flownode.SetFlowNodeStateCategory;
import org.bonitasoft.engine.api.impl.transaction.process.SetProcessStateCategory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransactionalProcessInstanceInterruptor extends AbstractProcessInstanceInterruptor {

    private final ProcessInstanceService processInstanceService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final ProcessExecutor processExecutor;

    private long count;

    public TransactionalProcessInstanceInterruptor(final ProcessInstanceService processInstanceService, final FlowNodeInstanceService flowNodeInstanceService,
            final ProcessExecutor processExecutor, final TechnicalLoggerService logger) {
        super(logger);
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processExecutor = processExecutor;
    }

    @Override
    protected void setProcessStateCategory(final long processInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        final SetProcessStateCategory setProcessStateCategoryTransaction = new SetProcessStateCategory(processInstanceService, processInstanceId, stateCategory);
        setProcessStateCategoryTransaction.execute();
    }

    @Override
    protected void resumeChildExecution(final long flowNodeInstanceId, final long processInstanceId, final long userId) throws SFlowNodeReadException,
            SFlowNodeExecutionException {
        // no need to handle failed state, all is in the same tx
        processExecutor.executeFlowNode(flowNodeInstanceId, null, null, processInstanceId, userId, userId);
    }

    @Override
    protected List<SFlowNodeInstance> getChildren(final long processInstanceId) throws SBonitaException {
        final SearchFlowNodeInstances<SFlowNodeInstance> searchFlowNodeInstancesTransaction = new SearchFlowNodeInstances<SFlowNodeInstance>(
                flowNodeInstanceService, getQueryOptions(processInstanceId), SFlowNodeInstance.class);
        searchFlowNodeInstancesTransaction.execute();
        final List<SFlowNodeInstance> children = searchFlowNodeInstancesTransaction.getResult();
        count = searchFlowNodeInstancesTransaction.getCount();
        return children;
    }

    @Override
    protected List<SFlowNodeInstance> getChildrenExcept(final long processInstanceId, final long childExceptionId) throws SBonitaException {
        final SearchFlowNodeInstances<SFlowNodeInstance> searchFlowNodeInstancesTransaction = new SearchFlowNodeInstances<SFlowNodeInstance>(
                flowNodeInstanceService, getQueryOptions(processInstanceId, childExceptionId), SFlowNodeInstance.class);
        searchFlowNodeInstancesTransaction.execute();
        final List<SFlowNodeInstance> children = searchFlowNodeInstancesTransaction.getResult();
        count = searchFlowNodeInstancesTransaction.getCount();
        return children;
    }

    @Override
    protected long getNumberOfChildren(final long processInstanceId) {
        return count;
    }

    @Override
    protected long getNumberOfChildrenExcept(final long processInstanceId, final long childExceptionId) {
        return count;
    }

    @Override
    protected void setChildStateCategory(final long flowNodeInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        final SetFlowNodeStateCategory setFlowNodeStateCategoryTransaction = new SetFlowNodeStateCategory(flowNodeInstanceService, flowNodeInstanceId,
                stateCategory);
        setFlowNodeStateCategoryTransaction.execute();
    }

}
