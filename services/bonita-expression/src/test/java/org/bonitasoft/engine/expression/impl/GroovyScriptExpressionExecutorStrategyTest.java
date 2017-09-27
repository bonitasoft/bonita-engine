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
