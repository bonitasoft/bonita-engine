/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.expression.control.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ExpressionResolverServiceImpl implements ExpressionResolverService {

    private static final SExpressionContext EMPTY_CONTEXT = new SExpressionContext();

    private final ExpressionService expressionService;

    private final ProcessDefinitionService processDefinitionService;

    private final ClassLoaderService classLoaderService;

    public ExpressionResolverServiceImpl(final ExpressionService expressionService, final ProcessDefinitionService processDefinitionService,
            final ClassLoaderService classLoaderService) {
        this.expressionService = expressionService;
        this.processDefinitionService = processDefinitionService;
        this.classLoaderService = classLoaderService;
    }

    @Override
    public Object evaluate(final SExpression expression) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        return evaluate(expression, EMPTY_CONTEXT);
    }

    @Override
    public Object evaluate(final SExpression expression, final SExpressionContext evaluationContext) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        return evaluateExpressionsFlatten(Collections.singletonList(expression), evaluationContext).get(0);
    }

    private List<Object> evaluateExpressionsFlatten(final List<SExpression> expressions, SExpressionContext evaluationContext)
            throws SInvalidExpressionException, SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException {

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final HashMap<String, Object> dependencyValues = new HashMap<String, Object>();
            if (evaluationContext == null) {
                evaluationContext = EMPTY_CONTEXT;
            } else {
                fillContext(evaluationContext, dependencyValues);
            }
            final Long processDefinitionId = evaluationContext.getProcessDefinitionId();
            if (processDefinitionId != null) {
                Thread.currentThread().setContextClassLoader(classLoaderService.getLocalClassLoader("PROCESS", processDefinitionId));
            }
            final HashMap<ExpressionKind, List<SExpression>> expressionMapByKind = new HashMap<ExpressionKind, List<SExpression>>();
            final int totalSize = flattenDependencies(expressionMapByKind, expressions);
            final ExpressionKind variableKind = new ExpressionKind(ExpressionType.TYPE_VARIABLE.name());
            // We incrementaly build the Map of already resolved expressions:
            final HashMap<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>(totalSize);
            final Map<SExpression, SExpression> dataReplacement = Collections.emptyMap();
            final List<SExpression> variableExpressions = expressionMapByKind.get(variableKind);
            if (evaluationContext.isEvaluateInDefinition() && variableExpressions != null && variableExpressions.size() > 0
                    && !variablesAreAllProvided(variableExpressions, evaluationContext)) {
                // We forbid the evaluation of expressions of type VARIABLE at process definition level:
                throw new SInvalidExpressionException("Evaluation of expressions of type VARIABLE is forbidden at process definition level");
            }
            // let's evaluate all expressions with no dependencies first:
            for (final ExpressionKind kind : ExpressionExecutorStrategy.NO_DEPENDENCY_EXPRESSION_EVALUATION_ORDER) {
                evaluateExpressionsOfKind(dependencyValues, expressionMapByKind, resolvedExpressions, kind, dataReplacement);
                expressionMapByKind.remove(kind);
            }
            // Then evaluate recursively all remaining expressions:
            for (final SExpression sExpression : expressions) {
                evaluateExpressionWithResolvedDependencies(sExpression, dependencyValues, resolvedExpressions, dataReplacement);
            }
            final ArrayList<Object> results = new ArrayList<Object>(expressions.size());
            for (final SExpression sExpression : expressions) {
                final int key = sExpression.getDiscriminant();
                final Object res = resolvedExpressions.get(key);
                if (res == null && !resolvedExpressions.containsKey(key)) {
                    throw new SExpressionEvaluationException("No result found for the expression " + sExpression);
                } else {
                    results.add(res);
                }
            }
            return results;
        } catch (final ClassLoaderException e) {
            throw new SExpressionEvaluationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private boolean variablesAreAllProvided(final List<SExpression> variableExpressions, final SExpressionContext evaluationContext) {
        boolean containsAll = true;
        final Iterator<SExpression> iterator = variableExpressions.iterator();
        final Map<String, Object> inputValues = evaluationContext.getInputValues();
        while (containsAll && iterator.hasNext()) {
            final SExpression next = iterator.next();
            containsAll = inputValues.containsKey(next.getContent());
        }
        return containsAll;
    }

    private void evaluateExpressionWithResolvedDependencies(final SExpression sExpression, final Map<String, Object> dependencyValues,
            final Map<Integer, Object> resolvedExpressions, final Map<SExpression, SExpression> dataReplacement) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        // Evaluate the dependencies first:
        for (final SExpression dep : sExpression.getDependencies()) {
            evaluateExpressionWithResolvedDependencies(dep, dependencyValues, resolvedExpressions, dataReplacement);
        }
        // Then evaluate the expression itself:
        if (!resolvedExpressions.containsKey(sExpression.getDiscriminant())) {
            // Let's evaluate the expression only if it is not already in the list of resolved dependencies:
            final Object exprResult = expressionService.evaluate(sExpression, dependencyValues, resolvedExpressions);
            addResultToMap(resolvedExpressions, dataReplacement, sExpression, exprResult, dependencyValues);
        }
    }

    private void evaluateExpressionsOfKind(final Map<String, Object> dependencyValues, final Map<ExpressionKind, List<SExpression>> expressionMapByKind,
            final Map<Integer, Object> resolvedExpressions, final ExpressionKind kind, final Map<SExpression, SExpression> dataReplacement)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        final List<SExpression> expressionsOfKind = expressionMapByKind.get(kind);
        if (expressionsOfKind != null) {
            final List<Object> evaluationResults = expressionService.evaluate(kind, expressionsOfKind, dependencyValues, resolvedExpressions);
            final Iterator<SExpression> variableIterator = expressionsOfKind.iterator();
            for (final Object evaluationResult : evaluationResults) {
                final SExpression expression = variableIterator.next();
                addResultToMap(resolvedExpressions, dataReplacement, expression, evaluationResult, dependencyValues);
            }
        }
    }

    private void addResultToMap(final Map<Integer, Object> resolvedExpressions, final Map<SExpression, SExpression> dataReplacement,
            final SExpression expression, final Object expressionResult, final Map<String, Object> dependencyValues) {
        resolvedExpressions.put(expression.getDiscriminant(), expressionResult);
        if (expressionService.mustPutEvaluatedExpressionInContext(expression.getExpressionKind())) {
            dependencyValues.put(expression.getContent(), expressionResult);
        }
        final SExpression replacement = dataReplacement.get(expression);
        if (replacement != null) {
            resolvedExpressions.put(replacement.getDiscriminant(), expressionResult);
            if (expressionService.mustPutEvaluatedExpressionInContext(expression.getExpressionKind())) {
                dependencyValues.put(replacement.getContent(), expressionResult);
            }
        }
    }

    private int flattenDependencies(final Map<ExpressionKind, List<SExpression>> map, final Collection<SExpression> collection) {
        int size = 0;
        for (final SExpression sExpression : collection) {
            final List<SExpression> exprList = getExpressionsOfKind(map, sExpression.getExpressionKind());
            if (!exprList.contains(sExpression)) {
                exprList.add(sExpression);
                size++;
            }
            if (sExpression.getDependencies() != null) {
                size += flattenDependencies(map, sExpression.getDependencies());
            }
        }
        return size;

    }

    /**
     * Return from the map the list of expressions of given ExpressionKind, and if no present, put it in the map before.
     */
    private List<SExpression> getExpressionsOfKind(final Map<ExpressionKind, List<SExpression>> map, final ExpressionKind expressionKind) {
        List<SExpression> list = map.get(expressionKind);
        if (list == null) {
            list = new ArrayList<SExpression>();
            map.put(expressionKind, list);
        }
        return list;
    }

    private void fillContext(final SExpressionContext evaluationContext, final Map<String, Object> dependencyValues) throws SInvalidExpressionException {
        if (evaluationContext.getContainerId() == null && evaluationContext.getProcessDefinitionId() != null) {
            try {
                final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(evaluationContext.getProcessDefinitionId());
                evaluationContext.setProcessDefinition(processDefinition);
                evaluationContext.setEvaluateInDefinition(true);
            } catch (final SBonitaException e) {
                throw new SInvalidExpressionException("Process definition not found with id " + evaluationContext.getProcessDefinitionId(), e);
            }
        }
        if (evaluationContext.getContainerId() != null) {
            dependencyValues.put(SExpressionContext.containerIdKey, evaluationContext.getContainerId());
        }
        if (evaluationContext.getContainerType() != null) {
            dependencyValues.put(SExpressionContext.containerTypeKey, evaluationContext.getContainerType());
        }
        if (evaluationContext.getProcessDefinitionId() != null) {
            dependencyValues.put(SExpressionContext.processDefinitionIdKey, evaluationContext.getProcessDefinitionId());
        }
        if (evaluationContext.getTime() != 0) {
            dependencyValues.put(SExpressionContext.timeKey, evaluationContext.getTime());
        }
        if (evaluationContext.getInputValues() != null) {
            dependencyValues.putAll(evaluationContext.getInputValues());
        }
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final SExpressionContext contextDependency) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        return evaluateExpressionsFlatten(expressions, contextDependency);
    }

}
