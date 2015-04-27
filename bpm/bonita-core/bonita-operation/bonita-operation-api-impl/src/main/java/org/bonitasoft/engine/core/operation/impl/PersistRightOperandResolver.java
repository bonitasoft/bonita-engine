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

package org.bonitasoft.engine.core.operation.impl;

import java.util.List;

import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Elias Ricken de Medeiros
 */
public class PersistRightOperandResolver {

    private OperationsAnalyzer operationsAnalyzer;

    public PersistRightOperandResolver(final OperationsAnalyzer operationsAnalyzer) {
        this.operationsAnalyzer = operationsAnalyzer;
    }

    public boolean shouldPersist(int currentIndex, List<SOperation> operations) {
        SOperation currentOperation = operations.get(currentIndex);
        if (!SLeftOperand.TYPE_BUSINESS_DATA.equals(currentOperation.getLeftOperand().getType())) {
            return false;
        }
        LeftOperandIndexes leftOperandIndexes = operationsAnalyzer.calculateIndexes(currentIndex, operations);
        if (currentIndex == leftOperandIndexes.getLastIndex()) {
            return true;
        }
        //get the index of next operation that will use the current left operand as dependency to set another business data
        int dependencyIndex = operationsAnalyzer.findBusinessDataDependencyIndex(currentOperation.getLeftOperand().getName(), currentIndex + 1, operations);
        return dependencyIndex != -1 && dependencyIndex < leftOperandIndexes.getNextIndex();
    }

}
