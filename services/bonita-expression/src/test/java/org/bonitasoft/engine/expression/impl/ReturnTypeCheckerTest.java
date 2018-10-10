/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;

public class ReturnTypeCheckerTest {

    @Test
    public void checkReturnType_should_skip_processing_if_result_is_null() throws SExpressionEvaluationException {
        new ReturnTypeChecker().checkReturnType(null, null, null);
    }

    @Test
    public void checkReturnType_should_detect_result_and_expression_return_type_incompatibility() {
        // given:
        final SExpression expression = expression("expressionName", "java.util.List");

        // when:
        Throwable thrown = catchThrowable(
                () -> new ReturnTypeChecker().checkReturnType(expression, "a String expression result",
                        Collections.emptyMap()));

        //then:
        assertThat(thrown).isInstanceOf(SExpressionEvaluationException.class)
                .hasMessageContaining("expressionName")
                .hasMessageContaining("java.util.List")
                .hasMessageContaining("java.lang.String");
    }

    @Test
    public void checkReturnType_should_allow_result_and_expression_return_type_that_are_convertible() throws Exception {
        final SExpression expression = expression("expressionName", "org.bonitasoft.engine.bpm.document.Document");
        final FileInputValue result = new FileInputValue("name.txt", "a content".getBytes(StandardCharsets.UTF_8));

        new ReturnTypeChecker().checkReturnType(expression, result, Collections.emptyMap());
    }

    @Test
    public void checkReturnType_should_fill_exception_context_with_activity_instance_id_if_any() {
        // given:
        final SExpression expression = expression("expressionName", "InvalidClassName_causing_ClassNotFound");
        final Map<String, Object> context = new HashMap<>();
        final long activityInstanceId = 1499999L;
        context.put("containerType", "ACTIVITY_INSTANCE");
        context.put("containerId", activityInstanceId);

        // when:
        Throwable thrown = catchThrowable(
                () -> new ReturnTypeChecker().checkReturnType(expression, "some evaluated expression result", context));

        // then:
        assertThat(thrown).isInstanceOf(SExpressionEvaluationException.class)
                .hasMessageContaining("expressionName")
                .hasMessageContaining("InvalidClassName_causing_ClassNotFound");
        assertThat(((SExpressionEvaluationException) thrown).getContext().get(SExceptionContext.FLOW_NODE_INSTANCE_ID))
                .isEqualTo(activityInstanceId);
    }

    @Test
    public void checkReturnType_should_fill_exception_context_with_process_instance_id_if_any() {
        // given:
        final SExpression expression = expression("expressionName", "InvalidClassName_causing_ClassNotFound");
        final Map<String, Object> context = new HashMap<>();
        final long processInstanceId = 8779L;
        context.put("containerType", "PROCESS_INSTANCE");
        context.put("containerId", processInstanceId);

        // when:
        Throwable thrown = catchThrowable(
                () -> new ReturnTypeChecker().checkReturnType(expression, "some evaluated expression result", context));

        // then:
        assertThat(thrown).isInstanceOf(SExpressionEvaluationException.class)
                .hasMessageContaining("expressionName")
                .hasMessageContaining("InvalidClassName_causing_ClassNotFound");
        assertThat(((SExpressionEvaluationException) thrown).getContext().get(SExceptionContext.PROCESS_INSTANCE_ID))
                .isEqualTo(processInstanceId);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static SExpression expression(String name, String returnType) {
        return new SExpressionImpl(name, "not_used_here", null, returnType, null, null);
    }

}
