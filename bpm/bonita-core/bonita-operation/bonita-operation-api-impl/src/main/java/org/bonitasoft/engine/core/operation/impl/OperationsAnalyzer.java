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
 */

package org.bonitasoft.engine.core.operation.impl;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 */
public class OperationsAnalyzer {

    /**
     * Finds the index of operation of type {@link SLeftOperand#TYPE_BUSINESS_DATA} that references the expression of type
     * {@link ExpressionType#TYPE_BUSINESS_DATA} with given name directly on the right operand or in transitive dependencies.
     * 
     * @param businessDataName the expression content
     * @param fromIndex index of operation from which the analyse must begins
     * @param operations list of operations @return the index of operation that references the expression with given name and type.
     */
    public int findBusinessDataDependencyIndex(String businessDataName, int fromIndex,
            final List<SOperation> operations) {
        if (fromIndex >= operations.size()) {
            return -1;
        }
        for (int i = fromIndex; i < operations.size(); i++) {
            SOperation operation = operations.get(i);
            SExpression rightOperand = operation.getRightOperand();
            if (matches(businessDataName, operation, rightOperand)) {
                return i;
            }
        }
        return -1;
    }

    private boolean matches(final String businessDataName, final SOperation operation, final SExpression expression) {
        if (expression == null) {
            return false;
        }
        boolean matches = SLeftOperand.TYPE_BUSINESS_DATA.equals(operation.getLeftOperand().getType()) && businessDataName.equals(expression.getContent())
                && ExpressionType.TYPE_BUSINESS_DATA.name().equals(expression.getExpressionType());
        if (!matches && expression.hasDependencies()) {
            Iterator<SExpression> iterator = expression.getDependencies().iterator();
            while (iterator.hasNext() && !matches) {
                matches = matches(businessDataName, operation, iterator.next());
            }
        }
        return matches;
    }

    /**
     * Calculates the next and the last indexes where the left operand located at the given index is used
     * 
     * @param indexOfCurrentOperation index of current operation
     * @param operations all operations
     * @return the next and the last indexes where the left operand located at the given index is used
     */
    public LeftOperandIndexes calculateIndexes(int indexOfCurrentOperation, List<SOperation> operations) {
        LeftOperandIndexes indexes = new LeftOperandIndexes();
        indexes.setLastIndex(indexOfCurrentOperation);
        SOperation currentOperation = operations.get(indexOfCurrentOperation);

        //start from operation that follows the current one
        for (int i = indexOfCurrentOperation + 1; i < operations.size(); i++) {
            SOperation operation = operations.get(i);
            if (operation.getLeftOperand().equals(currentOperation.getLeftOperand())) {
                indexes.setLastIndex(i);
                if (indexes.getNextIndex() == -1) {
                    indexes.setNextIndex(i);
                }
            }
        }
        return indexes;
    }

}
