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

import java.io.Serializable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
final class EvaluateExpression implements TransactionContentWithResult<Serializable> {

    private final SExpressionContext contextDependency;

    private final SExpression expression;

    private Serializable result;

    private final ExpressionResolverService expressionResolverService;

    public EvaluateExpression(final ExpressionResolverService expressionResolverService, final SExpressionContext contextDependency,
            final SExpression expression) {
        this.expressionResolverService = expressionResolverService;
        this.contextDependency = contextDependency;
        this.expression = expression;
    }

    @Override
    public void execute() throws SBonitaException {
        result = (Serializable) expressionResolverService.evaluate(expression, contextDependency);
    }

    @Override
    public Serializable getResult() {
        return result;
    }

}
