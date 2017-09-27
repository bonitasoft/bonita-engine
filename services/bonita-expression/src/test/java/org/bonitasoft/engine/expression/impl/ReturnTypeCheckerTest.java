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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;

public class ReturnTypeCheckerTest {

    @Test
    public void checkReturnTypeShouldFillExceptionContextWithActivityInstanceIdIfAny() {
        // given:
        final SExpression expression = new SExpressionImpl("expressionName", "not_used_here", null, "InvalidClassName_causing_ClassNotFound", null, null);
        final Map<String, Object> context = new HashMap<String, Object>();
        final long activityInstanceId = 1499999L;
        context.put("containerType", "ACTIVITY_INSTANCE");
        context.put("containerId", activityInstanceId);

        try {
            // when:
            new ReturnTypeChecker().checkReturnType(expression, "some evaluated expression result", context);

            fail("Should have raised an exception");
        } catch (final SExpressionEvaluationException e) {
            // then:
            assertThat(e.getContext().get(SExceptionContext.FLOW_NODE_INSTANCE_ID)).isEqualTo(activityInstanceId);
        }
    }

    @Test
    public void checkReturnTypeShouldFillExceptionContextWithProcessInstanceIdIfAny() {
        // given:
        final SExpression expression = new SExpressionImpl("expressionName", "not_used_here", null, "InvalidClassName_causing_ClassNotFound", null, null);
        final Map<String, Object> context = new HashMap<String, Object>();
        final long processInstanceId = 8779L;
        context.put("containerType", "PROCESS_INSTANCE");
        context.put("containerId", processInstanceId);

        try {
            // when:
            new ReturnTypeChecker().checkReturnType(expression, "some evaluated expression result", context);

            fail("Should have raised an exception");
        } catch (final SExpressionEvaluationException e) {
            // then:
            assertThat(e.getContext().get(SExceptionContext.PROCESS_INSTANCE_ID)).isEqualTo(processInstanceId);
        }
    }
}
