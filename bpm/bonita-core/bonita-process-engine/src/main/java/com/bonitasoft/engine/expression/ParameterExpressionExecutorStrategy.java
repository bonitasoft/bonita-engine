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
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;

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
            Object result = null;
            if (dependencyValues != null && !dependencyValues.isEmpty()) {
                if (dependencyValues.containsKey(PROCESS_DEFINITION_ID)) {
                    processDefinitionId = (Long) dependencyValues.get(PROCESS_DEFINITION_ID);
                    final SParameter para = parameterService.get(processDefinitionId, expressionContent);
                    final String returnType = expression.getReturnType();
                    if (Boolean.class.getName().equals(returnType)) {
                        result = Boolean.parseBoolean(para.getValue());
                    } else if (Double.class.getName().equals(returnType)) {
                        result = Double.parseDouble(para.getValue());
                    } else if (Integer.class.getName().equals(returnType)) {
                        result = Integer.parseInt(para.getValue());
                    } else if (String.class.getName().equals(returnType)) {
                        result = para.getValue();
                    }
                } else {
                    throw new SExpressionDependencyMissingException("Mandatory dependency processDefinitionId is missing.");
                }
            }
            return result;
        } catch (final SParameterProcessNotFoundException e) {
            throw new SExpressionEvaluationException("Referenced arameter '" + expressionContent + "' does not exist", e);
        }
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
