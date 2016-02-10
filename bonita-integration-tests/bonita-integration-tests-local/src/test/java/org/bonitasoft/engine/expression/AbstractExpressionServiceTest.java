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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class AbstractExpressionServiceTest extends CommonBPMServicesTest {

    protected static final long DEFINITION_ID_VALUE = 123L;

    protected static final Map<Integer, Object> EMPTY_RESOLVED_EXPRESSIONS = Collections.emptyMap();

    protected abstract DataInstanceService getDataInstanceService();

    protected abstract ExpressionService getExpressionService();

    protected abstract CacheService getCacheService();

    /*
    @AfterClass
    public static void tearDownPersistence() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
    }
    */

    protected SExpression buildExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        final SExpressionBuilder eb = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
        eb.setName(content);
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        try {
            return eb.done();
        } catch (final SInvalidExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected Object evaluate(final SExpression expression, final Map<Integer, Object> resolvedExpressions) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException, STransactionCreationException,
            STransactionCommitException, STransactionRollbackException {
        getTransactionService().begin();
        final Map<String, Object> dependencyValues = new HashMap<String, Object>();
        dependencyValues.put(ExpressionExecutorStrategy.DEFINITION_ID, DEFINITION_ID_VALUE);
        final Object result = getExpressionService().evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
        getTransactionService().complete();
        return result;
    }

    protected List<Object> evaluate(final ExpressionKind expressionKind, final List<SExpression> expressions, final Map<String, Object> dependencyValues,
            final Map<Integer, Object> resolvedExpressions) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException, STransactionCreationException, STransactionCommitException,
            STransactionRollbackException {
        getTransactionService().begin();
        final List<Object> result = getExpressionService().evaluate(expressionKind, expressions, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
        getTransactionService().complete();
        return result;
    }

    protected void evaluateAndCheckResult(final SExpression expression, final Object expectedValue, final Map<Integer, Object> resolvedExpression)
            throws Exception {
        final Object expressionResult = evaluate(expression, resolvedExpression);
        assertEquals(expectedValue, expressionResult);
    }

}
