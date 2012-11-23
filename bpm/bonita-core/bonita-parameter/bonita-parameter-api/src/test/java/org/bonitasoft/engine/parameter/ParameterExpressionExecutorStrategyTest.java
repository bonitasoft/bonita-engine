package org.bonitasoft.engine.parameter;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 */
public abstract class ParameterExpressionExecutorStrategyTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ParameterExpressionExecutorStrategyTest.class);

    private static final Map<Integer, Serializable> EMPTY_RESOLVED_EXPRESSIONS = Collections.<Integer, Serializable> emptyMap();

    protected abstract ExpressionService getExpressionService();

    protected abstract SExpressionBuilder getExpressionBuilder();

    protected abstract ParameterService getParameterService();

    protected abstract String getParameterClassName();// SParameterImpl.class.getName()

    protected abstract SParameter createParameter(String name, String value);

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOGGER.info("Failed test: " + this.getClass().getName() + "." + d.getMethodName());
        };

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + this.getClass().getName() + "." + d.getMethodName());
        }

    };

    @After
    public void tearDown() throws Exception {
        // TestUtil.closeTransactionIfOpen(txService);
    }

    private SExpression newExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) throws SInvalidExpressionException {
        final SExpressionBuilder eb = getExpressionBuilder().createNewInstance();
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        return eb.done();
    }

    @Test
    public void evaluateParameterExpression() throws Exception {
        final String nameParameter = "name";
        final long processDefinitionId = UUID.randomUUID().getLeastSignificantBits();
        getParameterService().addAll(processDefinitionId, Collections.singletonMap(nameParameter, "baptiste"));

        final SExpression strExpr = newExpression(nameParameter, SExpression.TYPE_PARAMETER, null, null, null);
        final Map<String, Object> dependencies = CollectionUtil.buildSimpleMap("processDefinitionId", processDefinitionId);
        final Serializable expressionResult = getExpressionService().evaluate(strExpr, dependencies, EMPTY_RESOLVED_EXPRESSIONS);
        final SParameter para = createParameter("name", "baptiste");
        assertEquals(para, expressionResult);
    }

    @Test(expected = SParameterNameNotFoundException.class)
    public void evaluateExpressionWithAnUnknownParameter() throws Exception {

        final long processDefinitionId = UUID.randomUUID().getLeastSignificantBits();
        getParameterService().addAll(processDefinitionId, Collections.singletonMap("name", "baptiste"));

        final String expressionContent = "address";
        final Map<String, Object> dependencies = CollectionUtil.buildSimpleMap("processDefinitionId", processDefinitionId);
        dependencies.put("processDefinitionId", processDefinitionId);
        final SExpression strExpr = newExpression(expressionContent, SExpression.TYPE_PARAMETER, null, null, null);
        final Serializable expressionResult = getExpressionService().evaluate(strExpr, dependencies, EMPTY_RESOLVED_EXPRESSIONS);
        final SParameter para = createParameter("address", "");
        assertEquals(para, expressionResult);
    }
}
