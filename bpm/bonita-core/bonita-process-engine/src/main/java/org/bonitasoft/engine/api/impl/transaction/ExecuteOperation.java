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
package org.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Zhang Bole
 */
public class ExecuteOperation implements TransactionContent {

    private final OperationService operationService;

    private final SOperation operation;

    private final long containerId;

    private final String containerType;

    private final SExpressionContext expressionContexts;

    public ExecuteOperation(final OperationService operationService, final long containerId, final String containerType, final SOperation operation,
            final SExpressionContext expressionContexts) {
        this.operationService = operationService;
        this.containerId = containerId;
        this.containerType = containerType;
        this.operation = operation;
        this.expressionContexts = expressionContexts;
    }

    @Override
    public void execute() throws SBonitaException {
        operationService.execute(operation, containerId, containerType, expressionContexts);
    }
}
