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
package org.bonitasoft.engine.expression.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class SExpressionEvaluationExceptionTest {

    @Mock
    private Throwable cause;

    private final String message = "message";

    private final String expressionName = "plop";

    /**
     * Test method for {@link org.bonitasoft.engine.expression.exception.SExpressionEvaluationException#getExpressionName()}.
     */
    @Test
    public final void return_expression_name_when_use_constructor_with_cause() {
        final SExpressionEvaluationException sExpressionEvaluationException = new SExpressionEvaluationException(cause, expressionName);

        final String result = sExpressionEvaluationException.getExpressionName();
        assertEquals(expressionName, result);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.expression.exception.SExpressionEvaluationException#getExpressionName()}.
     */
    @Test
    public final void return_expression_name_when_use_constructor_with_message() {
        final SExpressionEvaluationException sExpressionEvaluationException = new SExpressionEvaluationException(message, expressionName);

        final String result = sExpressionEvaluationException.getExpressionName();
        assertEquals(expressionName, result);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.expression.exception.SExpressionEvaluationException#getExpressionName()}.
     */
    @Test
    public final void return_expression_name_when_use_constructor_with_message_and_cause() {
        final SExpressionEvaluationException sExpressionEvaluationException = new SExpressionEvaluationException(message, cause, expressionName);

        final String result = sExpressionEvaluationException.getExpressionName();
        assertEquals(expressionName, result);
    }

}
