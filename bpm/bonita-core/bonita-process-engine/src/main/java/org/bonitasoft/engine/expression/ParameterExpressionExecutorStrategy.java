/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
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
public class ParameterExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    private final ParameterService parameterService;

    public ParameterExpressionExecutorStrategy(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @Override
    public Serializable evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Serializable> resolvedExpressions)
            throws SExpressionDependencyMissingException, SExpressionEvaluationException {
        long processDefinitionId;
        final String expressionContent = expression.getContent();
        try {
            Serializable result = null;
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
            throw new SExpressionEvaluationException("The paramter name " + expressionContent + " does not exist", e);
        }
    }

    @Override
    public boolean validate(final String expressionContent) {
        // $ can be part of variable name
        if (expressionContent != null && !expressionContent.isEmpty() && !expressionContent.trim().equals("")) {
            if (expressionContent.matches("(^[a-zA-Z]+|^\\$)[a-zA-Z0-9$]*")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_PARAMETER;
    }

    @Override
    public List<Serializable> evaluate(final List<SExpression> expressions, final Map<String, Object> dependencyValues,
            final Map<Integer, Serializable> resolvedExpressions) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Serializable> list = new ArrayList<Serializable>(expressions.size());
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
