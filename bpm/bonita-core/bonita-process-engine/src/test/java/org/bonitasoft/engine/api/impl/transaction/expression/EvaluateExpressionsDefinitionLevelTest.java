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
package org.bonitasoft.engine.api.impl.transaction.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EvaluateExpressionsDefinitionLevelTest {

    @Mock
    private ExpressionResolverService expressionResolverService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Test
    public void executeShouldEnrichExceptionContextWithProcessDefinitionId() throws Exception {
        // given:
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        Long processDefId = 15935777L;
        when(processDef.getId()).thenReturn(processDefId);
        when(expressionResolverService.evaluate(any(SExpression.class), any(SExpressionContext.class))).thenThrow(
                new SExpressionEvaluationException("some msg", "some expressionName"));

        try {
            // when:
            new EvaluateExpressionsDefinitionLevel(null, processDefId, expressionResolverService, processDefinitionService, null).evaluateExpression(null, null,
                    processDef);
            fail("There should be a exception raised");
        } catch (SExpressionEvaluationException e) {
            // then:
            assertThat(e.getContext().get(SExceptionContext.PROCESS_DEFINITION_ID)).isEqualTo(processDefId);
        }
    }

    @Test
    public void executeShouldEnrichExceptionContextWithProcessDefinitionName() throws Exception {
        // given:
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        String processName = "my_process";
        when(processDef.getName()).thenReturn(processName);
        when(expressionResolverService.evaluate(any(SExpression.class), any(SExpressionContext.class))).thenThrow(
                new SExpressionEvaluationException("some msg", "some expressionName"));

        try {
            // when:
            new EvaluateExpressionsDefinitionLevel(null, 196584L, expressionResolverService, processDefinitionService, null).evaluateExpression(null, null,
                    processDef);
            fail("There should be a exception raised");
        } catch (SExpressionEvaluationException e) {
            // then:
            assertThat(e.getContext().get(SExceptionContext.PROCESS_NAME)).isEqualTo(processName);
        }
    }

    @Test
    public void executeShouldEnrichExceptionContextWithProcessDefinitionVersion() throws Exception {
        // given:
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        String processVersion = "7.3.1";
        when(processDef.getVersion()).thenReturn(processVersion);
        when(expressionResolverService.evaluate(any(SExpression.class), any(SExpressionContext.class))).thenThrow(
                new SExpressionEvaluationException("some msg", "some expressionName"));

        try {
            // when:
            new EvaluateExpressionsDefinitionLevel(null, 6453241L, expressionResolverService, processDefinitionService, null).evaluateExpression(null, null,
                    processDef);
            fail("There should be a exception raised");
        } catch (SExpressionEvaluationException e) {
            // then:
            assertThat(e.getContext().get(SExceptionContext.PROCESS_VERSION)).isEqualTo(processVersion);
        }
    }
}
