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
package org.bonitasoft.engine.expression.impl.condition;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.expression.impl.ConditionExpressionExecutorStrategy;

/**
 * @author Elias Ricken de Medeiros
 */
public class BinaryComparatorMapper {

    private Map<String, BinaryComparator> evaluators;

    public BinaryComparatorMapper(EqualityComparator equalityComparator, InequalityComparator inequalityComparator) {
        evaluators = new HashMap<String, BinaryComparator>();
        evaluators.put(ConditionExpressionExecutorStrategy.EQUALS_COMPARATOR, new EqualsComparator(equalityComparator));
        evaluators.put(ConditionExpressionExecutorStrategy.NOT_EQUALS_COMPARATOR, new DifferentComparator(equalityComparator));
        evaluators.put(ConditionExpressionExecutorStrategy.GREATER_THAN_COMPARATOR, new GreaterThanComparator(inequalityComparator));
        evaluators.put(ConditionExpressionExecutorStrategy.GREATER_THAN_OR_EQUALS_COMPARATOR, new GreaterThanOrEqualsComparator(inequalityComparator));
        evaluators.put(ConditionExpressionExecutorStrategy.LESS_THAN_COMPARATOR, new LessThanComparator(inequalityComparator));
        evaluators.put(ConditionExpressionExecutorStrategy.lESS_THAN_OR_EQUALS_COMPARATOR, new LessThanOrEqualsComparator(inequalityComparator));

    }

    public BinaryComparator getEvaluator(String operator) {
        return evaluators.get(operator);
    }

}
