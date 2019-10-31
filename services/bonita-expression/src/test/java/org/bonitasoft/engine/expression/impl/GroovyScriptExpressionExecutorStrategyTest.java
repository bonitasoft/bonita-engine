/**
 * Copyright (C) 2015-2019 BonitaSoft S.A.
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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilder;
import org.bonitasoft.engine.expression.model.builder.impl.SExpressionBuilderFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExpressionExecutorStrategyTest {

    @Test
    public void evaluation_with_DefaultGroovyMethod_size_should_return_4()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "import static org.codehaus.groovy.runtime.DefaultGroovyMethods.*\n"
                + "size(new StringBuffer('test'))";
        SExpression expression = integerExpression(content);
        Object value = new GroovyScriptExpressionExecutorStrategy().evaluate(expression, emptyMap(), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(Integer.class)
                .isEqualTo(4);
    }

    @Test
    public void evaluation_with_string_size_should_return_4()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "new StringBuffer('test').size()";
        SExpression expression = integerExpression(content);
        Object value = new GroovyScriptExpressionExecutorStrategy().evaluate(expression, emptyMap(), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(Integer.class)
                .isEqualTo(4);
    }

    private static SExpressionBuilder expressionBuilder() {
        return new SExpressionBuilderFactoryImpl().createNewInstance().setName("test");
    }

    private static SExpression integerExpression(String content) throws SInvalidExpressionException {
        return expressionBuilder().setContent(content).setReturnType(Integer.class.getName()).done();
    }

    @Test
    public void evaluation_with_jsonBuilder_toString_should_work_on_java_8_and_java_11()
            throws SExpressionEvaluationException, SInvalidExpressionException {
        String content = "import groovy.json.JsonBuilder\n"
                + "new JsonBuilder('hello').toString()";
        SExpression expression = expressionBuilder().setContent(content).setReturnType(String.class.getName()).done();
        Object value = new GroovyScriptExpressionExecutorStrategy().evaluate(expression, emptyMap(), emptyMap(), null);
        assertThat(value)
                .isInstanceOf(String.class)
                .isEqualTo("\"hello\"");
    }

}
