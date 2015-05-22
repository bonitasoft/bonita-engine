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
 */
package org.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.impl.GroovyScriptExpressionExecutorCacheStrategy;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilderFactory;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Zhao na
 */
public class ExpressionServiceTest extends AbstractExpressionServiceTest {

    protected static final Map<Integer, Object> EMPTY_RESOLVED_EXPRESSIONS = Collections.emptyMap();

    private static DataInstanceService dataInstanceService;

    private static ExpressionService expressionService;

    private static CacheService cacheService;

    public ExpressionServiceTest() {
    }

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        expressionService = getTenantAccessor().getExpressionService();
        dataInstanceService = getTenantAccessor().getDataInstanceService();
        cacheService = getTenantAccessor().getCacheService();
        if (cacheService.isStopped()) {
            try {
                cacheService.start();
            } catch (final SBonitaException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        try {
            cacheService.stop();
            cacheService.start();
        } catch (final SBonitaException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    protected synchronized ExpressionService getExpressionService() {
        return expressionService;
    }

    @Override
    protected DataInstanceService getDataInstanceService() {
        return dataInstanceService;
    }

    @Override
    protected CacheService getCacheService() {
        return cacheService;
    }

    private Object evaluate(final SExpression expression, final Map<String, Object> dependencyValues, final Map<Integer, Object> resolvedExpressions)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException,
            STransactionCreationException, STransactionCommitException, STransactionRollbackException {
        getTransactionService().begin();
        final Object result = expressionService.evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
        getTransactionService().complete();
        return result;
    }

    @Test
    public void evaluateStrConstant() throws Exception {
        // string
        final String strContent = "aabb*&^%$#@()!~><?|}{+_0123456789";// "''" or"/"/""
        final SExpression strExpr = buildExpression(strContent, SExpression.TYPE_CONSTANT, String.class.getName(), null, null);
        evaluateAndCheckResult(strExpr, strContent, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateStrEmptyConstant() throws Exception {
        // string
        final String strContent = " ";
        final SExpression strExpr = buildExpression(strContent, SExpression.TYPE_CONSTANT, String.class.getName(), null, null);
        evaluateAndCheckResult(strExpr, strContent, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateWrongTypeIntegerExpression() throws Exception {
        SExpression dataExpr = null;
        final long containerId = 987456L;
        final String containerType = "process";
        final Map<String, Object> dependencyValues = new HashMap<String, Object>();
        dependencyValues.put("containerId", containerId);
        dependencyValues.put("containerType", containerType);
        try {
            final String dataName = "abcd";
            final SDataInstance dataInstance = buildDataInstance(dataName, String.class.getName(), "data_description", "'some data initial value'",
                    containerId, containerType, false);
            insertDataInstance(dataInstance);
            dataExpr = buildExpression(dataName, SExpression.TYPE_VARIABLE, Integer.class.getName(), null, null);
        } catch (final Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        evaluate(dataExpr, dependencyValues, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateWrongTypeGroovyExpression() throws Exception {
        final String strContent = "return new ArrayList();";
        final SExpression strExpr = buildExpression(strContent, SExpression.TYPE_READ_ONLY_SCRIPT, Boolean.class.getName(), SExpression.GROOVY, null);
        evaluate(strExpr, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test
    public void evaluateCorrectSuperTypeGroovyExpression() throws Exception {
        final String strContent = "return new ArrayList();";
        final SExpression strExpr = buildExpression(strContent, SExpression.TYPE_READ_ONLY_SCRIPT, List.class.getName(), SExpression.GROOVY, null);
        evaluate(strExpr, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test
    public void checkGroovyScriptStrategyUsesCache() throws Exception {
        //given
        final String strContent = "return \"junit test checkGroovyScriptStrategyUsesCache\"";
        final SExpression strExpr = buildExpression(strContent, SExpression.TYPE_READ_ONLY_SCRIPT, String.class.getName(), SExpression.GROOVY, null);
        final String cacheKey = GroovyScriptExpressionExecutorCacheStrategy.SCRIPT_KEY + strContent.hashCode();
        assertThat(cacheService.get(GroovyScriptExpressionExecutorCacheStrategy.GROOVY_SCRIPT_CACHE_NAME, cacheKey)).as("should not contains key").isNull();

        //when
        evaluate(strExpr, EMPTY_RESOLVED_EXPRESSIONS);

        //then
        assertThat(cacheService.get(GroovyScriptExpressionExecutorCacheStrategy.GROOVY_SCRIPT_CACHE_NAME, cacheKey)).as("should contains key").isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void evaluateListExpression() throws Exception {
        final String strConstant = "abcd";
        final String dataName = "myLocalVariable";
        final String dataValue = "ManuDataValue";
        final long containerId = 1498674654675968768L;
        final String containerType = "localEvaluationContext";
        insertDataInstance(buildDataInstance(dataName, String.class.getName(), "Some dummy data description", "'" + dataValue + "'", containerId,
                containerType, false));
        final SExpression exprConstant = buildExpression(strConstant, SExpression.TYPE_CONSTANT, String.class.getName(), null, null);
        final SExpression exprData = buildExpression(dataName, SExpression.TYPE_VARIABLE, String.class.getName(), null, null);
        final SExpression expr = buildExpression(null, SExpression.TYPE_LIST, List.class.getName(), null, Arrays.asList(exprConstant, exprData));
        final Map<String, Object> ctx = new HashMap<String, Object>(2);
        ctx.put("containerId", containerId);
        ctx.put("containerType", containerType);
        final Map<Integer, Object> evaluatedExpressions = new HashMap<Integer, Object>(2);
        evaluatedExpressions.put(exprConstant.getDiscriminant(), evaluate(exprConstant, EMPTY_RESOLVED_EXPRESSIONS));
        evaluatedExpressions.put(exprData.getDiscriminant(), evaluate(exprData, ctx, EMPTY_RESOLVED_EXPRESSIONS));
        final Object res = evaluate(expr, null, evaluatedExpressions);
        assertEquals(strConstant, ((List<Serializable>) res).get(0));
        assertEquals(dataValue, ((List<Serializable>) res).get(1));
    }

    @Test
    public void evaluateDoubleConstant() throws Exception {
        // Double
        final String doubleContent = "10.3";
        final SExpression doubExpr = buildExpression(doubleContent, SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final Object expressionResult = evaluate(doubExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals(10.3, expressionResult);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateDoubleEmptyConstant() throws Exception {
        final String strContent = "";
        final SExpression strExpr = buildExpression(strContent, SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        evaluateAndCheckResult(strExpr, strContent, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test
    public void evaluateDoubleTypeNoDecimal() throws Exception {
        // Double
        final String doubleContent = "10.";
        final SExpression doubExpr = buildExpression(doubleContent, SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final Object expressionResult = evaluate(doubExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals(10.0, expressionResult);
    }

    @Test
    public void evaluateDoubleTypeNoRadical() throws Exception {
        // Double
        final String doubleContent = ".3";
        final SExpression doubExpr = buildExpression(doubleContent, SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final Object expressionResult = evaluate(doubExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals(0.3, expressionResult);
    }

    @Test
    public void evaluateLongConstant() throws Exception {
        // Long
        final String longContent = "123000000";
        final SExpression longExpr = buildExpression(longContent, SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final Object expressionResult = evaluate(longExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals(123000000l, expressionResult);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateLongEmptyConstant() throws Exception {
        final String longContent = "";
        final SExpression longExpr = buildExpression(longContent, SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        evaluate(longExpr, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test(expected = SExpressionDependencyMissingException.class)
    public void evaluatePatternExpressionWithException() throws Exception {
        final String expressionContent = "Expression with missing dependencies";
        final List<SExpression> dependencies = new ArrayList<SExpression>(1);
        dependencies.add(buildExpression("value_will_be_missing", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null));
        final SExpression simplePatternExpression = newPatternExpression(expressionContent, dependencies);
        evaluate(simplePatternExpression, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test
    public void evaluatePatternExpressionWithoutDependencies() throws Exception {
        final String expressionContent = "Default expression with no dependencies";
        final SExpression simplePatternExpression = newPatternExpression(expressionContent, null);
        final Object expressionResult = evaluate(simplePatternExpression, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals(expressionContent, expressionResult);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateEmptyPatternExpression() throws Exception {
        final long processInstanceId = 123456L;
        final String data1Content = "emptyPatternData";
        insertDataInstance(buildDataInstance(data1Content, String.class.getName(), "emptyPatternData_description", "'EXPRESSION'", processInstanceId,
                "process", false));
        final SExpression data1Expr = buildExpression(data1Content, SExpression.TYPE_VARIABLE, String.class.getName(), null, null);

        final String constant2Content = "123456789";
        final SExpression intExpr = buildExpression(constant2Content, SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);

        final List<SExpression> dependencyExpresssions = new ArrayList<SExpression>(2);
        dependencyExpresssions.add(data1Expr);
        dependencyExpresssions.add(intExpr);

        final String expressionContent = " ";
        final SExpression patternExpression = newPatternExpression(expressionContent, dependencyExpresssions);
        final Map<String, Object> expressionEvalContext = new HashMap<String, Object>(2);
        expressionEvalContext.put(data1Content, "EXPRESSION");
        expressionEvalContext.put(constant2Content, 2);
        evaluate(patternExpression, expressionEvalContext, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test
    public void evaluateStrGroovy() throws Exception {
        // String
        final String groovyContentStr = "'a'+1+'b'";
        final SExpression groovyContentExpr = buildExpression(groovyContentStr, SExpression.TYPE_READ_ONLY_SCRIPT, String.class.getName(), SExpression.GROOVY,
                null);
        final Object expressionResult = evaluate(groovyContentExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals("a1b", expressionResult);
    }

    @Test
    public void evaluateNumericGroovy() throws Exception {
        // numeric
        final String groovyContentNum = "((1+2)*2)/3";
        final SExpression groovyNumExpr = buildExpression(groovyContentNum, SExpression.TYPE_READ_ONLY_SCRIPT, BigDecimal.class.getName(), SExpression.GROOVY,
                null);
        final Object expressionResult = evaluate(groovyNumExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals(new BigDecimal(2), expressionResult);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateStrEmptyGroovy() throws Exception {
        // String
        final String groovyContentStr = " ";
        final SExpression groovyContentExpr = buildExpression(groovyContentStr, SExpression.TYPE_READ_ONLY_SCRIPT, String.class.getName(), SExpression.GROOVY,
                null);
        final Object expressionResult = evaluate(groovyContentExpr, EMPTY_RESOLVED_EXPRESSIONS);
        assertEquals("a1b", expressionResult);
    }

    @Test(expected = SExpressionTypeUnknownException.class)
    public void evaluateUnkonwnExpressionType() throws SBonitaException {
        final String expressionContent = "str";
        final SExpression expression = buildExpression(expressionContent, "TYPE_UNKNOWN", Boolean.class.getName(), null, null);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(expressionContent, "abc");
        evaluate(expression, map, EMPTY_RESOLVED_EXPRESSIONS);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateUnknownProperty() throws Exception {
        // numeric
        final String groovyContentNum = "variable1";
        final SExpression groovyNumExpr = buildExpression(groovyContentNum, SExpression.TYPE_READ_ONLY_SCRIPT, BigDecimal.class.getName(), SExpression.GROOVY,
                null);
        evaluate(groovyNumExpr, EMPTY_RESOLVED_EXPRESSIONS);
    }

    private SExpression newPatternExpression(final String content, final List<SExpression> dependencies) {
        return buildExpression(content, SExpression.TYPE_PATTERN, String.class.getName(), null, dependencies);
    }

    private void initializeBuilder(final SDataDefinitionBuilder dataDefinitionBuilder, final String description, final String content,
                                   final boolean isTransient, final String defaultValueReturnType) {
        SExpression expression = null;
        if (content != null) {
            // create expression
            final SExpressionBuilder expreBuilder = BuilderFactory.get(SExpressionBuilderFactory.class).createNewInstance();
            // this discrimination'll be changed.
            expreBuilder.setContent(content).setExpressionType(SExpression.TYPE_READ_ONLY_SCRIPT).setInterpreter(SExpression.GROOVY)
                    .setReturnType(defaultValueReturnType);
            try {
                expression = expreBuilder.done();
            } catch (final SInvalidExpressionException e) {
                throw new IllegalArgumentException(e);
            }
        }

        dataDefinitionBuilder.setDescription(description);
        dataDefinitionBuilder.setTransient(isTransient);
        dataDefinitionBuilder.setDefaultValue(expression);
    }

    private SDataInstance buildDataInstance(final String instanceName, final String className, final String description, final String content,
                                            final long containerId, final String containerType, final boolean isTransient) throws SBonitaException {
        // create definition
        final SDataDefinitionBuilder dataDefinitionBuilder = BuilderFactory.get(SDataDefinitionBuilderFactory.class).createNewInstance(instanceName, className);
        initializeBuilder(dataDefinitionBuilder, description, content, isTransient, className);
        final SDataDefinition dataDefinition = dataDefinitionBuilder.done();
        // create datainstance
        final SDataInstanceBuilder dataInstanceBuilder = BuilderFactory.get(SDataInstanceBuilderFactory.class).createNewInstance(dataDefinition);
        evaluateDefaultValueOf(dataDefinition, dataInstanceBuilder);
        return dataInstanceBuilder.setContainerId(containerId).setContainerType(containerType).done();
    }

    private void evaluateDefaultValueOf(final SDataDefinition dataDefinition, final SDataInstanceBuilder dataInstanceBuilder) throws SBonitaException {
        final SExpression expression = dataDefinition.getDefaultValueExpression();
        if (expression != null) {
            dataInstanceBuilder.setValue((Serializable) evaluate(expression, EMPTY_RESOLVED_EXPRESSIONS));
        }
    }

    private void insertDataInstance(final SDataInstance dataInstance) throws SBonitaException {
        getTransactionService().begin();
        // create data instance
        dataInstanceService.createDataInstance(dataInstance);
        getTransactionService().complete();
    }

    private void verifyEvaluateVariable(final String expressionContent, final Class<?> classType, final String description, final Long containerId,
                                        final String containerType, final String value, final Boolean isTransient, final Serializable hopeValue) throws Exception {
        // create data
        final SDataInstance dataInstance = buildDataInstance(expressionContent, classType.getName(), description, value, containerId, containerType,
                isTransient);
        // insert data
        insertDataInstance(dataInstance);
        // definite expression
        final SExpression variableExpr = buildExpression(expressionContent, SExpression.TYPE_VARIABLE, classType.getName(), null, null);
        final Map<String, Object> dependencyValues = new HashMap<String, Object>();
        dependencyValues.put("containerId", containerId);
        dependencyValues.put("containerType", containerType);

        final Object resultValue = evaluate(variableExpr, dependencyValues, EMPTY_RESOLVED_EXPRESSIONS);
        if (resultValue != null) {
            assertEquals(classType, resultValue.getClass());
        }
        assertEquals(hopeValue, resultValue);
    }

    @Test
    public void evaluateShortTextVariableDB() throws Exception {
        verifyEvaluateVariable("userName", String.class, "123456qwewr", 11L, "process", "'Edward'", false, "Edward");
    }

    @Test
    public void evaluateShortTextVariableDBWithNoDefaultValue() throws Exception {
        verifyEvaluateVariable("nullableData", String.class, "data that is possibly null", 21L, "process", null, false, null);
    }

    @Test
    public void evaluateShortTextVariableCache() throws Exception {
        verifyEvaluateVariable("userName", String.class, "123456qwewr", 12L, "process", "'Edward'", true, "Edward");
    }

    @Test
    public void evaluateNumericVariableDB() throws Exception {
        verifyEvaluateVariable("pageSum", Integer.class, "evaluate Numeric Variable DB", 12L, "task", "100", false, 100);
    }

    @Test
    public void evaluateNumericVariableCache() throws Exception {
        verifyEvaluateVariable("pageSum", Integer.class, "evaluate Numeric Variabl eCache", 13L, "task", "100", true, 100);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateShortTextEmptyVariableDB() throws Exception {
        verifyEvaluateVariable("", String.class, "evaluate ShortText Empty Variable DB", 23456L, "container_16", "'Edward'", false, "Edward");
    }

    @Test(expected = SInvalidExpressionException.class)
    public void evaluateShortTextEmptyVariableCache() throws Exception {
        verifyEvaluateVariable("  ", String.class, "evaluate ShortText Empty Variable Cache", 34567L, "container_17", "'Edward'", true, "Edward");
    }

}
