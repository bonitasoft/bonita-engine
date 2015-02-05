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
package org.bonitasoft.engine.core.expression.control.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;

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

    private final TimeTracker timeTracker;

    public ExpressionResolverServiceImpl(final ExpressionService expressionService, final ProcessDefinitionService processDefinitionService,
            final ClassLoaderService classLoaderService, final TimeTracker timeTracker) {
        this.expressionService = expressionService;
        this.processDefinitionService = processDefinitionService;
        this.classLoaderService = classLoaderService;
        this.timeTracker = timeTracker;
    }

    @Override
    public Object evaluate(final SExpression expression) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        return evaluate(expression, EMPTY_CONTEXT);
    }

    @Override
    public Object evaluate(final SExpression expression, final SExpressionContext evaluationContext)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final long startTime = System.currentTimeMillis();
        try {
            return evaluateExpressionsFlatten(Collections.singletonList(expression), evaluationContext).get(0);
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EVALUATE_EXPRESSION_INCLUDING_CONTEXT)) {
                final long endTime = System.currentTimeMillis();
                final StringBuilder desc = new StringBuilder();
                desc.append("Expression: ");
                desc.append(expression);
                desc.append(" - ");
                desc.append("evaluationContext: ");
                desc.append(evaluationContext);
                timeTracker.track(TimeTrackerRecords.EVALUATE_EXPRESSION_INCLUDING_CONTEXT, desc.toString(), endTime - startTime);
            }
        }
    }

    private List<Object> evaluateExpressionsFlatten(final List<SExpression> expressions, final SExpressionContext evaluationContext)
            throws SInvalidExpressionException, SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        SExpressionContext newEvaluationContext = EMPTY_CONTEXT;
        try {
            final Map<String, Object> dependencyValues = new HashMap<String, Object>();
            if (evaluationContext != null) {
                newEvaluationContext = evaluationContext;
                fillContext(newEvaluationContext, dependencyValues);
            }

            loadProcessClassLoader(newEvaluationContext);

            final Map<SExpression, SExpression> dataReplacement = new HashMap<SExpression, SExpression>();
            // We incrementally build the Map of already resolved expressions:
            final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>();
            // Let's evaluate all expressions with no dependencies first:
            resolvedExpressions.putAll(evaluateAllExpressionsWithNoDependencies(dependencyValues, dataReplacement, expressions, newEvaluationContext));

            for (final SExpression sExpression : expressions) {
                if(sExpression != null){
                    // Then evaluate recursively all remaining expressions:
                    resolvedExpressions.putAll(evaluateExpressionWithResolvedDependencies(sExpression, dependencyValues, dataReplacement, resolvedExpressions,
                            newEvaluationContext.getContainerState()));
                }
            }
            final List<Object> results = new ArrayList<Object>(expressions.size());
            for (final SExpression sExpression : expressions) {
                if (sExpression != null) {
                    final int key = sExpression.getDiscriminant();
                    final Object res = resolvedExpressions.get(key);
                    if (res == null && !resolvedExpressions.containsKey(key)) {
                        throw new SExpressionEvaluationException("No result found for the expression " + sExpression, sExpression.getName());
                    }
                    results.add(res);
                } else {
                    results.add(null);
                }
            }
            return results;
        } catch (final SProcessDefinitionNotFoundException e) {
            throw buildSExpressionEvaluationExceptionWhenNotFindProcess(newEvaluationContext, e);
        } catch (final SProcessDefinitionReadException e) {
            throw buildSExpressionEvaluationExceptionWhenNotFindProcess(newEvaluationContext, e);
        } catch (final SClassLoaderException e) {
            throw new SExpressionEvaluationException(e, null);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void loadProcessClassLoader(final SExpressionContext evaluationContext) throws SClassLoaderException {
        Long processId;
        if (evaluationContext.getParentProcessDefinitionId() != null) {
            processId = evaluationContext.getParentProcessDefinitionId();
        } else {
            processId = evaluationContext.getProcessDefinitionId();
        }
        if (processId != null) {
            Thread.currentThread().setContextClassLoader(classLoaderService.getLocalClassLoader("PROCESS", processId));
        }
    }

    private SExpressionEvaluationException buildSExpressionEvaluationExceptionWhenNotFindProcess(final SExpressionContext evaluationContext,
            final SBonitaException e) {
        final SExpressionEvaluationException exception = new SExpressionEvaluationException("The process definition was not found.", e, null);
        exception.setProcessDefinitionIdOnContext(evaluationContext.getProcessDefinitionId());
        return exception;
    }

    private Map<Integer, Object> evaluateAllExpressionsWithNoDependencies(final Map<String, Object> dependencyValues,
            final Map<SExpression, SExpression> dataReplacement, final List<SExpression> expressions, final SExpressionContext evaluationContext)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>();
        final Map<ExpressionKind, List<SExpression>> expressionMapByKind = flattenDependencies(expressions);
        final List<SExpression> variableExpressions = expressionMapByKind.get(new ExpressionKind(ExpressionType.TYPE_VARIABLE.name()));

        if (evaluationContext.isEvaluateInDefinition() && variableExpressions != null && !variableExpressions.isEmpty()) {
            final SExpression expressionNotProvided = variablesAreAllProvided(variableExpressions, evaluationContext);
            if (expressionNotProvided != null) {
                // We forbid the evaluation of expressions of type VARIABLE at process definition level:
                throw new SExpressionEvaluationException("Evaluation of expressions of type VARIABLE is forbidden at process definition level.",
                        expressionNotProvided.getName());
            }
        }
        for (final ExpressionKind kind : ExpressionExecutorStrategy.NO_DEPENDENCY_EXPRESSION_EVALUATION_ORDER) {
            resolvedExpressions.putAll(evaluateExpressionsOfKind(dependencyValues, expressionMapByKind.get(kind), kind, dataReplacement, resolvedExpressions,
                    evaluationContext.getContainerState()));
            expressionMapByKind.remove(kind);
        }
        return resolvedExpressions;
    }

    private SExpression variablesAreAllProvided(final List<SExpression> variableExpressions, final SExpressionContext evaluationContext) {
        final Iterator<SExpression> iterator = variableExpressions.iterator();
        final Map<String, Object> inputValues = evaluationContext.getInputValues();
        while (iterator.hasNext()) {
            final SExpression next = iterator.next();
            if (!inputValues.containsKey(next.getContent())) {
                return next;
            }
        }
        return null;
    }

    private Map<? extends Integer, ? extends Object> evaluateExpressionWithResolvedDependencies(final SExpression sExpression,
            final Map<String, Object> dependencyValues, final Map<SExpression, SExpression> dataReplacement,
            final Map<Integer, Object> alreadyResolvedExpressions, final ContainerState containerState) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>(alreadyResolvedExpressions);
        // Evaluate the dependencies first:
        for (final SExpression dep : sExpression.getDependencies()) {
            resolvedExpressions.putAll(evaluateExpressionWithResolvedDependencies(dep, dependencyValues, dataReplacement, resolvedExpressions, containerState));
        }
        // Then evaluate the expression itself:
        if (!resolvedExpressions.containsKey(sExpression.getDiscriminant())) {
            // Let's evaluate the expression only if it is not already in the list of resolved dependencies:
            final Object exprResult = expressionService.evaluate(sExpression, dependencyValues, resolvedExpressions, containerState);
            addResultToMap(resolvedExpressions, dataReplacement, sExpression, exprResult, dependencyValues);
        }
        return resolvedExpressions;
    }

    private Map<Integer, Object> evaluateExpressionsOfKind(final Map<String, Object> dependencyValues, final List<SExpression> expressionsOfKind,
            final ExpressionKind kind, final Map<SExpression, SExpression> dataReplacement, final Map<Integer, Object> alreadyResolvedExpressions,
            final ContainerState containerState) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>();
        if (expressionsOfKind != null) {
            final List<Object> evaluationResults = expressionService.evaluate(kind, expressionsOfKind, dependencyValues, alreadyResolvedExpressions,
                    containerState);
            final Iterator<SExpression> variableIterator = expressionsOfKind.iterator();
            for (final Object evaluationResult : evaluationResults) {
                final SExpression expression = variableIterator.next();
                addResultToMap(resolvedExpressions, dataReplacement, expression, evaluationResult, dependencyValues);
            }
        }
        return resolvedExpressions;
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

    private Map<ExpressionKind, List<SExpression>> flattenDependencies(final Collection<SExpression> collection) {
        final Map<ExpressionKind, List<SExpression>> expressionMapByKind = new HashMap<ExpressionKind, List<SExpression>>();
        for (final SExpression sExpression : collection) {
            if(sExpression == null){
                continue;
            }
            final ExpressionKind expressionKind = sExpression.getExpressionKind();
            // Get from the map the list of expressions of given ExpressionKind
            List<SExpression> exprList = expressionMapByKind.get(expressionKind);

            // If no present, put it in the map
            if (exprList == null) {
                exprList = new ArrayList<SExpression>();
                expressionMapByKind.put(expressionKind, exprList);
            }
            if (!exprList.contains(sExpression)) {
                exprList.add(sExpression);
            }
            if (sExpression.getDependencies() != null) {
                expressionMapByKind.putAll(flattenDependencies(sExpression.getDependencies()));
            }
        }
        return expressionMapByKind;
    }

    private void fillContext(final SExpressionContext evaluationContext, final Map<String, Object> dependencyValues)
            throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        if (evaluationContext.getContainerId() == null && evaluationContext.getProcessDefinitionId() != null) {
            final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(evaluationContext.getProcessDefinitionId());
            evaluationContext.setProcessDefinition(processDefinition);
            evaluationContext.setEvaluateInDefinition(true);
        }
        if (evaluationContext.getContainerId() != null) {
            dependencyValues.put(SExpressionContext.CONTAINER_ID_KEY, evaluationContext.getContainerId());
        }
        if (evaluationContext.getContainerType() != null) {
            dependencyValues.put(SExpressionContext.CONTAINER_TYPE_KEY, evaluationContext.getContainerType());
        }
        if (evaluationContext.getProcessDefinitionId() != null) {
            dependencyValues.put(SExpressionContext.PROCESS_DEFINITION_ID_KEY, evaluationContext.getProcessDefinitionId());
        }
        if (evaluationContext.getTime() != 0) {
            dependencyValues.put(SExpressionContext.TIME_KEY, evaluationContext.getTime());
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
