/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.expression.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao na
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ConstantExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        if (expression.getContent().trim().equals("")) {
            throw new SInvalidExpressionException("The expresssion content cannot be empty. Expression: " + expression);
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_CONSTANT;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final String expressionContent = expression.getContent();
        Serializable result;
        final String returnType = expression.getReturnType();
        // here need to improve
        try {

            if (Boolean.class.getName().equals(returnType)) {
                result = Boolean.parseBoolean(expressionContent);
            } else if (Long.class.getName().equals(returnType)) {
                result = Long.parseLong(expressionContent);
            } else if (Double.class.getName().equals(returnType)) {
                result = Double.parseDouble(expressionContent);
            } else if (Float.class.getName().equals(returnType)) {
                result = Float.parseFloat(expressionContent);
            } else if (Integer.class.getName().equals(returnType)) {
                result = Integer.parseInt(expressionContent);
            } else if (String.class.getName().equals(returnType)) {
                result = expressionContent;
            } else if (Date.class.getName().equals(returnType)) { // "2013-01-02T02:42:12.17+02:00"
                result = new Date(); // FIXME: implement me
            } else {
                throw new SExpressionEvaluationException("unknown return type: " + returnType);
            }
        } catch (final NumberFormatException e) {
            throw new SExpressionEvaluationException("The content of the expression \"" + expression.getName() + "\" is not a number :" + expressionContent);
        }
        return result;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, dependencyValues, resolvedExpressions));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
