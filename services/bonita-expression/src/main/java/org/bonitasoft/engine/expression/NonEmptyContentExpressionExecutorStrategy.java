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
package org.bonitasoft.engine.expression;

import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class NonEmptyContentExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        if (expression == null) {
            throw new SInvalidExpressionException("The expression cannot be null.", null);
        }
        final String expressionContent = expression.getContent();

        if (expressionContent == null || expressionContent.trim().isEmpty()) {
            throw new SInvalidExpressionException("The expression content cannot be null or empty. Expression : " + expression, expression.getName());
        }
    }

}
