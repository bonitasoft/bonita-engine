/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.core.data.instance.impl.TransientDataInstanceDataSource;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.data.instance.DataInstanceDataSource;
import org.bonitasoft.engine.data.instance.DataInstanceDataSourceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.impl.DataInstanceServiceImpl;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.SBadTransactionStateException;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class AbstractExpressionServiceTest extends CommonServiceTest {

    protected static SExpressionBuilders sExpressionBuilders;

    private static SDataSource dataSource;

    private static SDataSource transientDataSource;

    private static DataService dataSourceService;

    private static SDataSourceBuilder dataSourceBuilder;

    protected static final Map<Integer, Object> EMPTY_RESOLVED_EXPRESSIONS = Collections.emptyMap();

    protected abstract SDataDefinitionBuilders getSDataDefinitionBuilders();

    protected abstract SDataInstanceBuilders getSDataInstanceBuilders();

    protected abstract DataInstanceService getDataInstanceService();

    protected abstract ExpressionService getExpressionService();

    protected abstract CacheService getCacheService();

    static {
        dataSourceService = getServicesBuilder().buildDataService();
        dataSourceBuilder = getServicesBuilder().buildDataSourceModelBuilder();
        sExpressionBuilders = getServicesBuilder().getInstanceOf(SExpressionBuilders.class);
    }

    @BeforeClass
    public static void setUpPersistence() throws Exception {
        dataSource = createDataInstanceDataSource(DataInstanceServiceImpl.DEFAULT_DATA_SOURCE, DataInstanceServiceImpl.DATA_SOURCE_VERSION,
                DataInstanceDataSourceImpl.class);
        transientDataSource = createDataInstanceDataSource(DataInstanceServiceImpl.TRANSIENT_DATA_SOURCE,
                DataInstanceServiceImpl.TRANSIENT_DATA_SOURCE_VERSION, TransientDataInstanceDataSource.class);
    }

    private static SDataSource createDataInstanceDataSource(final String dataSourceName, final String dataSourceVersion,
            final Class<? extends DataInstanceDataSource> clazz) throws Exception {
        getTransactionService().begin();
        final SDataSource dataSource = dataSourceBuilder.createNewInstance(dataSourceName, dataSourceVersion, SDataSourceState.ACTIVE, clazz.getName()).done();
        dataSourceService.createDataSource(dataSource);
        getTransactionService().complete();
        return dataSource;
    }

    @AfterClass
    public static void tearDownPersistence() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        removeDataInstanceDataSource(dataSource);
        removeDataInstanceDataSource(transientDataSource);
    }

    private static void removeDataInstanceDataSource(final SDataSource dataSource) throws Exception {
        getTransactionService().begin();
        if (dataSource != null) {
            dataSourceService.removeDataSource(dataSource.getId());
        }
        getTransactionService().complete();
    }

    protected SExpression buildExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        final SExpressionBuilder eb = sExpressionBuilders.getExpressionBuilder().createNewInstance();
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
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException, SBadTransactionStateException,
            FireEventException, STransactionCreationException, STransactionCommitException, STransactionRollbackException {
        getTransactionService().begin();
        final Object result = getExpressionService().evaluate(expression, resolvedExpressions);
        getTransactionService().complete();
        return result;
    }

    protected List<Object> evaluate(final ExpressionKind expressionKind, final List<SExpression> expressions, final Map<String, Object> dependencyValues,
            final Map<Integer, Object> resolvedExpressions) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException, SBadTransactionStateException, FireEventException,
            STransactionCreationException, STransactionCommitException, STransactionRollbackException {
        getTransactionService().begin();
        final List<Object> result = getExpressionService().evaluate(expressionKind, expressions, dependencyValues, resolvedExpressions);
        getTransactionService().complete();
        return result;
    }

    protected void evaluateAndCheckResult(final SExpression expression, final Object expectedValue, final Map<Integer, Object> resolvedExpression)
            throws Exception {
        final Object expressionResult = evaluate(expression, resolvedExpression);
        assertEquals(expectedValue, expressionResult);
    }

}
