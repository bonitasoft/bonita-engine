/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zhao na
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 * @author Celine Souchet
 */

public class ExpressionServiceImpl implements ExpressionService {

    private static final Logger log = LoggerFactory.getLogger(ExpressionServiceImpl.class);
    private final Map<ExpressionKind, ExpressionExecutorStrategy> expressionExecutorsMap = new HashMap<>();
    private boolean checkExpressionReturnType = false;

    private final TimeTracker timeTracker;

    public ExpressionServiceImpl(final boolean checkExpressionReturnType,
            final TimeTracker timeTracker) {
        super();
        this.checkExpressionReturnType = checkExpressionReturnType;
        this.timeTracker = timeTracker;
    }

    @Override
    public void setExpressionExecutorStrategy(List<ExpressionExecutorStrategy> expressionExecutors) {
        for (final ExpressionExecutorStrategy expressionExecutorStrategy : expressionExecutors) {
            expressionExecutorsMap.put(expressionExecutorStrategy.getExpressionKind(), expressionExecutorStrategy);
        }
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        return evaluate(expression, new HashMap<String, Object>(1), resolvedExpressions, containerState);
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues,
            final Map<Integer, Object> resolvedExpressions, final ContainerState containerState)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        if (log.isTraceEnabled()) {
            log.trace(LogUtil.getLogBeforeMethod(this.getClass(), "evaluate"));
        }

        final ExpressionExecutorStrategy expressionExecutorStrategy = getStrategy(expression.getExpressionKind());
        validateExpression(expressionExecutorStrategy, expression);

        Object expressionResult = null;
        final long startTime = System.currentTimeMillis();
        try {
            expressionResult = expressionExecutorStrategy.evaluate(expression, dependencyValues, resolvedExpressions,
                    containerState);
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EVALUATE_EXPRESSION)) {
                final long endTime = System.currentTimeMillis();
                timeTracker.track(TimeTrackerRecords.EVALUATE_EXPRESSION, "Expression: " + expression + " - "
                        + "dependencyValues: " + dependencyValues + " - " + "strategy: " + expressionExecutorStrategy,
                        endTime - startTime);
            }
        }
        if (mustCheckExpressionReturnType()) {
            new ReturnTypeChecker().checkReturnType(expression, expressionResult, dependencyValues);
        }

        if (log.isTraceEnabled()) {
            log.trace(LogUtil.getLogAfterMethod(this.getClass(), "evaluate"));
        }

        return expressionResult;
    }

    private void validateExpression(final ExpressionExecutorStrategy expressionExecutorStrategy,
            final SExpression expression) throws SInvalidExpressionException {
        try {
            // this will throw exception if the expression is invalid
            expressionExecutorStrategy.validate(expression);
        } catch (final SInvalidExpressionException e) {
            log.trace(LogUtil.getLogOnExceptionMethod(this.getClass(),
                    "evaluate", "Invalid Expression : " + expression.getContent()));

            throw e;
        }
    }

    private ExpressionExecutorStrategy getStrategy(final ExpressionKind expressionKind)
            throws SExpressionTypeUnknownException {
        final ExpressionExecutorStrategy expressionExecutorStrategy = expressionExecutorsMap.get(expressionKind);
        if (expressionExecutorStrategy == null) {
            log.trace(LogUtil.getLogOnExceptionMethod(this.getClass(),
                    "evaluate", "Unable to find an executor for expression type " + expressionKind));

            throw new SExpressionTypeUnknownException(
                    "Unable to find an executor for expression type " + expressionKind);
        }
        return expressionExecutorStrategy;
    }

    @Override
    public List<Object> evaluate(final ExpressionKind expressionKind, final List<SExpression> expressions,
            final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException {
        if (log.isTraceEnabled()) {
            log.trace(LogUtil.getLogBeforeMethod(this.getClass(), "evaluate"));
        }

        final ExpressionExecutorStrategy expressionExecutorStrategy = getStrategy(expressionKind);

        List<Object> list = null;
        final long startTime = System.currentTimeMillis();
        try {
            list = expressionExecutorStrategy.evaluate(expressions, dependencyValues, resolvedExpressions,
                    containerState);
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EVALUATE_EXPRESSIONS)) {
                final long endTime = System.currentTimeMillis();
                timeTracker.track(TimeTrackerRecords.EVALUATE_EXPRESSIONS, "Expressions: " + expressions + " - "
                        + "dependencyValues: " + dependencyValues + " - " + "strategy: " + expressionExecutorStrategy,
                        endTime - startTime);
            }
        }
        if (list == null || list.size() != expressions.size()) {
            final String exceptionMessage = "Result list size " + (list == null ? 0 : list.size())
                    + " is different from expression list size " + expressions.size();
            if (log.isTraceEnabled()) {
                log.trace(LogUtil.getLogOnExceptionMethod(this.getClass(), "evaluate", exceptionMessage));
            }

            throw new SExpressionEvaluationException(exceptionMessage, null);
        }
        if (mustCheckExpressionReturnType()) {
            for (int i = 0; i < list.size(); i++) {
                new ReturnTypeChecker().checkReturnType(expressions.get(i), list.get(i), dependencyValues);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace(LogUtil.getLogAfterMethod(this.getClass(), "evaluate"));
        }

        return list;
    }

    @Override
    public boolean mustCheckExpressionReturnType() {
        return checkExpressionReturnType;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext(final ExpressionKind expressionKind) {
        return expressionExecutorsMap.get(expressionKind).mustPutEvaluatedExpressionInContext();
    }

}
