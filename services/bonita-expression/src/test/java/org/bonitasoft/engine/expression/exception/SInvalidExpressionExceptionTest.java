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
public class SInvalidExpressionExceptionTest {

    @Mock
    private Throwable cause;

    private final String expressionName = "expressionName";

    private final String message = "message";

    /**
     * Test method for {@link org.bonitasoft.engine.expression.exception.SInvalidExpressionException#getExpressionName()}.
     */
    @Test
    public final void return_expression_name_when_use_constructor_with_cause() {
        final SInvalidExpressionException sExpressionEvaluationException = new SInvalidExpressionException(cause, expressionName);

        final String result = sExpressionEvaluationException.getExpressionName();
        assertEquals(expressionName, result);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.expression.exception.SInvalidExpressionException#getExpressionName()}.
     */
    @Test
    public final void return_expression_name_when_use_constructor_with_message() {
        final SInvalidExpressionException sExpressionEvaluationException = new SInvalidExpressionException(message, expressionName);

        final String result = sExpressionEvaluationException.getExpressionName();
        assertEquals(expressionName, result);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.expression.exception.SInvalidExpressionException#getExpressionName()}.
     */
    @Test
    public final void return_expression_name_when_use_constructor_with_message_and_cause() {
        final SInvalidExpressionException sExpressionEvaluationException = new SInvalidExpressionException(message, cause, expressionName);

        final String result = sExpressionEvaluationException.getExpressionName();
        assertEquals(expressionName, result);
    }
}
