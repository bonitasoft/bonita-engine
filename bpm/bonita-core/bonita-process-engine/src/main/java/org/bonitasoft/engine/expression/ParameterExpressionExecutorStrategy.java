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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;

/**
 * Retrieve a String parameter from the ParameterService. The content of the expression must be the parameter name, as a String.
 * The dependency map must contain a value for 'processDefinitionId' key to identify the process definition context to evaluate the parameter.
 * 
 * @see {@link ParameterService}
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ParameterExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    private final ParameterService parameterService;

    public ParameterExpressionExecutorStrategy(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        final String expressionContent = expression.getContent();
        try {
            if (context != null && !context.isEmpty()) {
                if (context.containsKey(PROCESS_DEFINITION_ID)) {
                    final long processDefinitionId = (Long) context.get(PROCESS_DEFINITION_ID);
                    final SParameter parameter = parameterService.get(processDefinitionId, expressionContent);
                    try {
                        final String returnType = expression.getReturnType();
                        if (Boolean.class.getName().equals(returnType)) {
                            return Boolean.parseBoolean(parameter.getValue());
                        } else if (Double.class.getName().equals(returnType)) {
                            return Double.parseDouble(parameter.getValue());
                        } else if (Integer.class.getName().equals(returnType)) {
                            return Integer.parseInt(parameter.getValue());
                        } else if (String.class.getName().equals(returnType)) {
                            return parameter.getValue();
                        }
                    } catch (final NumberFormatException e) {
                        throw new SExpressionEvaluationException("Can't convert value = " + parameter.getValue() + " in type = returnType", e,
                                expression.getName());
                    }
                } else {
                    throw new SExpressionDependencyMissingException("Mandatory dependency processDefinitionId is missing.");
                }
            }
        } catch (final SParameterProcessNotFoundException e) {
            throw new SExpressionEvaluationException("Referenced parameter '" + expressionContent + "' does not exist", e, expression.getName());
        }
        return null;
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        super.validate(expression);
        // $ can be part of variable name
        if (!expression.getContent().matches("(^[a-zA-Z]+|^\\$)[a-zA-Z0-9$]*")) {
            throw new SInvalidExpressionException("The expression content does not matches with (^[a-zA-Z]+|^\\$)[a-zA-Z0-9$]* in expression: " + expression,
                    expression.getName());
        }

    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_PARAMETER;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return true;
    }

}
