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
package org.bonitasoft.engine.execution.event;

import java.io.Serializable;
import java.util.List;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class OperationsWithContext implements Serializable {

    private static final long serialVersionUID = 6034976719148086546L;

    private final SExpressionContext context;

    private final List<SOperation> operations;

    private final String containerType;

    /**
     * @param context
     * @param operations
     */
    public OperationsWithContext(final SExpressionContext context, final List<SOperation> operations) {
        this.context = context;
        this.operations = operations;
        containerType = SFlowElementsContainerType.PROCESS.name();
    }

    /**
     * @param object
     * @param object2
     * @param containerType
     */
    public OperationsWithContext(final SExpressionContext context, final List<SOperation> operations, final String containerType) {
        this.context = context;
        this.operations = operations;
        this.containerType = containerType;
    }

    /**
     * @return the context
     */
    public SExpressionContext getContext() {
        return context;
    }

    /**
     * @return the operations
     */
    public List<SOperation> getOperations() {
        return operations;
    }

    /**
     * @return
     */
    public String getContainerType() {
        return containerType;
    }

}
