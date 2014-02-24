/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.parameter.SParameter;
import com.bonitasoft.engine.parameter.SParameterProcessNotFoundException;

/**
 * Retrieve a String parameter from the ParameterService. The content of the expression must be the parameter name, as a String.
 * The dependency map must contain a value for 'processDefinitionId' key to identify the process definition context to evaluate the parameter.
 * 
 * @see {@link ParameterService}
 * @author Zhao Na
 */
public class ParameterExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    private final ParameterService parameterService;

    public ParameterExpressionExecutorStrategy(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        long processDefinitionId;
        final String expressionContent = expression.getContent();
        try {
            if (dependencyValues != null && !dependencyValues.isEmpty()) {
                if (dependencyValues.containsKey(PROCESS_DEFINITION_ID)) {
                    processDefinitionId = (Long) dependencyValues.get(PROCESS_DEFINITION_ID);
                    final SParameter para = parameterService.get(processDefinitionId, expressionContent);
                    try {
                        final String returnType = expression.getReturnType();
                        if (Boolean.class.getName().equals(returnType)) {
                            return Boolean.parseBoolean(para.getValue());
                        } else if (Double.class.getName().equals(returnType)) {
                            return Double.parseDouble(para.getValue());
                        } else if (Integer.class.getName().equals(returnType)) {
                            return Integer.parseInt(para.getValue());
                        } else if (String.class.getName().equals(returnType)) {
                            return para.getValue();
                        }
                    } catch (final NumberFormatException e) {
                        throw new SExpressionEvaluationException("Can't convert value = " + para.getValue() + " in type = returnType", e, expression.getName());
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
            throw new SInvalidExpressionException("The expression content does not matches with (^[a-zA-Z]+|^\\$)[a-zA-Z0-9$]* in expression: " + expression);
        }

    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_PARAMETER;
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
        return true;
    }

}
