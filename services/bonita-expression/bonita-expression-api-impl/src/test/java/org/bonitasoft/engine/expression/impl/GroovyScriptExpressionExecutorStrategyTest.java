package org.bonitasoft.engine.expression.impl;

import java.util.HashMap;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.impl.SExpressionBuilderFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExpressionExecutorStrategyTest {

    @Test
    public void testEvaluationWithDefaultGroovyMethod_Size_should_return_4() throws SExpressionEvaluationException, SInvalidExpressionException {
        GroovyScriptExpressionExecutorStrategy groovyScriptExpressionExecutorStrategy = new GroovyScriptExpressionExecutorStrategy();
        String content = "import static org.codehaus.groovy.runtime.DefaultGroovyMethods.*\n"
                + "size(new StringBuffer('test'))";
        SExpression expression = new SExpressionBuilderFactoryImpl().createNewInstance().setName("test").setContent(content)
                .setReturnType("java.lang.Integer").done();
        groovyScriptExpressionExecutorStrategy.evaluate(expression,
                new HashMap<String, Object>(), new HashMap<Integer, Object>(), null);
    }

    @Test
    public void testEvaluationWithString_Size_should_return_4() throws SExpressionEvaluationException, SInvalidExpressionException {
        GroovyScriptExpressionExecutorStrategy groovyScriptExpressionExecutorStrategy = new GroovyScriptExpressionExecutorStrategy();
        String content = "new StringBuffer('test').size()";
        SExpression expression = new SExpressionBuilderFactoryImpl().createNewInstance().setName("test").setContent(content)
                .setReturnType("java.lang.Integer").done();
        groovyScriptExpressionExecutorStrategy.evaluate(expression,
                new HashMap<String, Object>(), new HashMap<Integer, Object>(), null);
    }

}
