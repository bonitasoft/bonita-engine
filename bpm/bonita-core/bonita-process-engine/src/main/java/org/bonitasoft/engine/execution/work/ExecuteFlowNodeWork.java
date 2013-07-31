/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.execution.ContainerExecutor;
import org.bonitasoft.engine.work.TxBonitaWork;

/**
 * @author Baptiste Mesta
 */
public class ExecuteFlowNodeWork extends TxBonitaWork {

    private final ContainerExecutor containerExecutor;

    private final long flowNodeInstanceId;

    private final List<SOperation> operations;

    private final SExpressionContext contextDependency;

    private final Long processInstanceId;

    public ExecuteFlowNodeWork(final ContainerExecutor containerExecutor, final long flowNodeInstanceId, final List<SOperation> operations,
            final SExpressionContext contextDependency, final long processInstanceId) {
        this.containerExecutor = containerExecutor;
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.operations = operations;
        this.contextDependency = contextDependency;
        this.processInstanceId = processInstanceId;
    }

    @Override
    protected void work() throws Exception {
        containerExecutor.executeFlowNode(flowNodeInstanceId, contextDependency, operations, processInstanceId, null, null);
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId:" + processInstanceId + ", flowNodeInstanceId: " + flowNodeInstanceId;
    }
}
